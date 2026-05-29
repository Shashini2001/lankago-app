package com.example.lankagotest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*

class SeatBookingActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var rvBusResults: RecyclerView
    private lateinit var adapter: BusResultAdapter
    private val tripList = ArrayList<TripData>()

    private lateinit var tvFromLocation: TextView
    private lateinit var tvToLocation: TextView
    private lateinit var btnSearchBuses: Button

    // Launcher to get the location selected by the user from your Location List screen
    private val selectLocationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedCity = result.data?.getStringExtra("SELECTED_CITY")
            val isFrom = result.data?.getBooleanExtra("IS_FROM", true) ?: true

            if (isFrom) {
                tvFromLocation.text = selectedCity
            } else {
                tvToLocation.text = selectedCity
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seat_booking)

        database = FirebaseDatabase.getInstance().reference

        tvFromLocation = findViewById(R.id.tvFromLocation)
        tvToLocation = findViewById(R.id.tvToLocation)
        btnSearchBuses = findViewById(R.id.btnSearchBuses)
        rvBusResults = findViewById(R.id.rvBusResults)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        btnBack.setOnClickListener { finish() }

        // Setup RecyclerView
        rvBusResults.layoutManager = LinearLayoutManager(this)
        adapter = BusResultAdapter(tripList) { selectedTrip ->
            // --- GO TO SEAT SELECTION SCREEN ---
            val intent = Intent(this, SeatSelectionActivity::class.java)
            intent.putExtra("TRIP_ID", selectedTrip.tripId)
            intent.putExtra("BUS_NAME", selectedTrip.busName)
            intent.putExtra("PRICE", selectedTrip.price)
            startActivity(intent)
        }
        rvBusResults.adapter = adapter

        // 1. Open Location List when clicking "From"
        tvFromLocation.setOnClickListener {
            val intent = Intent(this, LocationSelectionActivity::class.java)
            intent.putExtra("IS_FROM", true)
            selectLocationLauncher.launch(intent)
        }

        // 2. Open Location List when clicking "To"
        tvToLocation.setOnClickListener {
            val intent = Intent(this, LocationSelectionActivity::class.java)
            intent.putExtra("IS_FROM", false)
            selectLocationLauncher.launch(intent)
        }

        // 3. Search Firebase when clicking Search Button
        btnSearchBuses.setOnClickListener {
            val from = tvFromLocation.text.toString()
            val to = tvToLocation.text.toString()

            if (from == "Select From" || to == "Select To") {
                Toast.makeText(this, "Please select locations", Toast.LENGTH_SHORT).show()
            } else {
                searchBusesInFirebase(from, to)
            }
        }

        // Bottom Navigation Logic
        bottomNav.selectedItemId = R.id.nav_routes
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, PassengerHomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_routes -> true
                R.id.nav_track -> {
                    startActivity(Intent(this, LiveTrackingActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_notifications -> {
                    startActivity(Intent(this, PassengerNotificationActivity2::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, PassengerProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun searchBusesInFirebase(from: String, to: String) {
        Toast.makeText(this, "Searching...", Toast.LENGTH_SHORT).show()
        
        database.child("Trips").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tripList.clear()

                for (tripSnapshot in snapshot.children) {
                    val trip = tripSnapshot.getValue(TripData::class.java)
                    
                    if (trip != null && trip.fromLocation == from && trip.toLocation == to) {
                        val completeTrip = trip.copy(tripId = tripSnapshot.key ?: "")
                        tripList.add(completeTrip)
                    }
                }

                adapter.notifyDataSetChanged()

                if (tripList.isEmpty()) {
                    Toast.makeText(this@SeatBookingActivity, "No buses found for this route", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SeatBookingActivity, "Database Error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
