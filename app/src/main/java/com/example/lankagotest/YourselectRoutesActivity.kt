package com.example.lankagotest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
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
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.Locale

class YourselectRoutesActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var database: DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var drawerLayout: DrawerLayout

    private var selectedRouteId: String = ""
    private var userLocation: Location? = null

    // UI Elements
    private lateinit var tvDistanceAway: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_youselect_routes)

        database = FirebaseDatabase.getInstance().reference
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        drawerLayout = findViewById(R.id.drawerLayout)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        val btnMenuDrawer = findViewById<ImageButton>(R.id.btnMenuDrawer)

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

        // Drawer Menu Click
        btnMenuDrawer.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Handle Drawer Menu Clicks
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_settings -> Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
                R.id.nav_about -> startActivity(Intent(this, AboutUsActivity::class.java))
                R.id.nav_qr_payment -> startActivity(Intent(this, QrScannerActivity::class.java))
                R.id.nav_seat_booking -> startActivity(Intent(this, SeatBookingActivity::class.java))
                R.id.nav_languages -> startActivity(Intent(this, LanguageSelectionActivity::class.java))
                R.id.nav_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, PassengerSignInActivity::class.java))
                    finishAffinity()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

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
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, PassengerHomeActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_routes -> true
                R.id.nav_track -> {
                    startActivity(Intent(this, LiveTrackingActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_notifications -> {
                    startActivity(Intent(this, PassengerNotificationActivity2::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, PassengerProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
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
                                    // Replace with your custom bus icon if you have one
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

                    // Auto-zoom map to show buses (optional)
                    if (snapshot.childrenCount > 0) {
                        // Just an example, zoom to Sri Lanka roughly
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(7.8731, 80.7718), 7f))
                    }
                } else if (!activeBusFound) {
                    tvDistanceAway.text = "No buses found"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@YourselectRoutesActivity, "Database Error", Toast.LENGTH_SHORT).show()
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
                // Add a blue dot for the user on the map
                mMap.isMyLocationEnabled = true
            }
        }
    }

    private fun shareTripDetails(routeName: String) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "I am currently tracking a bus on $routeName using Lanka Go! My ETA is 15 mins.")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Share Trip via"))
    }
}
