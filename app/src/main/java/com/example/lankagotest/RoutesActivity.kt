package com.example.lankagotest

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class RoutesActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var rvRoutes: RecyclerView
    private lateinit var routeAdapter: RouteAdapter
    private lateinit var routeList: ArrayList<BusRoute>
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_routes)

        // Initialize UI Elements
        drawerLayout = findViewById(R.id.drawerLayout)
        val btnMenu = findViewById<ImageButton>(R.id.btnMenu)
        val btnLocation = findViewById<ImageButton>(R.id.btnLocation)
        val etSearchRoute = findViewById<EditText>(R.id.etSearchRoute)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        rvRoutes = findViewById(R.id.rvRoutes)

        // Setup RecyclerView
        rvRoutes.layoutManager = LinearLayoutManager(this)
        rvRoutes.setHasFixedSize(true)
        routeList = arrayListOf()

        // Initialize Adapter & Handle Route Clicks
        routeAdapter = RouteAdapter(routeList) { selectedRoute ->
            // Navigate to Your Routes UI when an item is clicked
            val intent = Intent(this, YourRoutesActivity::class.java) // Ensure this activity exists
            intent.putExtra("ROUTE_NAME", selectedRoute.routeName)
            startActivity(intent)
        }
        rvRoutes.adapter = routeAdapter

        // Fetch Data from Firebase
        database = FirebaseDatabase.getInstance().getReference("Routes")
        fetchRoutesFromFirebase()

        // Setup Search Bar Logic
        etSearchRoute.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Top Bar Navigation Clicks
        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START) // Opens the slide-out menu
        }

        // Navigation Drawer Item Clicks
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, PassengerHomeActivity::class.java))
                    finish()
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, PassengerProfileActivity::class.java))
                }
                R.id.nav_settings -> {
                    Toast.makeText(this, "Settings Clicked", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_about -> {
                    startActivity(Intent(this, AboutUsActivity::class.java))
                }
                R.id.nav_seat_booking -> {
                    startActivity(Intent(this, SeatBookingActivity::class.java))
                }
                R.id.nav_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, PassengerSignInActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        btnLocation.setOnClickListener {
            // Navigate to the Google Map (Passenger Home)
            startActivity(Intent(this, PassengerHomeActivity::class.java))
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
                R.id.nav_routes -> true // Already here
                R.id.nav_track -> {
                    startActivity(Intent(this, LiveTrackingActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_notifications -> {
                    Toast.makeText(this, "Notifications Clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_profile -> {
                    Toast.makeText(this, "Profile Clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchRoutesFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                routeList.clear()
                if (snapshot.exists()) {
                    for (routeSnapshot in snapshot.children) {
                        val routeName = routeSnapshot.child("routeName").getValue(String::class.java)
                        val routeId = routeSnapshot.key
                        if (routeName != null && routeId != null) {
                            routeList.add(BusRoute(routeId, routeName))
                        }
                    }
                    routeAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RoutesActivity, "Failed to load routes", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun filter(text: String) {
        val filteredList = ArrayList<BusRoute>()
        for (item in routeList) {
            // Case insensitive search
            if (item.routeName.lowercase().contains(text.lowercase())) {
                filteredList.add(item)
            }
        }
        routeAdapter.filterList(filteredList)
    }
}
