package com.example.lankagotest

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class AboutUsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        // Close the About Us screen and go back to the previous screen
        btnBack.setOnClickListener {
            finish()
        }
    }
}
