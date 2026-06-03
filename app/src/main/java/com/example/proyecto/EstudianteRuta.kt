package com.example.proyecto

import androidx.annotation.ColorRes

data class EstudianteRuta(
    val nombre: String,
    val ruta: String,
    val estado: String,
    @ColorRes val colorEstado: Int
)
