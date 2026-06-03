package com.example.proyecto

import androidx.annotation.ColorRes

/**
 * Modelo de datos para las rutas asignadas al conductor.
 */
data class RutaConductor(
    val nombreRuta: String,
    val busAsignado: String,
    val estado: String,
    @ColorRes val colorEstado: Int
)
