package com.example.lankagotest

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class DriverWelcomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_welcome)

        val btnGetStart = findViewById<AppCompatButton>(R.id.btnGetStart)

        btnGetStart.setOnClickListener {
            // Navigate to the Driver Login Screen
            val intent = Intent(this, DriverLoginActivity::class.java)
            startActivity(intent)
            finish() // Prevents the user from going back to the welcome screen
        }
    }
}