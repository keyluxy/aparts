package com.example.apartapp.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.math.BigDecimal

@Serializable
data class ListingDto(
    @SerialName("id")
    val id: Int,
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String?,
    @SerialName("price")
    val price: String,
    @SerialName("district")
    val district: String?,
    @SerialName("sourceUrl")
    val sourceUrl: String?,
    @SerialName("imageUrls")
    val imageUrls: List<String>?,
    @SerialName("cityName")
    val cityName: String?,
    @SerialName("rooms")
    val rooms: Int?,
    @SerialName("sourceName")
    val sourceName: String?
)

