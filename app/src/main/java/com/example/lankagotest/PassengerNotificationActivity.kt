package com.example.lankagotest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class PassengerNotificationActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    // 1. Setup the modern Android Permission Launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            savePermissionToFirebase("Granted")
        } else {
            savePermissionToFirebase("Denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passenger_notification)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val btnAllow = findViewById<TextView>(R.id.btnAllow)
        val btnDontAllow = findViewById<TextView>(R.id.btnDontAllow)

        btnAllow.setOnClickListener {
            askForNotificationPermission()
        }

        btnDontAllow.setOnClickListener {
            savePermissionToFirebase("Denied")
        }
    }

    // 2. Ask the system for permission
    private fun askForNotificationPermission() {
        // Android 13+ requires explicit permission for notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // Already granted
                savePermissionToFirebase("Granted")
            } else {
                // Launch the system pop-up
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // For older Android versions, permission is granted automatically on install
            savePermissionToFirebase("Granted")
        }
    }

    // 3. Save to Firebase Database
    private fun savePermissionToFirebase(status: String) {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid

            // 1. Save the "Granted" or "Denied" status
            database.child("Users").child(userId).child("notificationsAllowed").setValue(status)
                .addOnSuccessListener {
                    // 2. IF GRANTED, GET THE UNIQUE DEVICE TOKEN
                    if (status == "Granted") {
                        getAndSaveFCMToken(userId)
                    } else {
                        Toast.makeText(this, "Notifications Denied", Toast.LENGTH_SHORT).show()
                        navigateToNextScreen()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save settings.", Toast.LENGTH_SHORT).show()
                    navigateToNextScreen()
                }
        } else {
            navigateToNextScreen()
        }
    }

    // 4. Get the FCM Token
    private fun getAndSaveFCMToken(userId: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Get new FCM registration token
                val token = task.result

                // Save this token to the database!
                database.child("Users").child(userId).child("fcmToken").setValue(token)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Notifications Activated!", Toast.LENGTH_SHORT).show()
                        navigateToNextScreen()
                    }
                    .addOnFailureListener {
                        navigateToNextScreen()
                    }
            } else {
                // If it fails to get token, just move to the next screen anyway
                navigateToNextScreen()
            }
        }
    }

    private fun navigateToNextScreen() {
        // Route to the Passenger Welcome Screen
        val intent = Intent(this, com.example.lankagotest.ui.theme.PassengerWelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
