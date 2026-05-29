package com.example.lankagotest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView

data class TripSchedule(
    val tripName: String = "",
    val startDetails: String = "",
    val endDetails: String = "",
    val status: String = "",
)

class ScheduleAdapter(private val tripList: List<TripSchedule>) : RecyclerView.Adapter<ScheduleAdapter.TripViewHolder>() {

    class TripViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTripName: TextView = view.findViewById(R.id.tvTripName)
        val tvStartDetails: TextView = view.findViewById(R.id.tvStartDetails)
        val tvEndDetails: TextView = view.findViewById(R.id.tvEndDetails)
        val btnStatusLeft: TextView = view.findViewById(R.id.btnStatusLeft)
        val btnActionRight: TextView = view.findViewById(R.id.btnActionRight)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_schedule_trip, parent, false)
        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val trip = tripList[position]

        holder.tvTripName.text = trip.tripName
        holder.tvStartDetails.text = trip.startDetails
        holder.tvEndDetails.text = trip.endDetails

        // Change button colors and text based on Firebase Status
        when (trip.status) {
            "UPCOMING" -> {
                holder.btnStatusLeft.text = "Upcoming"
                holder.btnStatusLeft.setBackgroundResource(R.drawable.bg_pill_green)
                holder.btnActionRight.visibility = View.VISIBLE
                holder.btnActionRight.text = "View Map"
                holder.btnActionRight.setBackgroundColor("#4A7BBF".toColorInt())
            }
            "ONGOING" -> {
                holder.btnStatusLeft.text = "Ongoing"
                holder.btnStatusLeft.setBackgroundColor("#4A7BBF".toColorInt())
                holder.btnActionRight.visibility = View.VISIBLE
                holder.btnActionRight.text = "End Trip"
                holder.btnActionRight.setBackgroundResource(R.drawable.bg_pill_red)
            }
            "COMPLETED" -> {
                // Hide right button, make left button span full width and say "Completed"
                holder.btnActionRight.visibility = View.GONE
                holder.btnStatusLeft.text = "Completed"
                holder.btnStatusLeft.setBackgroundResource(R.drawable.bg_pill_dark_blue)
            }
        }
    }

    override fun getItemCount() = tripList.size
}
