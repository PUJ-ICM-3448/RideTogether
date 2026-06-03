package com.example.proyecto

/**
 * Modelo de datos para un registro en el historial de viajes.
 */
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
