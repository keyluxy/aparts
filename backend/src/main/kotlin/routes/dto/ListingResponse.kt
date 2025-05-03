package com.example.routes.dto

import kotlinx.serialization.Serializable

@Serializable
data class ListingResponse(
    val id: Int,
    val title: String,
    val description: String? = null,
    val price: String,
    val district: String? = null,
    val createdAt: String? = null,
    val publicationDate: String? = null,
    val sourceId: Int,
    val cityId: Int,
    val sourceName: String? = null,
    val sourceUrl: String? = null,
    val cityName: String? = null,
    val imageUrls: List<String> = emptyList(),
    val rooms: Int? = null
)
