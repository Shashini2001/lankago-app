package com.example.lankagotest

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RoleSelectionActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_selection)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val btnDriver = findViewById<LinearLayout>(R.id.btnSelectDriver)
        val btnPassenger = findViewById<LinearLayout>(R.id.btnSelectPassenger)

        btnDriver.setOnClickListener {
            saveRoleAndNavigate("Driver")
        }

        btnPassenger.setOnClickListener {
            saveRoleAndNavigate("Passenger")
        }
    }

    private fun saveRoleAndNavigate(role: String) {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid

            // Save the role to Firebase
            database.child("Users").child(userId).child("userRole").setValue(role)
                .addOnSuccessListener {
                    navigateBasedOnRole(role)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save role: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // If they haven't logged in yet, you might want to save this to SharedPreferences instead
            // For now, we will just navigate them to the next screen.
            navigateBasedOnRole(role)
        }
    }

    private fun navigateBasedOnRole(role: String) {
        val intent = if (role == "Driver") {
            // Flow: Driver Notification (Permission) -> Welcome -> Login -> Register -> Verification -> Home
            Intent(this, DriverNotificationActivity::class.java)
        } else {
            // Flow: Passenger Notification (Permission) -> Welcome -> Sign In -> Sign Up -> Verification -> Home
            Intent(this, PassengerNotificationActivity::class.java)
        }

        startActivity(intent)
        finish() // Call finish so they don't accidentally press 'back' into this screen
    }
}
