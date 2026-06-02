package com.example.proyecto

data class TripHistory(
    val studentName: String,
    val route: String,
    val bus: String?,
    val driver: String?,
    val date: String,
    val status: TripStatus?,
    val timeInfo: String
)
enum class TripStatus {
    COMPLETADO, INCIDENTE
}
