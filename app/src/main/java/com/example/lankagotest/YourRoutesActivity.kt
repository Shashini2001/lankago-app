package com.example.lankagotest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.*
import java.util.Locale
import java.util.UUID

class YourRoutesActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var database: DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    private var selectedRouteId: String = ""
    private var userLocation: Location? = null
    
    // UI Elements
    private lateinit var tvDistanceAway: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_youselect_routes)

        database = FirebaseDatabase.getInstance().reference
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Get Route data passed from the previous screen
        selectedRouteId = intent.getStringExtra("ROUTE_ID") ?: "177"
        val routeName = intent.getStringExtra("ROUTE_NAME") ?: "MALABE - KADUWELA"

        // Setup UI
        findViewById<TextView>(R.id.tvTripTitle).text = "YOUR TRIP: $routeName"
        findViewById<TextView>(R.id.tvRouteNumber).text = "Route : $selectedRouteId"
        tvDistanceAway = findViewById(R.id.tvDistanceAway)
        
        val btnShare = findViewById<ImageButton>(R.id.btnShare)
        val btnEndTrip = findViewById<AppCompatButton>(R.id.btnEndTrip)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        // Initialize Map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Share Live Location
        btnShare.setOnClickListener {
            shareTripDetails(routeName)
        }

        // End Trip Button
        btnEndTrip.setOnClickListener {
            Toast.makeText(this, "Trip Ended", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, PassengerHomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Clears backstack
            startActivity(intent)
            finish()
        }

        // Bottom Navigation Logic
        bottomNav.selectedItemId = R.id.nav_routes
        bottomNav.setOnItemSelectedListener { item ->
            // Implement your standard navigation logic here
            true
        }

        // Request GPS Permissions
        fetchUserLocation()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        
        // Listen to Firebase ONLY for buses on this route
        trackBusesForRoute(selectedRouteId)
    }

    private fun trackBusesForRoute(routeId: String) {
        val activeBusesRef = database.child("ActiveBuses")

        activeBusesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mMap.clear()
                var closestDistance = Float.MAX_VALUE
                var activeBusFound = false

                for (busSnapshot in snapshot.children) {
                    val busRouteId = busSnapshot.child("routeId").getValue(String::class.java)
                    
                    // Filter: Only show buses for the selected route
                    if (busRouteId == routeId) {
                        val lat = busSnapshot.child("latitude").getValue(Double::class.java)
                        val lng = busSnapshot.child("longitude").getValue(Double::class.java)

                        if (lat != null && lng != null) {
                            activeBusFound = true
                            val busLatLng = LatLng(lat, lng)
                            
                            // Add Bus Marker
                            mMap.addMarker(
                                MarkerOptions()
                                    .position(busLatLng)
                                    .title("Bus $routeId")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                            )

                            // Calculate distance if user location is known
                            userLocation?.let { userLoc ->
                                val busLoc = Location("").apply { latitude = lat; longitude = lng }
                                val distanceInMeters = userLoc.distanceTo(busLoc)
                                
                                if (distanceInMeters < closestDistance) {
                                    closestDistance = distanceInMeters
                                }
                            }
                        }
                    }
                }

                // Update the Distance Text Box
                if (activeBusFound && closestDistance != Float.MAX_VALUE) {
                    val distanceKm = String.format(Locale.getDefault(), "%.1f", closestDistance / 1000)
                    tvDistanceAway.text = "${distanceKm}Km Away"
                    
                    // Auto-zoom map to show Sri Lanka roughly
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(7.8731, 80.7718), 7f)) 
                } else if (!activeBusFound) {
                    tvDistanceAway.text = "No buses found"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@YourRoutesActivity, "Database Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }
        
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                userLocation = location
                mMap.isMyLocationEnabled = true 
            }
        }
    }

    private fun shareTripDetails(routeName: String) {
        // 1. Create a unique ID for this specific trip
        val uniqueTripId = UUID.randomUUID().toString().substring(0, 8)
        val trackingLink = "https://lankago.app/track?trip=$uniqueTripId"
        val shareMessage = "I'm on my way! Follow Shashini's trip live on Lanka Go: $trackingLink"

        // 2. Save the trip intent to Firebase so the link actually works when clicked
        val tripData = mapOf(
            "passengerName" to "Shashini", // You can fetch this from auth.currentUser later
            "routeId" to selectedRouteId,
            "destination" to routeName,
            "isActive" to true
        )
        database.child("SharedTrips").child(uniqueTripId).setValue(tripData)

        // 3. Show the Custom Bottom Sheet
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_share_bottom_sheet, null)
        bottomSheetDialog.setContentView(view)

        // Find the buttons inside the bottom sheet
        val btnWhatsApp = view.findViewById<LinearLayout>(R.id.btnShareWhatsApp)
        val btnMessenger = view.findViewById<LinearLayout>(R.id.btnShareMessenger)
        val btnMore = view.findViewById<LinearLayout>(R.id.btnShareMore)

        // Handle WhatsApp Click
        btnWhatsApp.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.setPackage("com.whatsapp") // Targets WhatsApp directly
            intent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "WhatsApp is not installed.", Toast.LENGTH_SHORT).show()
            }
            bottomSheetDialog.dismiss()
        }

        // Handle Messenger Click
        btnMessenger.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.setPackage("com.facebook.orca") // Targets Messenger directly
            intent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Messenger is not installed.", Toast.LENGTH_SHORT).show()
            }
            bottomSheetDialog.dismiss()
        }

        // Handle General Share (Matches Uber's Native Sheet exactly)
        btnMore.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(intent, "Share Live Trip"))
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }
}
