package com.example.lankagotest

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*

class DriverTripsActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var rvTrips: RecyclerView
    private val tripList = ArrayList<TripSchedule>()
    private lateinit var adapter: ScheduleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_schedules) // Reusing the same layout structure

        database = FirebaseDatabase.getInstance().reference
        rvTrips = findViewById(R.id.rvTrips)
        rvTrips.layoutManager = LinearLayoutManager(this)

        adapter = ScheduleAdapter(tripList)
        rvTrips.adapter = adapter

        setupBottomNavigation()
        fetchTrips()
    }

    private fun setupBottomNavigation() {
        val driverBottomNav = findViewById<BottomNavigationView>(R.id.driverBottomNav)
        driverBottomNav.selectedItemId = R.id.nav_driver_trips

        driverBottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_driver_home -> {
                    startActivity(Intent(this, DriverHomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_driver_schedule -> {
                    startActivity(Intent(this, DriverSchedulesActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_driver_trips -> true
                R.id.nav_driver_notifications -> {
                    startActivity(Intent(this, DriverNotificationActivity2::class.java))
                    finish()
                    true
                }
                R.id.nav_driver_profile -> {
                    startActivity(Intent(this, DriverProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchTrips() {
        // Fetch logic similar to schedule or as needed
        database.child("DriverTrips").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tripList.clear()
                for (tripSnapshot in snapshot.children) {
                    val trip = tripSnapshot.getValue(TripSchedule::class.java)
                    if (trip != null) {
                        tripList.add(trip)
                    }
                }
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
