package com.example.proyecto

/**
 * Modelo de datos para las notificaciones.
 * Los tipos pueden ser: "bus", "estudiante", "incidente"
 */
data class NotificationItem(
    val id: Int,
    val type: String,
    val title: String,
    val description: String,
    val iconRes: Int? = null // Opcional si se usan recursos específicos
)
