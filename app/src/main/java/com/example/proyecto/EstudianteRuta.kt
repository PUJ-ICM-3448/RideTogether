package com.example.proyecto

import androidx.annotation.ColorRes

/**
 * Modelo de datos para los estudiantes en una ruta específica.
 */
data class EstudianteRuta(
    val nombre: String,
    val ruta: String,
    val estado: String,
    @ColorRes val colorEstado: Int
)
