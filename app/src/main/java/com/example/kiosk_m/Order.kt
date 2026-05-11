package com.example.kiosk_m

data class Order(
    val id: String = "",
    val items: List<MenuItem> = emptyList(),
    val status: String = "Preparing.....",
    val timestamp: Long = System.currentTimeMillis(),
    val isMcDo: Boolean = false
) {
    // No-argument constructor required for Firebase
    constructor() : this("", emptyList(), "Preparing.....", System.currentTimeMillis(), false)
}
