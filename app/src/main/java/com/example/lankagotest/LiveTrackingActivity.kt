package com.example.lankagotest

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class LiveTrackingActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var database: DatabaseReference
    private lateinit var drawerLayout: DrawerLayout

    // Keep track of active bus markers so we can move them instead of redrawing
    private val activeBusMarkers = HashMap<String, Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use activity_track as it contains the required DrawerLayout, Map, and Buttons
        setContentView(R.layout.activity_track)

        database = FirebaseDatabase.getInstance().reference
        drawerLayout = findViewById(R.id.drawerLayout)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)

        val btnMenuDrawer = findViewById<ImageButton>(R.id.btnMenuDrawer)
        val btnLocationMap = findViewById<ImageButton>(R.id.btnLocationMap)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        // Initialize Map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Top Bar Buttons
        btnMenuDrawer.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Handle Drawer Item Clicks
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, PassengerHomeActivity::class.java))
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, PassengerProfileActivity::class.java))
                }
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

        btnLocationMap.setOnClickListener {
            // Re-center map to the default route view
            val malabe = LatLng(6.9044, 79.9616)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(malabe, 13f))
        }

        // Bottom Navigation Logic (Track tab selected!)
        bottomNav.selectedItemId = R.id.nav_track
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, PassengerHomeActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_routes -> {
                    startActivity(Intent(this, YourselectRoutesActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_track -> true // Already here
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
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // 1. Draw the Blue Route Line
        drawRoutePolyline()

        // 2. Add the "Your Stop" Marker
        addBusStopMarker()

        // 3. Start tracking the buses!
        listenForLiveBuses()

        // Center map perfectly between Malabe and Kaduwela
        val centerPoint = LatLng(6.9186, 79.9730)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerPoint, 13f))
    }

    // --- DRAWS THE BLUE LINE ON THE MAP ---
    private fun drawRoutePolyline() {
        // You would typically get these coordinates from an API or database, 
        // but here is a hardcoded path for the Malabe -> Kaduwela route.
        val routeCoordinates = listOf(
            LatLng(6.9044, 79.9616), // Malabe
            LatLng(6.9120, 79.9680), // Midpoint 1
            LatLng(6.9230, 79.9750), // Midpoint 2
            LatLng(6.9328, 79.9845)  // Kaduwela
        )

        val polylineOptions = PolylineOptions()
            .addAll(routeCoordinates)
            .color(android.graphics.Color.parseColor("#4A7BBF")) // Your app's blue
            .width(12f) // Thickness of the line
            .geodesic(true)

        mMap.addPolyline(polylineOptions)
    }

    // --- ADDS THE BUS STOP MARKER ---
    private fun addBusStopMarker() {
        val kaduwelaStop = LatLng(6.9328, 79.9845)
        mMap.addMarker(
            MarkerOptions()
                .position(kaduwelaStop)
                .title("Your stop")
                .snippet("Kaduwela Bus Stand")
        )?.showInfoWindow() // Automatically pops up the "Your stop" text bubble
    }

    // --- LIVE TRACKING FROM FIREBASE ---
    private fun listenForLiveBuses() {
        val activeBusesRef = database.child("ActiveBuses")

        activeBusesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (busSnapshot in snapshot.children) {
                    val busId = busSnapshot.key ?: continue
                    val lat = busSnapshot.child("latitude").getValue(Double::class.java)
                    val lng = busSnapshot.child("longitude").getValue(Double::class.java)

                    if (lat != null && lng != null) {
                        val busLocation = LatLng(lat, lng)

                        // If marker already exists on the map, just move it (smooth tracking)
                        if (activeBusMarkers.containsKey(busId)) {
                            activeBusMarkers[busId]?.position = busLocation
                        } else {
                            // If it's a new bus, create a new marker
                            val marker = mMap.addMarker(
                                MarkerOptions()
                                    .position(busLocation)
                                    .title("Bus 177")
                                    // Make it yellow to match your design
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                            )
                            if (marker != null) {
                                activeBusMarkers[busId] = marker
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LiveTrackingActivity, "Failed to load live buses.", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
