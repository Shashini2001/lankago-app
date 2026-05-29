package com.example.lankagotest

data class TripData(
    val tripId: String = "",
    val busName: String = "",
    val fromLocation: String = "",
    val toLocation: String = "",
    val departureTime: String = "",
    val price: String = "",
    val availableSeats: Int = 0
)
