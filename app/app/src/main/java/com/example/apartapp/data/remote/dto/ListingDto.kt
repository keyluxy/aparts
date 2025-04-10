@file:Suppress("PLUGIN_IS_NOT_ENABLED")

package com.example.apartapp.data.remote.dto

import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class ListingDto(
    val id: Int,
    val title: String,
    val description: String? = null,
    val price: BigDecimal,
    val address: String? = null,
    val url: String? = null,
    val imageUrls: List<String>? = null,
    val city: String?,
    val rooms: Int?,
)





