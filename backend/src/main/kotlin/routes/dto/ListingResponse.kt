package com.example.routes.dto

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class ListingResponse(
    val id: Int,
    val title: String,
    val description: String? = null,
    val price: String, // Преобразуйте число в строку, либо используйте другой тип, например, Double
    val address: String? = null,
    val createdAt: String? = null, // Можно сохранить дату как строку в формате ISO
    val views: String? = null,
    val publicationDate: String? = null,
    val seller: String? = null,
    val sellerUrl: String? = null,
    val sourceId: Int,
    val cityId: Int,
    val imageUrls: List<String> = emptyList()
)
