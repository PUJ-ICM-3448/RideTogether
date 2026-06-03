package com.example.proyecto

data class NotificationItem(
    val id: Int,
    val type: String,
    val title: String,
    val description: String,
    val iconRes: Int? = null
)
