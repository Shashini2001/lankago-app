package com.example.lankagotest

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity

class IntroActivity2 : ComponentActivity() {

    private var currentPage = 0

    // 1. The Images - using existing drawables from the project
    private val images = intArrayOf(
        R.drawable.busintro,     // Was intro_image_1
        R.drawable.busstopntro,  // Was intro_image_2
        R.drawable.bushareintro   // Was intro_image_3
    )

    // 2. The Top Black Text ("Welcome to", "Quick and Reliable")
    private val topTexts = arrayOf(
        "Welcome to",
        "Quick and Reliable",
        "Ready to go?"
    )

    // 3. The Large Blue Text ("Lanka Go!", "Bus Tracking")
    private val blueTexts = arrayOf(
        "Lanka Go!",
        "Bus Tracking",
        "Start Journey"
    )

    // 4. The Bottom Description Text
    private val descriptions = arrayOf(
        "Sri Lanka's first\nreal-time bus tracking and booking assistant.\n\nGetting your day-to-day bus travel\nupdates is now just a matter of few\nclicks!",
        "Use our real-time tracking feature to see exactly where your bus is and when it will arrive at your stop.",
        "Your final description here."
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

        // Hide the sub-description view since we merged the text into one paragraph
        tvSubDesc.text = ""

        // Handle Next Button Click
        btnNext.setOnClickListener {
            if (currentPage < 2) {
                // Go to next page
                currentPage++

                // Update the UI with new image and text
                imgIntro.setImageResource(images[currentPage])
                tvWelcome.text = topTexts[currentPage]
                tvAppName.text = blueTexts[currentPage]
                tvDescTitle.text = descriptions[currentPage]
            } else {
                // On the last page, finish intro and go to the app
                completeIntroAndNavigate()
            }
        }

        // Handle Skip Button Click
        btnSkip.setOnClickListener {
            completeIntroAndNavigate()
        }
    }

    private fun completeIntroAndNavigate() {
        val sharedPreferences = getSharedPreferences("LankaGoPrefs", MODE_PRIVATE)
        sharedPreferences.edit().putBoolean("isFirstTime", false).apply()

        // Go to Language Selection
        val intent = Intent(this, LanguageSelectionActivity::class.java)
        startActivity(intent)
        finish()
    }
}
