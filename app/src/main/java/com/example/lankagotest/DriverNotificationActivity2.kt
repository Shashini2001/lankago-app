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

class DriverNotificationActivity2 : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var rvNotifications: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private val notificationList = mutableListOf<AppNotification>()
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_notification2)

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
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_driver_settings -> Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
                R.id.nav_driver_about -> startActivity(Intent(this, AboutUsActivity::class.java))
                R.id.nav_driver_link_history -> Toast.makeText(this, "Link History clicked", Toast.LENGTH_SHORT).show()
                R.id.nav_driver_languages -> Toast.makeText(this, "Languages clicked", Toast.LENGTH_SHORT).show()
                R.id.nav_driver_sign_out -> {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, DriverLoginActivity::class.java))
                    finishAffinity()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // 4. Bottom Navigation Logic
        bottomNav.selectedItemId = R.id.nav_driver_notifications

        bottomNav.setOnItemSelectedListener { item ->
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
                R.id.nav_driver_notifications -> true // Already here
                R.id.nav_driver_profile -> {
                    startActivity(Intent(this, DriverProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchNotifications() {
        database.child("DriverNotifications").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                notificationList.clear()
                for (notifSnapshot in snapshot.children) {
                    val message = notifSnapshot.child("message").getValue(String::class.java) ?: ""
                    if (message.isNotEmpty()) {
                        notificationList.add(AppNotification(message))
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DriverNotificationActivity2, "Failed to load notifications", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
