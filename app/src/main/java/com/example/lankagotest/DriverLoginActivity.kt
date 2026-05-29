package com.example.lankagotest

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class DriverLoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_login)

        auth = FirebaseAuth.getInstance()

        val etDriverId = findViewById<EditText>(R.id.etDriverId)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnSignIn = findViewById<ImageButton>(R.id.btnSignIn)
        val tvCreateAccount = findViewById<TextView>(R.id.tvCreateAccount)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)

        // 1. Sign In Button Logic
        btnSignIn.setOnClickListener {
            val driverId = etDriverId.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (driverId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both ID and Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // The Trick: Convert "D-123" into "D-123@driver.lankago.app" so Firebase accepts it as an email!
            val firebaseEmail = "$driverId@driver.lankago.app"

            // Authenticate with Firebase
            auth.signInWithEmailAndPassword(firebaseEmail, password)
                .addOnSuccessListener {
                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

                    // Go to the Driver Dashboard we built earlier!
                    val intent = Intent(this, DriverHomeActivity::class.java)
                    startActivity(intent)
                    finish() // Prevent going back to login screen
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Invalid ID or Password", Toast.LENGTH_SHORT).show()
                }
        }

        // 2. Go to Registration Page
        tvCreateAccount.setOnClickListener {
            val intent = Intent(this, DriverRegisterActivity::class.java)
            startActivity(intent)
        }

        // 3. Forgot Password
        tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Contacting admin for password reset...", Toast.LENGTH_SHORT).show()
        }
    }
}
