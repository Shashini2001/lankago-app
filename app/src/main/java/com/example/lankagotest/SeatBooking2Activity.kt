package com.example.lankagotest

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.bottomnavigation.BottomNavigationView

class SeatBooking2Activity : AppCompatActivity() {

    private var selectedDate = "2026-05-25"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seat_booking)

        // Find all the UI elements
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val etFrom = findViewById<TextView>(R.id.tvFromLocation)
        val etTo = findViewById<TextView>(R.id.tvToLocation)
        val btnSwap = findViewById<ImageButton>(R.id.btnSwap)
        val btnToday = findViewById<AppCompatButton>(R.id.btnToday)
        val btnTomorrow = findViewById<AppCompatButton>(R.id.btnTomorrow)
        val btnSearchBuses = findViewById<AppCompatButton>(R.id.btnSearchBuses)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        // 1. Back Button Action
        btnBack.setOnClickListener {
            finish()
        }

        // 2. Swap Button Action (Flips 'From' and 'To' text)
        btnSwap.setOnClickListener {
            val temp = etFrom.text.toString()
            etFrom.setText(etTo.text.toString())
            etTo.setText(temp)
        }

        // 3. Date Selection Logic
        btnToday.setOnClickListener {
            selectedDate = "2026-05-25"
            btnToday.setBackgroundColor(Color.parseColor("#4A7BBF"))
            btnToday.setTextColor(Color.WHITE)

            btnTomorrow.setBackgroundResource(R.drawable.bg_input_card)
            btnTomorrow.setTextColor(Color.parseColor("#555555"))
        }

        btnTomorrow.setOnClickListener {
            selectedDate = "2026-05-26"
            btnTomorrow.setBackgroundColor(Color.parseColor("#4A7BBF"))
            btnTomorrow.setTextColor(Color.WHITE)

            btnToday.setBackgroundResource(R.drawable.bg_input_card)
            btnToday.setTextColor(Color.parseColor("#555555"))
        }

        // 4. Search Button Action
        btnSearchBuses.setOnClickListener {
            val fromLocation = etFrom.text.toString().trim()
            val toLocation = etTo.text.toString().trim()

            if (fromLocation.isEmpty() || toLocation.isEmpty()) {
                Toast.makeText(this, "Please enter both locations", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Searching buses from $fromLocation to $toLocation...", Toast.LENGTH_SHORT).show()

            // Navigate to Bus Results
            val intent = Intent(this, BusResultsActivity::class.java)
            intent.putExtra("FROM", fromLocation)
            intent.putExtra("TO", toLocation)
            intent.putExtra("DATE", selectedDate)
            startActivity(intent)
        }

        // 5. Bottom Navigation Setup
        bottomNav.selectedItemId = R.id.nav_routes // Highlight the correct tab
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, PassengerHomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_routes -> {
                    startActivity(Intent(this, RoutesActivity::class.java))
                    finish()
                    true
                }
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
}
