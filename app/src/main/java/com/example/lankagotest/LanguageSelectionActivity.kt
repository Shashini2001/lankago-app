package com.example.lankagotest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LanguageSelectionActivity : AppCompatActivity() {

    // Firebase variables
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language) // Make sure this matches your XML filename

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Link the buttons from your XML
        val btnEnglish = findViewById<Button>(R.id.btnEnglish)
        val btnSinhala = findViewById<Button>(R.id.btnSinhala)
        val btnTamil = findViewById<Button>(R.id.btnTamil)

        // Set Click Listeners
        btnEnglish.setOnClickListener {
            saveLanguageToFirebase("en") // 'en' is standard code for English
        }

        btnSinhala.setOnClickListener {
            saveLanguageToFirebase("si") // 'si' is standard code for Sinhala
        }

        btnTamil.setOnClickListener {
            saveLanguageToFirebase("ta") // 'ta' is standard code for Tamil
        }
    }

    private fun saveLanguageToFirebase(languageCode: String) {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid

            // Save the language code to the database under this user's profile
            database.child("Users").child(userId).child("languagePreference").setValue(languageCode)
                .addOnSuccessListener {
                    Toast.makeText(this, "Language updated successfully", Toast.LENGTH_SHORT).show()
                    navigateToNextScreen()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save language: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // If the user isn't logged in yet, you might just move them to the next screen anyway
            // and save the language locally (see my follow-up question below).
            navigateToNextScreen()
        }
    }

    private fun navigateToNextScreen() {
        // Go to your login or main app screen
        val intent = Intent(this, LocationPermissionActivity::class.java) // Change MainActivity to wherever they go next
        startActivity(intent)
        finish() // Prevents the user from hitting 'back' to return to language selection
    }
}