package com.example.lankagotest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BusResultAdapter(
    private val tripList: List<TripData>,
    private val onBusClick: (TripData) -> Unit // Handles clicks to go to Seat Selection
) : RecyclerView.Adapter<BusResultAdapter.BusViewHolder>() {

    class BusViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvBusName: TextView = view.findViewById(R.id.tvBusName)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvRoute: TextView = view.findViewById(R.id.tvRoute)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvSeats: TextView = view.findViewById(R.id.tvSeats)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bus_result, parent, false)
        return BusViewHolder(view)
    }

    override fun onBindViewHolder(holder: BusViewHolder, position: Int) {
        val trip = tripList[position]
        
        holder.tvBusName.text = trip.busName
        holder.tvTime.text = trip.departureTime
        holder.tvRoute.text = "${trip.fromLocation} -> ${trip.toLocation}"
        holder.tvPrice.text = trip.price
        holder.tvSeats.text = "${trip.availableSeats} Seats Available"

        // When the user clicks this bus card, trigger the function to go to Seat Selection
        holder.itemView.setOnClickListener {
            onBusClick(trip)
        }
    }

    override fun getItemCount() = tripList.size
}
