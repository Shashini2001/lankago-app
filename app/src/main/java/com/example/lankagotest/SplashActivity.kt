package com.example.lankagotest

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ensure this matches your exact XML file name
        setContentView(R.layout.activity_splash)

        // Add a 2.5 second delay (2500 milliseconds) to show the logo
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserStatus()
        }, 2500)
    }

    private fun checkUserStatus() {
        // Access SharedPreferences to check local phone storage
        val sharedPreferences = getSharedPreferences("LankaGoPrefs", MODE_PRIVATE)

        // Check the "isFirstTime" variable. If it doesn't exist, it defaults to 'true'.
        val isFirstTime = sharedPreferences.getBoolean("isFirstTime", true)

        if (isFirstTime) {
            // --- IT IS THE USER'S VERY FIRST TIME ---
            val intent = Intent(this, IntroActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // --- IT IS A RETURNING USER ---
            checkUserRoleAndNavigate()
        }
    }

    private fun checkUserRoleAndNavigate() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val database = FirebaseDatabase.getInstance().reference
            database.child("Users").child(currentUser.uid).child("userRole").get()
                .addOnSuccessListener { snapshot ->
                    val role = snapshot.value?.toString()
                    val intent = when (role) {
                        "Driver" -> Intent(this, DriverHomeActivity::class.java)
                        "Passenger" -> Intent(this, PassengerHomeActivity::class.java)
                        else -> Intent(this, RoleSelectionActivity::class.java)
                    }
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    // Fallback to Role Selection if database fails
                    startActivity(Intent(this, RoleSelectionActivity::class.java))
                    finish()
                }
        } else {
            // Not logged in, go to Role Selection or Welcome
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }
    }
}
