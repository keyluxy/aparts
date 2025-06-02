package com.example.apartapp.domain.model

data class Listing(
    val id: Int,
    val title: String,
    val description: String?,
    val price: android.icu.math.BigDecimal,
    val district: String?,
    val city: String?,
    val rooms: Int?,
    val url: String? = null,
    val imageUrls: List<String> = emptyList(),
    val sourceName: String? = null
)
