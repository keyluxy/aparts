package com.example.model

import java.time.LocalDateTime

data class ListingEntity(
    val id: Long = 0L,
    val title: String,
    val description: String?,
    val price: Double,
    val currency: String = "RUB",
    val address: String?,
    val url: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),

    // Новые поля
    val views: Int? = null,
    val publicationDate: LocalDateTime? = null,
    val seller: String? = null,
    val sellerUrl: String? = null
)
