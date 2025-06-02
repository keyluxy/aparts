package com.example.apartapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AdminListingRequest(
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("price")
    val price: String,
    @SerializedName("district")
    val district: String? = null,
    @SerializedName("rooms")
    val rooms: Int? = null,
    @SerializedName("cityName")
    val cityName: String,
    @SerializedName("sourceName")
    val sourceName: String,
    @SerializedName("publicationDate")
    val publicationDate: String? = null,
    @SerializedName("images")
    val images: List<String>? = null
) 