package com.example.routes.dto

import kotlinx.serialization.Serializable

@Serializable
data class ListingResponse(
    val id: Int,
    val title: String,
    val description: String? = null,
    val price: String,
    val address: String? = null,
    val createdAt: String? = null,
    val views: String? = null,
    val publicationDate: String? = null,
    val seller: String? = null,
    val sellerUrl: String? = null,
    val sourceId: Int,
    val cityId: Int,
    val sourceName: String? = null,
    val sourceUrl: String? = null,
    val cityName: String? = null,  // добавлено
    val imageUrls: List<String> = emptyList(),
    val rooms: Int? = null
)
