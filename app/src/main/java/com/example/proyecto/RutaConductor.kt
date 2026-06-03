package com.example.proyecto

import androidx.annotation.ColorRes
data class RutaConductor(
    val nombreRuta: String,
    val busAsignado: String,
    val estado: String,
    @ColorRes val colorEstado: Int
)
