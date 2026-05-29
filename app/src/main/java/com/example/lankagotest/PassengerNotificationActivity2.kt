package com.example.lankagotest

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class PassengerNotificationActivity2 : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var rvNotifications: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private val notificationList = mutableListOf<AppNotification>()
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passenger_notification2)

        database = FirebaseDatabase.getInstance().reference
        drawerLayout = findViewById(R.id.drawerLayout)
        rvNotifications = findViewById(R.id.rvNotifications)

        val btnMenuDrawer = findViewById<ImageButton>(R.id.btnMenuDrawer)
        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        // 1. Setup RecyclerView
        rvNotifications.layoutManager = LinearLayoutManager(this)
        adapter = NotificationAdapter(notificationList)
        rvNotifications.adapter = adapter

        // 2. Fetch Notifications from Firebase
        fetchNotifications()

        // 3. Top Menu Button
        btnMenuDrawer.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Handle Drawer Item Clicks
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, PassengerHomeActivity::class.java))
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, PassengerProfileActivity::class.java))
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

        // 4. Bottom Navigation Logic
        bottomNav.selectedItemId = R.id.nav_notifications // Ensure the bell icon is highlighted!

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
                R.id.nav_notifications -> true // Already here
                R.id.nav_profile -> {
                    startActivity(Intent(this, PassengerProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchNotifications() {
        database.child("Notifications").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                notificationList.clear()

                for (notifSnapshot in snapshot.children) {
                    val message = notifSnapshot.child("message").getValue(String::class.java) ?: ""
                    if (message.isNotEmpty()) {
                        notificationList.add(AppNotification(message))
                    }
                }

                // Refresh the list UI
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@PassengerNotificationActivity2, "Failed to load notifications", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
