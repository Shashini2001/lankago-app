package com.example.lankagotest

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.ByteArrayOutputStream
import java.io.InputStream

class DriverProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences

    // UI Elements
    private lateinit var imgProfilePicture: ShapeableImageView
    private lateinit var tvDriverName: TextView
    private lateinit var tvBusRegNo: TextView
    private lateinit var tvRouteNo: TextView
    private lateinit var tvPhoneNo: TextView
    private lateinit var drawerLayout: DrawerLayout

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            imgProfilePicture.setImageURI(uri)
            uploadImageAsBase64(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Check and Apply Dark Mode Preference immediately when the page loads
        sharedPreferences = getSharedPreferences("LankaGoSettings", Context.MODE_PRIVATE)
        val isDarkModeOn = sharedPreferences.getBoolean("DARK_MODE", false)
        if (isDarkModeOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        setContentView(R.layout.activity_driver_profile)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        drawerLayout = findViewById(R.id.drawerLayout)
        imgProfilePicture = findViewById(R.id.imgProfilePicture)
        tvDriverName = findViewById(R.id.tvDriverName)
        tvBusRegNo = findViewById(R.id.tvBusRegNo)
        tvRouteNo = findViewById(R.id.tvRouteNo)
        tvPhoneNo = findViewById(R.id.tvPhoneNo)
        
        val btnMenuDrawer = findViewById<ImageButton>(R.id.btnMenuDrawer)
        val btnEditPicture = findViewById<AppCompatButton>(R.id.btnEditPicture)
        val btnTopEdit = findViewById<ImageButton>(R.id.btnTopEdit)
        val driverBottomNav = findViewById<BottomNavigationView>(R.id.driverBottomNav)
        val navigationDrawerMenu = findViewById<NavigationView>(R.id.navigationDrawerMenu)

        fetchDriverData()

        // Click Edit to show profile picture dialog
        btnEditPicture.setOnClickListener {
            showProfilePictureDialog()
        }
        
        btnTopEdit.setOnClickListener {
            showProfilePictureDialog()
        }

        // Open Drawer
        btnMenuDrawer.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        
        // 2. Handle Drawer Menu Icon Clicks
        navigationDrawerMenu.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_driver_settings -> {
                    Toast.makeText(this, "Settings coming soon", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_driver_about -> {
                    startActivity(Intent(this, AboutUsActivity::class.java))
                }
                R.id.nav_driver_link_history -> {
                    Toast.makeText(this, "Link History coming soon", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_driver_languages -> {
                    Toast.makeText(this, "Languages coming soon", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_driver_dark_mode -> {
                    // TOGGLE DARK MODE ON/OFF
                    val editor = sharedPreferences.edit()
                    val currentlyDark = sharedPreferences.getBoolean("DARK_MODE", false)
                    
                    if (currentlyDark) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) // Switch to Light
                        editor.putBoolean("DARK_MODE", false)
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) // Switch to Dark
                        editor.putBoolean("DARK_MODE", true)
                    }
                    editor.apply()
                }
                R.id.nav_driver_sign_out -> {
                    auth.signOut()
                    startActivity(Intent(this, DriverLoginActivity::class.java))
                    finishAffinity() 
                }
            }
            
            // Close the drawer after clicking an item
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Bottom Navigation Logic
        driverBottomNav.selectedItemId = R.id.nav_driver_profile
        driverBottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_driver_home -> {
                    startActivity(Intent(this, DriverHomeActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_driver_schedule -> {
                    startActivity(Intent(this, DriverSchedulesActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_driver_trips -> {
                    startActivity(Intent(this, DriverTripsActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_driver_notifications -> {
                    startActivity(Intent(this, DriverNotificationActivity2::class.java))
                    finish()
                    true
                }
                R.id.nav_driver_profile -> true
                else -> false
            }
        }
    }

    private fun showProfilePictureDialog() {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.dialog_profile_picture, null)
        dialog.setContentView(view)

        val btnClose = view.findViewById<View>(R.id.btnClose)
        val btnDelete = view.findViewById<View>(R.id.btnDeletePicture)
        val btnCamera = view.findViewById<LinearLayout>(R.id.btnCamera)
        val btnGallery = view.findViewById<LinearLayout>(R.id.btnGallery)
        val btnAvatar = view.findViewById<LinearLayout>(R.id.btnAvatar)

        btnClose.setOnClickListener { dialog.dismiss() }

        btnDelete.setOnClickListener {
            deleteProfilePicture()
            dialog.dismiss()
        }

        btnCamera.setOnClickListener {
            Toast.makeText(this, "Camera feature coming soon!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        btnGallery.setOnClickListener {
            pickImageLauncher.launch("image/*")
            dialog.dismiss()
        }

        btnAvatar.setOnClickListener {
            Toast.makeText(this, "Avatar selection coming soon!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun deleteProfilePicture() {
        val userId = auth.currentUser?.uid ?: return
        database.child("Users").child(userId).child("profileImageBase64").removeValue()
            .addOnSuccessListener {
                imgProfilePicture.setImageResource(R.drawable.driverpng) // Reset to default
                Toast.makeText(this, "Profile picture deleted", Toast.LENGTH_SHORT).show()
            }
    }

    // --- Firebase Database Fetch & Save Code ---
    
    private fun fetchDriverData() {
        val userId = auth.currentUser?.uid ?: return

        database.child("Users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    tvDriverName.text = snapshot.child("username").getValue(String::class.java) ?: "N/A"
                    tvBusRegNo.text = snapshot.child("driverId").getValue(String::class.java) ?: "N/A" 
                    tvRouteNo.text = snapshot.child("routeNumber").getValue(String::class.java) ?: "N/A"
                    tvPhoneNo.text = snapshot.child("phone").getValue(String::class.java) ?: "N/A"

                    val base64Image = snapshot.child("profileImageBase64").getValue(String::class.java)
                    if (!base64Image.isNullOrEmpty()) {
                        try {
                            val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
                            val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                            imgProfilePicture.setImageBitmap(decodedImage)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DriverProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun uploadImageAsBase64(imageUri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        Toast.makeText(this, "Saving image...", Toast.LENGTH_SHORT).show()

        try {
            val imageStream: InputStream? = contentResolver.openInputStream(imageUri)
            val selectedImage = BitmapFactory.decodeStream(imageStream)
            val baos = ByteArrayOutputStream()
            selectedImage.compress(Bitmap.CompressFormat.JPEG, 50, baos) 
            val imageBytes = baos.toByteArray()
            val base64String = Base64.encodeToString(imageBytes, Base64.DEFAULT)

            database.child("Users").child(userId).child("profileImageBase64").setValue(base64String)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile picture updated successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save image data.", Toast.LENGTH_SHORT).show()
                }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show()
        }
    }
}
