package com.example.kiosk_m

data class MenuItem(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val imageRes: Int = 0,
    val imageUrl: String = "",
    val quantity: Int = 1,
    val components: List<String> = emptyList()
) {
    // No-argument constructor required for Firebase
    constructor() : this("", "", 0.0, "", 0, "", 1, emptyList())
}
