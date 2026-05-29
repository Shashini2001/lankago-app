package com.example.lankagotest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class DriverNotificationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

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
        setContentView(R.layout.activity_driver_notification)

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

    private fun askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                savePermissionToFirebase("Granted")
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            savePermissionToFirebase("Granted")
        }
    }

    private fun savePermissionToFirebase(status: String) {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            database.child("Users").child(userId).child("notificationsAllowed").setValue(status)
                .addOnSuccessListener {
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

    private fun getAndSaveFCMToken(userId: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                database.child("Users").child(userId).child("fcmToken").setValue(token)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Notifications Activated!", Toast.LENGTH_SHORT).show()
                        navigateToNextScreen()
                    }
                    .addOnFailureListener {
                        navigateToNextScreen()
                    }
            } else {
                navigateToNextScreen()
            }
        }
    }

    private fun navigateToNextScreen() {
        val intent = Intent(this, DriverWelcomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}
