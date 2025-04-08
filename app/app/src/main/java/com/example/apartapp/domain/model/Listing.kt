package com.example.apartapp.domain.model

import java.math.BigDecimal

data class Listing(
    val id: Int,
    val title: String,
    val description: String?,
    val price: BigDecimal,
    val address: String?,
    val url: String? = null,
    val imageUrls: List<String> = emptyList()
)
