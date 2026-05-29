package com.example.lankagotest

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Seat(val number: String, var isBooked: Boolean = false)

class SeatAdapter(
    private val seatList: List<Seat>,
    private val onSeatSelected: (String) -> Unit
) : RecyclerView.Adapter<SeatAdapter.SeatViewHolder>() {

    private var selectedPosition: Int = -1

    class SeatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSeatNumber: TextView = itemView.findViewById(R.id.tvSeatNumber)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_seat, parent, false)
        return SeatViewHolder(view)
    }

    override fun onBindViewHolder(holder: SeatViewHolder, position: Int) {
        val seat = seatList[position]

        // Handle Empty Aisle Spaces
        if (seat.number.isEmpty()) {
            holder.tvSeatNumber.visibility = View.INVISIBLE
            holder.itemView.setOnClickListener(null)
            return
        }

        holder.tvSeatNumber.visibility = View.VISIBLE
        holder.tvSeatNumber.text = seat.number

        // Handle Booked vs Selected vs Available
        when {
            seat.isBooked -> {
                holder.tvSeatNumber.setBackgroundResource(R.drawable.bg_seat_booked)
                holder.tvSeatNumber.setTextColor(Color.WHITE)
                holder.itemView.setOnClickListener(null)
            }
            position == selectedPosition -> {
                holder.tvSeatNumber.setBackgroundResource(R.drawable.bg_seat_selected)
                holder.tvSeatNumber.setTextColor(Color.WHITE)
                holder.itemView.setOnClickListener {
                    val oldPos = selectedPosition
                    selectedPosition = -1
                    notifyItemChanged(oldPos)
                    onSeatSelected("")
                }
            }
            else -> {
                holder.tvSeatNumber.setBackgroundResource(R.drawable.bg_seat_available)
                holder.tvSeatNumber.setTextColor(Color.BLACK)
                holder.itemView.setOnClickListener {
                    val oldPos = selectedPosition
                    selectedPosition = holder.adapterPosition
                    if (oldPos != -1) notifyItemChanged(oldPos)
                    notifyItemChanged(selectedPosition)
                    onSeatSelected(seat.number)
                }
            }
        }
    }

    override fun getItemCount() = seatList.size
}
