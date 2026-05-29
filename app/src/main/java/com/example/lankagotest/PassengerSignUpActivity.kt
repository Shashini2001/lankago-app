package com.example.lankagotest

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lankagotest.ui.theme.PassengerWelcomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class PassengerSignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passenger_sign_up)

        // Initialize Firebase Engines
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // View Extractions
        val btnBackToSignIn = findViewById<LinearLayout>(R.id.btnBackToSignIn)
        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPhoneNumber = findViewById<EditText>(R.id.etPhoneNumber)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnSubmitSignUp = findViewById<ImageButton>(R.id.btnSubmitSignUp)

        // 1. Back To Sign In Navigation
        btnBackToSignIn.setOnClickListener {
            val intent = Intent(this, PassengerSignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 2. Form Submission Execution Block
        btnSubmitSignUp.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val phone = etPhoneNumber.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // input validations
            if (username.isEmpty()) {
                etUsername.error = "Username is required"
                return@setOnClickListener
            }
            if (phone.isEmpty()) {
                etPhoneNumber.error = "Phone number required"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = "Password required"
                return@setOnClickListener
            }

            // Critical Design Requirement: Explicit Minimum 8 Character Constraint Check
            if (password.length < 8) {
                etPassword.error = "Password must be at least 8 characters long"
                return@setOnClickListener
            }

            // Create a Firebase-friendly email behind the scenes using phone number
            val firebaseEmail = "$phone@passenger.lankago.app"

            // Authentication Engine Core Execution
            auth.createUserWithEmailAndPassword(firebaseEmail, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid

                        if (userId != null) {
                            // Construct structural data payload dictionary to preserve space schema mapping
                            val userMap = HashMap<String, Any>()
                            userMap["username"] = username
                            userMap["phone"] = phone
                            userMap["email"] = firebaseEmail
                            userMap["userRole"] = "Passenger" // Pre-assign core functional classification route

                            // Commit transaction payload node mapping directly to Realtime structural database trees
                            database.child("Users").child(userId).setValue(userMap)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()

                                    // Route explicitly to Verification Activity to verify phone number
                                    val intent = Intent(this, VerificationActivity::class.java)
                                    intent.putExtra("PHONE_NUMBER", phone)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Database entry sync failure: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, "Sign up initialization failure: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}
