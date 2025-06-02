package com.example.routes.dto

import kotlinx.serialization.Serializable

@Serializable
data class AdminListingRequest(
    val title: String,
    val description: String? = null,
    val price: String, // BigDecimal в виде строки
    val district: String? = null,
    val rooms: Int? = null,
    val cityName: String,
    val sourceName: String,
    val publicationDate: String? = null,
    val images: List<String>? = null // Base64 строки
) 