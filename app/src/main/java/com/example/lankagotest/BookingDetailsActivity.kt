package com.example.lankagotest

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.google.firebase.database.*

class BookingDetailsActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private val bookingId = "booking_778899" // We will get this dynamically later

    // UI Variables
    private lateinit var tvTicketFrom: TextView
    private lateinit var tvTicketTo: TextView
    private lateinit var tvTicketDate: TextView
    private lateinit var tvBusName: TextView
    private lateinit var tvPrice: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvBusNumber: TextView
    private lateinit var tvSeatNumber: TextView
    private lateinit var tvPassenger: TextView
    private lateinit var tvStoppingPoint: TextView
    private lateinit var imgQrCode: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking_details)

        database = FirebaseDatabase.getInstance().reference

        // Get the dynamic booking ID from Intent
        val dynamicBookingId = intent.getStringExtra("BOOKING_ID") ?: "booking_778899"

        // Bind UI Elements
        tvTicketFrom = findViewById(R.id.tvTicketFrom)
        tvTicketTo = findViewById(R.id.tvTicketTo)
        tvTicketDate = findViewById(R.id.tvTicketDate)
        tvBusName = findViewById(R.id.tvBusName)
        tvPrice = findViewById(R.id.tvPrice)
        tvTime = findViewById(R.id.tvTime)
        tvBusNumber = findViewById(R.id.tvBusNumber)
        tvSeatNumber = findViewById(R.id.tvSeatNumber)
        tvPassenger = findViewById(R.id.tvPassenger)
        tvStoppingPoint = findViewById(R.id.tvStoppingPoint)
        imgQrCode = findViewById(R.id.imgQrCode)

        // Load the data from Firebase
        fetchBookingDetails(dynamicBookingId)
    }

    private fun fetchBookingDetails(id: String) {
        database.child("Bookings").child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // 1. Set the text data
                    tvTicketFrom.text = snapshot.child("from").getValue(String::class.java)
                    tvTicketTo.text = snapshot.child("to").getValue(String::class.java)
                    tvTicketDate.text = "Booking date : ${snapshot.child("date").getValue(String::class.java)}"
                    tvBusName.text = snapshot.child("busName").getValue(String::class.java)
                    tvPrice.text = snapshot.child("price").getValue(String::class.java)
                    tvTime.text = snapshot.child("time").getValue(String::class.java)
                    tvBusNumber.text = snapshot.child("busNumber").getValue(String::class.java)
                    tvSeatNumber.text = snapshot.child("seatNumber").getValue(String::class.java)
                    tvPassenger.text = snapshot.child("passengerName").getValue(String::class.java)
                    tvStoppingPoint.text = snapshot.child("stoppingPoint").getValue(String::class.java)

                    // 2. Generate the QR Code
                    val qrDataText = snapshot.child("qrData").getValue(String::class.java) ?: "INVALID"
                    generateQRCode(qrDataText)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@BookingDetailsActivity, "Failed to load ticket", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun generateQRCode(content: String) {
        try {
            val barcodeEncoder = BarcodeEncoder()
            // Convert the text into a 400x400 QR Code Bitmap image
            val bitmap: Bitmap = barcodeEncoder.encodeBitmap(content, BarcodeFormat.QR_CODE, 400, 400)

            // Set the generated image into the ImageView
            imgQrCode.setImageBitmap(bitmap)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Could not generate QR code", Toast.LENGTH_SHORT).show()
        }
    }
}
