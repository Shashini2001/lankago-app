package com.example.lankagotest

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity

class LocationPermissionActivity : ComponentActivity() {

    // Variable to track if they selected "Precise" (true) or "Approximate" (false)
    private var isPreciseSelected = true

    // The modern way to request permissions in Android
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
                navigateToHome()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
                navigateToHome()
            }
            else -> {
                // No location access granted.
                Toast.makeText(this, "Location permission is required to track buses", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_permission)

        val btnPrecise = findViewById<LinearLayout>(R.id.btnPrecise)
        val btnApproximate = findViewById<LinearLayout>(R.id.btnApproximate)

        val btnWhileUsing = findViewById<TextView>(R.id.btnWhileUsing)
        val btnOnlyThisTime = findViewById<TextView>(R.id.btnOnlyThisTime)
        val btnDontAllow = findViewById<TextView>(R.id.btnDontAllow)

        val imgPrecise = findViewById<ImageView>(R.id.imgPrecise)
        val imgApproximate = findViewById<ImageView>(R.id.imgApproximate)

        // Handle visual toggle between Precise and Approximate
        btnPrecise.setOnClickListener {
            isPreciseSelected = true
            imgPrecise.alpha = 1.0f
            imgApproximate.alpha = 0.5f
        }

        btnApproximate.setOnClickListener {
            isPreciseSelected = false
            imgPrecise.alpha = 0.5f
            imgApproximate.alpha = 1.0f
        }

        // Handle "While using the app"
        btnWhileUsing.setOnClickListener {
            requestActualPermissions()
        }

        // Handle "Only this time"
        btnOnlyThisTime.setOnClickListener {
            requestActualPermissions()
        }

        // Handle "Don't allow"
        btnDontAllow.setOnClickListener {
            Toast.makeText(this, "You must allow location to use Lanka Go.", Toast.LENGTH_SHORT).show()
            // Optionally close the app or send them back to a restricted home screen
        }
    }

    private fun requestActualPermissions() {
        // This triggers the official Android system dialog
        if (isPreciseSelected) {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        } else {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    private fun navigateToHome() {
        // Change WelcomeActivity to your actual next screen
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
