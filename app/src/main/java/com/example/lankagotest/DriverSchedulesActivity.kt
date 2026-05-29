package com.example.lankagotest

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*

class DriverSchedulesActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var rvTrips: RecyclerView
    private val tripList = ArrayList<TripSchedule>()
    private lateinit var adapter: ScheduleAdapter

    private val driverId = "driver_samantha_123" // Replace with actual logged-in ID
    private val selectedDate = "2026-02-05" // You can make this dynamic later

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_schedules)

        database = FirebaseDatabase.getInstance().reference
        rvTrips = findViewById(R.id.rvTrips)
        rvTrips.layoutManager = LinearLayoutManager(this)

        adapter = ScheduleAdapter(tripList)
        rvTrips.adapter = adapter

        setupBottomNavigation()
        fetchScheduleFromFirebase()
    }

    private fun setupBottomNavigation() {
        val driverBottomNav = findViewById<BottomNavigationView>(R.id.driverBottomNav)

        // 1. Set the currently selected item so it highlights correctly on this page
        driverBottomNav.selectedItemId = R.id.nav_driver_schedule

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
                    startActivity(Intent(this, DriverHomeActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_driver_schedule -> {
                    // We are already on Schedule, do nothing
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

    private fun fetchScheduleFromFirebase() {
        // Point to the specific driver and specific date
        val scheduleRef = database.child("DriverSchedules").child(driverId).child(selectedDate)

        scheduleRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // 1. Set the top Bus Info box
                    val busNo = snapshot.child("busNo").getValue(String::class.java)
                    val route = snapshot.child("route").getValue(String::class.java)

                    findViewById<TextView>(R.id.tvBusNo).text = "Bus No : $busNo"
                    findViewById<TextView>(R.id.tvRoute).text = "Route : $route"

                    // 2. Load the Trips List
                    tripList.clear()
                    val tripsSnapshot = snapshot.child("trips")
                    for (tripData in tripsSnapshot.children) {
                        val trip = tripData.getValue(TripSchedule::class.java)
                        if (trip != null) {
                            tripList.add(trip)
                        }
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@DriverSchedulesActivity, "No schedule found for today", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DriverSchedulesActivity, "Failed to load schedule", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
