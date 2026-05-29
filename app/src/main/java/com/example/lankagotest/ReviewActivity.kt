package com.example.lankagotest

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class ReviewActivity : AppCompatActivity() {

    private lateinit var ratingBar: RatingBar
    private lateinit var btnUploadImage: ImageButton
    private lateinit var btnSubmitReport: AppCompatButton

    private var selectedImageUri: Uri? = null
    private val busId = "NA 8062" // This would normally be passed via Intent from the active trip

    // Launcher to pick an image from the phone gallery
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            // Provide visual feedback that the image was attached
            btnUploadImage.setColorFilter(android.graphics.Color.parseColor("#4CAF50")) // Turn icon green
            Toast.makeText(this, "Image attached!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        ratingBar = findViewById(R.id.ratingBar)
        btnUploadImage = findViewById(R.id.btnUploadImage)
        btnSubmitReport = findViewById(R.id.btnSubmitReport)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        // 1. Open Gallery when image icon is clicked
        btnUploadImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // 2. Handle Submit Button
        btnSubmitReport.setOnClickListener {
            val userRating = ratingBar.rating

            if (userRating == 0.0f) {
                Toast.makeText(this, "Please select a star rating", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Disable button to prevent double-clicks
            btnSubmitReport.isEnabled = false
            btnSubmitReport.text = "Submitting..."

            if (selectedImageUri != null) {
                uploadImageAndSaveReview(userRating)
            } else {
                saveReviewToDatabase(userRating, "") // Save without image
            }
        }

        // 3. Bottom Navigation Setup
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, PassengerHomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_routes -> {
                    startActivity(Intent(this, RoutesActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_track -> {
                    startActivity(Intent(this, LiveTrackingActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_notifications -> {
                    startActivity(Intent(this, PassengerNotificationActivity2::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, PassengerProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun uploadImageAndSaveReview(rating: Float) {
        val storageRef = FirebaseStorage.getInstance().reference
        val fileName = "review_images/${UUID.randomUUID()}.jpg"
        val imageRef = storageRef.child(fileName)

        imageRef.putFile(selectedImageUri!!).addOnSuccessListener {
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                saveReviewToDatabase(rating, uri.toString())
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Image upload failed.", Toast.LENGTH_SHORT).show()
            btnSubmitReport.isEnabled = true
            btnSubmitReport.text = "Submit Report"
        }
    }

    private fun saveReviewToDatabase(rating: Float, imageUrl: String) {
        val database = FirebaseDatabase.getInstance().reference
        val reviewId = "review_" + UUID.randomUUID().toString().substring(0, 8)

        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentTime = formatter.format(Date())

        val reviewData = mapOf(
            "busId" to busId,
            "rating" to rating,
            "reportImageUrl" to imageUrl,
            "timestamp" to currentTime
        )

        database.child("Reviews").child(reviewId).setValue(reviewData)
            .addOnSuccessListener {
                Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_LONG).show()
                finish() // Close the review screen and go back
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to submit review.", Toast.LENGTH_SHORT).show()
                btnSubmitReport.isEnabled = true
                btnSubmitReport.text = "Submit Report"
            }
    }
}
