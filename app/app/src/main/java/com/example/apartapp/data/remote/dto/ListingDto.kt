package com.example.apartapp.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class ListingDto(
    val id: Int,
    val title: String,
    val description: String? = null,
    val price: BigDecimal,
    val address: String? = null,
    @SerializedName("sourceUrl")
    val url: String? = null,               // теперь будет парситься из "sourceUrl"
    val imageUrls: List<String>? = null,
    @SerializedName("cityName")
    val city: String? = null,              // теперь будет парситься из "cityName"
    val rooms: Int? = null,
    val sourceName: String? = null
)
