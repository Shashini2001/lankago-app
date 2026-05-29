package com.example.lankagotest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RouteAdapter(
    private var routeList: List<BusRoute>,
    private val onRouteClick: (BusRoute) -> Unit
) : RecyclerView.Adapter<RouteAdapter.RouteViewHolder>() {

    class RouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRouteName: TextView = itemView.findViewById(R.id.tvRouteName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_route_card, parent, false)
        return RouteViewHolder(view)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        val currentRoute = routeList[position]
        holder.tvRouteName.text = currentRoute.routeName

        // Handle clicking the item
        holder.itemView.setOnClickListener {
            onRouteClick(currentRoute)
        }
    }

    override fun getItemCount(): Int = routeList.size

    // Used for the Search Bar to update the list
    fun filterList(filteredList: List<BusRoute>) {
        routeList = filteredList
        notifyDataSetChanged()
    }
}
