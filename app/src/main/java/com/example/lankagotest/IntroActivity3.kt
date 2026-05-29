package com.example.lankagotest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class IntroActivity3 : ComponentActivity() {

    private var currentPage = 0

    // 1. The Images for all 3 pages - using existing drawables
    private val images = intArrayOf(
        R.drawable.busintro,     // Was intro_image_1
        R.drawable.busstopntro,  // Was intro_image_2
        R.drawable.bushareintro   // Was intro_image_3
    )

    // 2. The Top Black Text
    private val topTexts = arrayOf(
        "Welcome to",
        "Quick and Reliable",
        "Location"
    )

    // 3. The Large Blue Text
    private val blueTexts = arrayOf(
        "Lanka Go!",
        "Bus Tracking",
        "Sharing"
    )

    // 4. The Bottom Description Text
    private val descriptions = arrayOf(
        "Sri Lanka's first\nreal-time bus tracking and booking assistant.\n\nGetting your day-to-day bus travel\nupdates is now just a matter of few\nclicks!",
        "Use our real-time tracking feature to see exactly where your bus is and when it will arrive at your stop.",
        "Get real-time bus updates and share\nlive locations with just a few clicks.\nMake your day-to-day bus travel\neasier, faster, and smarter."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val imgIntro = findViewById<ImageView>(R.id.imgIntro)
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val tvAppName = findViewById<TextView>(R.id.tvAppName)
        val tvDescTitle = findViewById<TextView>(R.id.tvDescTitle)
        val tvSubDesc = findViewById<TextView>(R.id.tvSubDesc)

        val btnNext = findViewById<ImageButton>(R.id.btnNext)
        val btnSkip = findViewById<TextView>(R.id.btnSkip)

        // Clear the unused sub-description field
        tvSubDesc.text = ""

        // Load the first page immediately
        updateUI(imgIntro, tvWelcome, tvAppName, tvDescTitle)

        // Handle Next Button Click
        btnNext.setOnClickListener {
            if (currentPage < 2) {
                currentPage++
                updateUI(imgIntro, tvWelcome, tvAppName, tvDescTitle)
            } else {
                // On the last page, run the Firebase check!
                completeIntroAndNavigate()
            }
        }

        // Handle Skip Button Click
        btnSkip.setOnClickListener {
            completeIntroAndNavigate()
        }
    }

    private fun updateUI(img: ImageView, topText: TextView, blueText: TextView, desc: TextView) {
        img.setImageResource(images[currentPage])
        topText.text = topTexts[currentPage]
        blueText.text = blueTexts[currentPage]
        desc.text = descriptions[currentPage]
    }

    // 5. THE FIREBASE ROUTING LOGIC
    private fun completeIntroAndNavigate() {
        // Mark intro as finished so it doesn't show again on next app launch
        val sharedPreferences = getSharedPreferences("LankaGoPrefs", MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("isFirstTime", false).apply()

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // User is logged in, check their role in the database
            val database = FirebaseDatabase.getInstance().reference
            val userId = currentUser.uid

            database.child("Users").child(userId).child("userRole").get()
                .addOnSuccessListener { snapshot ->
                    val role = snapshot.value as? String

                    when (role) {
                        "Driver" -> {
                            startActivity(Intent(this, DriverHomeActivity::class.java))
                        }
                        "Passenger" -> {
                            startActivity(Intent(this, PassengerHomeActivity::class.java))
                        }
                        else -> {
                            // If role is missing, send them to select it
                            startActivity(Intent(this, RoleSelectionActivity::class.java))
                        }
                    }
                    finish()
                }
                .addOnFailureListener {
                    // Database read failed, fallback to role selection
                    Log.e("IntroActivity3", "Failed to read user role", it)
                    startActivity(Intent(this, RoleSelectionActivity::class.java))
                    finish()
                }
        } else {
            // User is NOT logged in, send them to the beginning of the flow
            val intent = Intent(this, RoleSelectionActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
