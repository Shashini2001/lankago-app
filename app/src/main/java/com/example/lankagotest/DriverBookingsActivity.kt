package com.example.lankagotest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class DriverBookingsActivity : AppCompatActivity() {

    private lateinit var rvBookings: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var database: DatabaseReference
    private val bookingsList = mutableListOf<Booking>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_bookings)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        rvBookings = findViewById(R.id.rvBookings)
        tvEmpty = findViewById(R.id.tvEmpty)

        btnBack.setOnClickListener { finish() }

        rvBookings.layoutManager = LinearLayoutManager(this)
        val adapter = BookingsAdapter(bookingsList)
        rvBookings.adapter = adapter

        database = FirebaseDatabase.getInstance().reference.child("Bookings")
        
        // Fetch bookings (you can filter by driver's bus number here later)
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                bookingsList.clear()
                for (data in snapshot.children) {
                    val booking = data.getValue(Booking::class.java)
                    if (booking != null) {
                        bookingsList.add(booking)
                    }
                }
                
                if (bookingsList.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                } else {
                    tvEmpty.visibility = View.GONE
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    class BookingsAdapter(private val list: List<Booking>) : RecyclerView.Adapter<BookingsAdapter.ViewHolder>() {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val name: TextView = view.findViewById(R.id.tvPassengerName)
            val route: TextView = view.findViewById(R.id.tvRoute)
            val seat: TextView = view.findViewById(R.id.tvSeat)
            val time: TextView = view.findViewById(R.id.tvBookingTime)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_booking, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val booking = list[position]
            holder.name.text = booking.passengerName
            holder.route.text = "${booking.from} to ${booking.to}"
            holder.seat.text = "Seat: ${booking.seatNumber}"
            holder.time.text = "Time: ${booking.time}"
        }

        override fun getItemCount() = list.size
    }
}
