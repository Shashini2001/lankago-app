package com.example.lankagotest

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BusResultsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bus_results)

        // Get data from previous screen
        val fromLocation = intent.getStringExtra("FROM") ?: "Colombo"
        val toLocation = intent.getStringExtra("TO") ?: "Jaffna"

        findViewById<TextView>(R.id.tvHeaderFrom).text = fromLocation
        findViewById<TextView>(R.id.tvHeaderTo).text = toLocation

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        // Setup the list
        val rvBusResults = findViewById<RecyclerView>(R.id.rvBusResults)
        rvBusResults.layoutManager = LinearLayoutManager(this)

        // Dummy Data (This will come from Firebase later!)
        val dummyData = listOf(
            TripData("1", "Annai Muthumari (Morning)", fromLocation, toLocation, "8:00 AM", "LKR 1400", 13),
            TripData("2", "Sri Murugan Travels (N1)", fromLocation, toLocation, "8:00 PM", "LKR 1200", 20)
        )

        val adapter = BusResultAdapter(dummyData) { selectedTicket ->
            // Phase 3: Go to the Seat Selection Grid!
            val intent = Intent(this, SeatSelectionActivity::class.java)
            startActivity(intent)
        }

        rvBusResults.adapter = adapter
    }
}
