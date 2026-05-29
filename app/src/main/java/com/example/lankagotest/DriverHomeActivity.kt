package com.example.lankagotest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class DriverHomeActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var isTripActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_LankaGotest)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_home)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val tvGreeting = findViewById<TextView>(R.id.tvDriverGreeting)
        val imgProfile = findViewById<ImageView>(R.id.imgDriverProfile)
        val tvStatus = findViewById<TextView>(R.id.tvBusStatus)
        val btnStartStop = findViewById<AppCompatButton>(R.id.btnStartStopTrip)
        val btnScanQr = findViewById<AppCompatButton>(R.id.btnScanQr)
        val btnViewBookings = findViewById<AppCompatButton>(R.id.btnViewBookings)
        val driverBottomNav = findViewById<BottomNavigationView>(R.id.driverBottomNav)

        // Load Driver Profile
        loadDriverProfile(tvGreeting, imgProfile)

        // Navigation to Bookings
        btnViewBookings.setOnClickListener {
            startActivity(Intent(this, DriverBookingsActivity::class.java))
        }

        // Initialize Map programmatically to avoid InflateException
        val mapFragment = supportFragmentManager.findFragmentById(R.id.driverMap) as? SupportMapFragment
            ?: SupportMapFragment.newInstance().also {
                supportFragmentManager.beginTransaction()
                    .add(R.id.driverMap, it)
                    .commit()
            }
        mapFragment.getMapAsync(this)

        // setup Bottom Navigation
        setupBottomNavigation(driverBottomNav)

        // Start/Stop Trip Logic
        btnStartStop.setOnClickListener {
            // 1. Check if we have permission first
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
                return@setOnClickListener
            }

            val serviceIntent = Intent(this, DriverLocationService::class.java)

            if (!isTripActive) {
                // START LIVE TRACKING
                isTripActive = true
                btnStartStop.text = "STOP TRIP \n(Turn off Live Location)"
                btnStartStop.setBackgroundColor(Color.parseColor("#E53935")) // Turn red
                tvStatus.text = "Status: Online & Tracking"
                tvStatus.setTextColor(Color.parseColor("#2E8B57"))
                
                // Start the background service
                ContextCompat.startForegroundService(this, serviceIntent)
                Toast.makeText(this, "Live Location Started!", Toast.LENGTH_LONG).show()
                
            } else {
                // STOP LIVE TRACKING
                isTripActive = false
                btnStartStop.text = "START TRIP \n(Turn on Live Location)"
                btnStartStop.setBackgroundResource(R.drawable.bg_big_button_green) // Turn green
                tvStatus.text = "Status: Offline"
                tvStatus.setTextColor(Color.parseColor("#D32F2F"))
                
                // Stop the background service
                stopService(serviceIntent)
                Toast.makeText(this, "Trip Ended.", Toast.LENGTH_LONG).show()
            }
        }

        // Scan QR Logic
        btnScanQr.setOnClickListener {
            val intent = Intent(this, QrScannerActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupBottomNavigation(driverBottomNav: BottomNavigationView) {
        // 1. Set the currently selected item so it highlights correctly on this page
        driverBottomNav.selectedItemId = R.id.nav_driver_home

        // 2. Add the Red Notification Badge
        val badge = driverBottomNav.getOrCreateBadge(R.id.nav_driver_notifications)
        badge.isVisible = true
        badge.number = 1
        badge.backgroundColor = Color.parseColor("#E53935") // Red background
        badge.badgeTextColor = Color.WHITE

        // 3. Handle Navigation Clicks
        driverBottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_driver_home -> {
                    // We are already on Home, do nothing
                    true
                }
                R.id.nav_driver_schedule -> {
                    startActivity(Intent(this, DriverSchedulesActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_driver_trips -> {
                    startActivity(Intent(this, DriverTripsActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_driver_notifications -> {
                    // Clear the badge when they click on notifications!
                    driverBottomNav.removeBadge(R.id.nav_driver_notifications)
                    startActivity(Intent(this, DriverNotificationActivity2::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_driver_profile -> {
                    startActivity(Intent(this, DriverProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun loadDriverProfile(tvGreeting: TextView, imgProfile: ImageView) {
        val user = auth.currentUser
        if (user != null) {
            database.child("Users").child(user.uid).get().addOnSuccessListener { snapshot ->
                // 1. Set Username Greeting
                val name = snapshot.child("username").value?.toString() ?: "Driver"
                tvGreeting.text = "Hi $name,"

                // 2. Load Profile Image (Base64)
                val base64Image = snapshot.child("profileImageBase64").getValue(String::class.java)
                if (!base64Image.isNullOrEmpty()) {
                    try {
                        val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
                        val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        imgProfile.setImageBitmap(decodedImage)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        }
        val colombo = LatLng(6.9271, 79.8612)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(colombo, 12f))
    }
}
