package com.example.lankagotest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class LocationSelectionActivity : AppCompatActivity() {

    private val cities = arrayOf(
        "Ambalangoda", "Ampara", "Anuradhapura", "Badulla", "Batticaloa",
        "Colombo", "Dambulla", "Galle", "Hambantota", "Jaffna",
        "Kandy", "Kurunegala", "Matara", "Nuwara Eliya", "Ratnapura",
        "Trincomalee", "Vavuniya"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_selection)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val lvLocations = findViewById<ListView>(R.id.lvLocations)

        btnBack.setOnClickListener { finish() }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, cities)
        lvLocations.adapter = adapter

        lvLocations.setOnItemClickListener { _, _, position, _ ->
            val selectedCity = cities[position]
            val resultIntent = Intent()
            resultIntent.putExtra("SELECTED_CITY", selectedCity)
            resultIntent.putExtra("IS_FROM", intent.getBooleanExtra("IS_FROM", true))
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}
