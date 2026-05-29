package com.example.lankagotest

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.util.UUID

class SeatSelectionActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var rvSeats: RecyclerView
    private lateinit var seatAdapter: SeatAdapter
    private val seatList = mutableListOf<Seat>()
    private var selectedSeatNumber: String? = null
    
    private val tripId = "trip_kaduwela_nittambuwa"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seat_selection)

        database = FirebaseDatabase.getInstance().reference
        rvSeats = findViewById(R.id.rvSeats)

        // Setup the grid with exactly 6 columns
        rvSeats.layoutManager = GridLayoutManager(this, 6)

        // Initialize the bus layout structure
        generateBusLayout()

        seatAdapter = SeatAdapter(seatList) { seatNumber ->
            selectedSeatNumber = seatNumber
            Toast.makeText(this, "Selected Seat: $seatNumber", Toast.LENGTH_SHORT).show()
        }
        rvSeats.adapter = seatAdapter

        // Listen for live bookings from Firebase
        fetchBookedSeats()

        // Confirm Booking Button
        val btnConfirmBooking = findViewById<AppCompatButton>(R.id.btnConfirmBooking)
        btnConfirmBooking.setOnClickListener {
            val seat = selectedSeatNumber
            if (seat != null && seat.isNotEmpty()) {
                confirmBookingAndShowSuccess(seat)
            } else {
                Toast.makeText(this, "Please select a seat first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateBusLayout() {
        seatList.clear()
        
        // Rows 1 to 12 (Left pair, Aisle, Right pair)
        var leftSeatNum = 1
        var rightSeatNum = 28
        
        for (row in 1..12) {
            seatList.add(Seat(leftSeatNum.toString()))         // Col 1
            seatList.add(Seat((leftSeatNum + 1).toString()))   // Col 2
            seatList.add(Seat(""))                             // Col 3 (Aisle)
            seatList.add(Seat(""))                             // Col 4 (Aisle)
            seatList.add(Seat(rightSeatNum.toString()))        // Col 5
            seatList.add(Seat((rightSeatNum + 1).toString()))  // Col 6
            
            leftSeatNum += 2
            rightSeatNum += 2
        }

        // Row 13 (Back row with 6 seats continuous)
        seatList.add(Seat("25"))
        seatList.add(Seat("26"))
        seatList.add(Seat("27"))
        seatList.add(Seat("52"))
        seatList.add(Seat("53"))
        seatList.add(Seat("54"))
    }

    private fun fetchBookedSeats() {
        database.child("TripBookings").child(tripId).child("bookedSeats")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    
                    // First, reset all real seats to 'available'
                    seatList.forEach { if (it.number.isNotEmpty()) it.isBooked = false }

                    // Then, mark the ones found in Firebase as 'booked'
                    for (seatSnapshot in snapshot.children) {
                        val bookedSeatNum = seatSnapshot.getValue(String::class.java)
                        
                        val seatIndex = seatList.indexOfFirst { it.number == bookedSeatNum }
                        if (seatIndex != -1) {
                            seatList[seatIndex].isBooked = true
                        }
                    }
                    
                    // Refresh the UI
                    seatAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@SeatSelectionActivity, "Failed to load seats", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun confirmBookingAndShowSuccess(selectedSeat: String) {
        // 1. Generate a unique booking ID and QR Data
        val bookingId = "booking_" + UUID.randomUUID().toString().substring(0, 6)
        val qrCodeData = "VERIFY_TICKET_$bookingId"

        // 2. Prepare the data to save to Firebase
        val bookingData = mapOf(
            "passengerName" to "Jayantha Samaranayake", // Get this from Auth later
            "from" to "Kaduwela",
            "to" to "Nittambuwa",
            "date" to "13-01-2026",
            "time" to "10.30 a.m",
            "busName" to "NCG Express",
            "busNumber" to "DL-6389",
            "seatNumber" to "S $selectedSeat",
            "price" to "Rs. 500.00",
            "qrData" to qrCodeData,
            "status" to "SUCCESS"
        )

        // 3. Save to Firebase Database
        database.child("Bookings").child(bookingId).setValue(bookingData)
            .addOnSuccessListener {
                // Also mark the seat as booked in the Trip node
                database.child("TripBookings").child(tripId).child("bookedSeats").push().setValue(selectedSeat)
                
                // FIREBASE SAVE SUCCESSFUL - Show the Popup Dialog!
                showSuccessDialog(bookingId)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Booking failed. Try again.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showSuccessDialog(bookingId: String) {
        // Create the custom dialog
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_booking_success)
        
        // Make the background of the dialog window transparent so the rounded corners show
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // Prevent dismissing by clicking outside (optional)
        dialog.setCancelable(false) 

        // Find the buttons inside the dialog
        val btnCancel = dialog.findViewById<TextView>(R.id.btnDialogCancel)
        val btnDone = dialog.findViewById<TextView>(R.id.btnDialogDone)

        // Cancel Button Action
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        // Done Button Action (Proceed to the E-Ticket screen)
        btnDone.setOnClickListener {
            dialog.dismiss()
            
            // Go to the Booking Details Screen
            val intent = Intent(this, BookingDetailsActivity::class.java)
            intent.putExtra("BOOKING_ID", bookingId)
            startActivity(intent)
            finish() // Close the current selection screen
        }

        // Finally, show it!
        dialog.show()
    }
}
