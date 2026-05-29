package com.example.lankagotest

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.firebase.database.FirebaseDatabase

class DriverLocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val database = FirebaseDatabase.getInstance().reference

    // Hardcoded for now. Later you can pass this via Intent from FirebaseAuth
    private val driverId = "driver_xyz789"

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Setup what happens when we get a new GPS location
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    // Send Live GPS to Firebase!
                    val updates = mapOf(
                        "latitude" to location.latitude,
                        "longitude" to location.longitude,
                        "isActive" to true
                    )
                    database.child("Drivers").child(driverId).updateChildren(updates)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 1. Show the sticky notification so Android keeps the app alive
        startForeground(1, createNotification())

        // 2. Request GPS updates every 5 seconds
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000)
            .build()

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop tracking and tell Firebase the driver is offline
        fusedLocationClient.removeLocationUpdates(locationCallback)
        database.child("Drivers").child(driverId).child("isActive").setValue(false)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // We don't need binding for this simple tracker
    }

    private fun createNotification(): Notification {
        val channelId = "DriverLocationChannel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Live Tracking", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Lanka Go Driver")
            .setContentText("Live location sharing is active...")
            .setSmallIcon(R.drawable.ic_location_marker) // Use your location icon
            .build()
    }
}
