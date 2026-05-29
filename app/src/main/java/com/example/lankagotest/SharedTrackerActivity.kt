package com.example.lankagotest

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.database.*

class SharedTrackerActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var database: DatabaseReference
    private var tripId: String = ""
    private var driverMarker: Marker? = null

    // UI elements
    private lateinit var tvStatusHeader: TextView
    private lateinit var tvPickupAddress: TextView
    private lateinit var tvDropoffAddress: TextView
    private lateinit var tvDriverName: TextView
    private lateinit var tvVehicleDetails: TextView
    private lateinit var tvDriverRating: TextView
    private lateinit var imgDriverAvatar: ShapeableImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shared_tracker)

        database = FirebaseDatabase.getInstance().reference

        // Extract deep-link payload or intent parameter bundle references
        tripId = intent.getStringExtra("TRIP_ID") ?: ""
        handleDeepLinkIntent()

        // Bind view pointers
        tvStatusHeader = findViewById(R.id.tvStatusHeader)
        tvPickupAddress = findViewById(R.id.tvPickupAddress)
        tvDropoffAddress = findViewById(R.id.tvDropoffAddress)
        tvDriverName = findViewById(R.id.tvDriverName)
        tvVehicleDetails = findViewById(R.id.tvVehicleDetails)
        tvDriverRating = findViewById(R.id.tvDriverRating)
        imgDriverAvatar = findViewById(R.id.imgDriverAvatar)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.sharedMap) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun handleDeepLinkIntent() {
        val data = intent.data
        if (data != null && data.isHierarchical) {
            val uriTripParam = data.getQueryParameter("trip")
            if (!uriTripParam.isNullOrEmpty()) {
                tripId = uriTripParam
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Light mode map - no custom styling applied by default
        // mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_dark_style))

        if (tripId.isNotEmpty()) {
            fetchSharedTripMetadata(tripId)
        } else {
            Toast.makeText(this, "No active tracking context found.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchSharedTripMetadata(id: String) {
        database.child("SharedTrips").child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return

                val passenger = snapshot.child("passengerName").value?.toString() ?: "Passenger"
                val pickup = snapshot.child("pickupLocation").value?.toString() ?: "Current Location"
                val dropoff = snapshot.child("dropoffLocation").value?.toString() ?: "Destination"
                val driverId = snapshot.child("driverId").value?.toString() ?: ""

                // Populate trip card data strings
                tvStatusHeader.text = "$passenger is on their way"
                tvPickupAddress.text = pickup
                tvDropoffAddress.text = dropoff

                if (driverId.isNotEmpty()) {
                    streamDriverLiveLocation(driverId)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun streamDriverLiveLocation(driverId: String) {
        database.child("Drivers").child(driverId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return

                // Extract vehicle data parameters
                val name = snapshot.child("name").value?.toString() ?: ""
                val model = snapshot.child("busType").value?.toString() ?: ""
                val plate = snapshot.child("plateNumber").value?.toString() ?: ""
                val rating = snapshot.child("rating").value?.toString() ?: "5.0"
                val avatarUrl = snapshot.child("profileImageUrl").value?.toString() ?: ""
                val lat = snapshot.child("latitude").getValue(Double::class.java)
                val lng = snapshot.child("longitude").getValue(Double::class.java)

                // Sync view details
                tvDriverName.text = name
                tvVehicleDetails.text = "$model • $plate"
                tvDriverRating.text = "★ $rating"

                if (avatarUrl.isNotEmpty()) {
                    Glide.with(this@SharedTrackerActivity).load(avatarUrl).into(imgDriverAvatar)
                }

                // Render shifting coordinate nodes on map viewport frame updates
                if (lat != null && lng != null) {
                    val driverPos = LatLng(lat, lng)

                    if (driverMarker == null) {
                        driverMarker = mMap.addMarker(
                            MarkerOptions()
                                .position(driverPos)
                                .title(name)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        )
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(driverPos, 15f))
                    } else {
                        driverMarker?.position = driverPos
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}