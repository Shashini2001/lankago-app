package com.example.lankagotest.ui.theme

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.widget.AppCompatButton
import com.example.lankagotest.PassengerSignInActivity
import com.example.lankagotest.R

class PassengerWelcomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passenger_welcome)

        // Find the button
        val btnGetStart = findViewById<AppCompatButton>(R.id.btnGetStart)

        // Set click listener
        btnGetStart.setOnClickListener {
            navigateToPassengerSignIn()
        }
    }

    private fun navigateToPassengerSignIn() {
        val intent = Intent(this, PassengerSignInActivity::class.java)
        startActivity(intent)

        // Optional: Call finish() if you don't want the user to be able
        // to press the back button and return to this welcome screen.
        finish()
    }
}
