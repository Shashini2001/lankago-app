package com.example.lankagotest

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class PassengerHomeActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passenger_home)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        drawerLayout = findViewById(R.id.drawerLayout)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        val btnMenuDrawer = findViewById<ImageButton>(R.id.btnMenuDrawer)

        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val imgProfile = findViewById<ShapeableImageView>(R.id.imgProfile)
        val etSearchBus = findViewById<EditText>(R.id.etSearchBus)
        val btnTrackBus = findViewById<AppCompatButton>(R.id.btnTrackBus)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

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
                    auth.signOut()
                    startActivity(Intent(this, PassengerSignInActivity::class.java))
                    finishAffinity()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // 1. Fetch User Data (Name & Profile Pic)
        loadUserProfile(tvGreeting, imgProfile)

        // 2. Initialize Google Map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        // 3. Search Bar Logic (Dummy setup - connect to database later)
        etSearchBus?.setOnEditorActionListener { _, _, _ ->
            val query = etSearchBus.text.toString()
            Toast.makeText(this, "Searching for bus: $query", Toast.LENGTH_SHORT).show()
            true
        }

        // 4. Track Bus Button Logic -> Go to Live Tracking UI
        btnTrackBus?.setOnClickListener {
            val intent = Intent(this, LiveTrackingActivity::class.java)
            startActivity(intent)
        }

        // 5. Bottom Navigation Logic
        bottomNav?.selectedItemId = R.id.nav_home
        bottomNav?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true // Already here
                R.id.nav_routes -> {
                    startActivity(Intent(this, YourselectRoutesActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_track -> {
                    startActivity(Intent(this, LiveTrackingActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_notifications -> {
                    startActivity(Intent(this, PassengerNotificationActivity2::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, PassengerProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }

    // Connect to Firebase to get User Profile
    private fun loadUserProfile(tvGreeting: TextView?, imgProfile: ShapeableImageView?) {
        val user = auth.currentUser
        if (user != null) {
            database.child("Users").child(user.uid).get().addOnSuccessListener { snapshot ->
                val name = snapshot.child("username").value?.toString() ?: "Passenger"
                tvGreeting?.text = "Hi $name,"

                val profilePicUrl = snapshot.child("profileImageUrl").value?.toString()
                if (profilePicUrl != null && imgProfile != null) {
                    Glide.with(this).load(profilePicUrl).into(imgProfile)
                }
            }
        }
    }

    // Setup the Google Map
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Focus map on Colombo/Malabe area as a default starting point
        val malabe = LatLng(6.9044, 79.9616)
        mMap.addMarker(MarkerOptions().position(malabe).title("You are here"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(malabe, 14f))
    }
}
