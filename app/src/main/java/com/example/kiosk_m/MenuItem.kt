package com.example.kiosk_m

data class MenuItem(
    val id: String,
    val name: String,
    val price: Double,
    val category: String,
    val imageRes: Int,
    val quantity: Int = 1
)
