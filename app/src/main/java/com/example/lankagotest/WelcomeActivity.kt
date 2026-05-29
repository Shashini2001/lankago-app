package com.example.lankagotest

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.firebase.auth.FirebaseAuth

class WelcomeActivity : ComponentActivity() {

    // Firebase Auth variable
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Link the root layout so we can detect touches anywhere on the screen
        val welcomeContainer = findViewById<ConstraintLayout>(R.id.welcomeContainer)

        // Set up the touch listener
        welcomeContainer.setOnClickListener {
            navigateToRoleSelection()
        }
    }

    override fun onStart() {
        super.onStart()
        // Check if user is already signed in from a previous session
        val currentUser = auth.currentUser
        
        if (currentUser != null) {
            // User is already logged in! Skip the welcome screen.
            // Note: Change 'HomeActivity' to your actual main app screen
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish() // Prevent going back to welcome screen
        }
    }

    private fun navigateToRoleSelection() {
        // User touched the screen, go to Driver/Passenger selection
        // Note: Ensure you have created a 'RoleSelectionActivity'
        val intent = Intent(this, RoleSelectionActivity::class.java)
        startActivity(intent)
        // We DO NOT call finish() here, so the user can press 'Back'
        // to return to the welcome screen if they want to.
    }
}
