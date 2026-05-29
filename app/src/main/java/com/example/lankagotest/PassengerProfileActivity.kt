package com.example.lankagotest

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class PassengerProfileActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var imgProfilePicture: ShapeableImageView
    private lateinit var bottomSheetDialog: BottomSheetDialog

    // This launcher opens the phone's gallery and returns the selected image Uri
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            // Instantly display the image in the UI locally
            imgProfilePicture.setImageURI(uri)
            Toast.makeText(this, "Profile picture updated locally!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passenger_profile)

        drawerLayout = findViewById(R.id.drawerLayout)
        val btnMenuDrawer = findViewById<ImageButton>(R.id.btnMenuDrawer)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)

        imgProfilePicture = findViewById(R.id.imgProfilePicture)
        val btnEditPicture = findViewById<AppCompatButton>(R.id.btnEditPicture)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        // 1. Open Navigation Drawer
        btnMenuDrawer.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Header click handling
        val headerView = navigationView.getHeaderView(0)
        headerView.findViewById<LinearLayout>(R.id.nav_header_review_area).setOnClickListener {
            startActivity(Intent(this, ReviewActivity::class.java))
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        // 2. Handle Drawer Item Clicks
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, PassengerHomeActivity::class.java))
                }
                R.id.nav_profile -> {
                    // Already here
                }
                R.id.nav_review -> {
                    startActivity(Intent(this, ReviewActivity::class.java))
                }
                R.id.nav_settings -> Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
                R.id.nav_about -> startActivity(Intent(this, AboutUsActivity::class.java))
                R.id.nav_qr_payment -> startActivity(Intent(this, QrScannerActivity::class.java))
                R.id.nav_seat_booking -> startActivity(Intent(this, SeatBookingActivity::class.java))
                R.id.nav_languages -> startActivity(Intent(this, LanguageSelectionActivity::class.java))
                R.id.nav_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, PassengerSignInActivity::class.java))
                    finishAffinity()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // 3. Open Bottom Sheet to change picture
        btnEditPicture.setOnClickListener {
            showProfilePictureDialog()
        }

        // 2. Navigation setup (Highlight Profile!)
        bottomNav.selectedItemId = R.id.nav_profile
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, PassengerHomeActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_routes -> {
                    startActivity(Intent(this, YourselectRoutesActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_track -> {
                    startActivity(Intent(this, LiveTrackingActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_notifications -> {
                    startActivity(Intent(this, PassengerNotificationActivity2::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.nav_profile -> true // Already here
                else -> false
            }
        }
    }

    private fun showProfilePictureDialog() {
        // Initialize the Bottom Sheet (using the custom theme for transparent corners)
        bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.dialog_profile_picture, null)
        bottomSheetDialog.setContentView(view)

        val btnClose = view.findViewById<ImageButton>(R.id.btnClose)
        val btnDelete = view.findViewById<ImageButton>(R.id.btnDeletePicture)
        val btnGallery = view.findViewById<LinearLayout>(R.id.btnGallery)
        val btnCamera = view.findViewById<LinearLayout>(R.id.btnCamera)
        val btnAvatar = view.findViewById<LinearLayout>(R.id.btnAvatar)

        // CLOSE BUTTON
        btnClose.setOnClickListener { bottomSheetDialog.dismiss() }

        // OPEN GALLERY BUTTON
        btnGallery.setOnClickListener {
            pickImageLauncher.launch("image/*") // Request any type of image
            bottomSheetDialog.dismiss()
        }

        // DELETE BUTTON
        btnDelete.setOnClickListener {
            // Reset to the default icon (using 'man' as it's the default in XML)
            imgProfilePicture.setImageResource(R.drawable.man)
            Toast.makeText(this, "Picture removed locally", Toast.LENGTH_SHORT).show()
            bottomSheetDialog.dismiss()
        }

        // CAMERA BUTTON (Placeholder)
        btnCamera.setOnClickListener {
            Toast.makeText(this, "Camera not setup. Use Gallery instead!", Toast.LENGTH_SHORT).show()
        }

        // AVATAR BUTTON (Placeholder)
        btnAvatar.setOnClickListener {
            Toast.makeText(this, "Select from local avatars feature coming soon!", Toast.LENGTH_SHORT).show()
        }

        bottomSheetDialog.show()
    }
}
