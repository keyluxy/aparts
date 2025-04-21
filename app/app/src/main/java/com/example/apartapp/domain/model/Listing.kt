package com.example.apartapp.domain.model

import java.math.BigDecimal

data class Listing(
    val id: Int,
    val title: String,
    val description: String?,
    val price: BigDecimal,
    val address: String?,
    val city: String?,
    val rooms: Int?,
    val url: String? = null,          // ссылка на источник
    val imageUrls: List<String> = emptyList(),
    val sourceName: String? = null    // название источника
)
