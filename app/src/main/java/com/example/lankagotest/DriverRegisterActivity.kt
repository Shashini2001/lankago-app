package com.example.lankagotest

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class DriverRegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_register)

        auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance().reference

        val btnBackToSignIn = findViewById<LinearLayout>(R.id.btnBackToSignIn)
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etDriverId = findViewById<EditText>(R.id.etDriverId)
        val etRoute = findViewById<EditText>(R.id.etRoute)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnSignUp = findViewById<ImageButton>(R.id.btnSignUp)

        // 1. Back Button Logic
        btnBackToSignIn.setOnClickListener {
            finish() // Closes this page and goes back to Login
        }

        // 2. Sign Up Logic
        btnSignUp.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val driverId = etDriverId.text.toString().trim()
            val route = etRoute.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validation
            if (username.isEmpty() || phone.isEmpty() || driverId.isEmpty() || route.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Generate the Firebase-compatible email using the Driver ID
            val firebaseEmail = "$driverId@driver.lankago.app"

            // Disable button to prevent multiple clicks
            btnSignUp.isEnabled = false

            // Create User in Firebase Authentication
            auth.createUserWithEmailAndPassword(firebaseEmail, password)
                .addOnSuccessListener { authResult ->
                    val userId = authResult.user?.uid

                    if (userId != null) {
                        // Create a map of the driver's extra details
                        val driverData = mapOf(
                            "username" to username,
                            "phone" to phone,
                            "driverId" to driverId,
                            "routeNumber" to route,
                            "email" to firebaseEmail,
                            "role" to "Driver"
                        )

                        // Save extra details to Realtime Database
                        database.child("Users").child(userId).setValue(driverData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_LONG).show()

                                // Send them to the Driver Verification Screen
                                val intent = Intent(this, DriverVerificationActivity::class.java)
                                intent.putExtra("PHONE_NUMBER", phone)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to save driver details", Toast.LENGTH_SHORT).show()
                                btnSignUp.isEnabled = true
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Registration Failed: ${exception.message}", Toast.LENGTH_LONG).show()
                    btnSignUp.isEnabled = true
                }
        }
    }
}
