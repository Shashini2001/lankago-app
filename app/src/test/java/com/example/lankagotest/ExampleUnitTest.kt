package com.example.lankagotest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ensure this matches your exact XML file name
        setContentView(R.layout.activity_splash)

        // Add a 2.5second delay (2500 milliseconds) to show the logo
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserStatus()
        }, 2500)
    }

    private fun checkUserStatus() {
        // Access SharedPreferences to check local phone storage
        val sharedPreferences = getSharedPreferences("LankaGoPrefs", Context.MODE_PRIVATE)

        // Check the "isFirstTime" variable. If it doesn't exist, it defaults to 'true'.
        val isFirstTime = sharedPreferences.getBoolean("isFirstTime", true)

        if (isFirstTime) {
            // --- IT IS THE USER'S VERY FIRST TIME ---

            // 1. Update the memory so it is no longer their first time
            sharedPreferences.edit().putBoolean("isFirstTime", false).apply()

            // 2. Send them to the Language Selection screen
            val intent = Intent(this, LanguageSelectionActivity::class.java)
            startActivity(intent)

        } else {
            // --- IT IS A RETURNING USER ---

            // Send them directly to the main app (Driver or Passenger Home)
            // IMPORTANT: Change 'HomeActivity' to the actual name of your home screen
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        // Close the Splash screen so the user can't press 'Back' and get stuck here
        finish()
    }
}

