package com.example.proyecto

/**
 * Modelo de datos para un incidente en el historial.
 */
data class IncidentItem(
    val title: String,
    val date: String,
    val description: String,
    val author: String
)
