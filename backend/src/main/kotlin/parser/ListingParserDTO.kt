package com.example.parser

data class ListingParserDTO(
    val title: String,
    val description: String? = null,
    val price: Double,
    val currency: String = "RUB",
    val cityId: Int,
    val address: String? = null,
    val rooms: Int? = null,
    val area: Double? = null,
    val floor: Int? = null,
    val totalFloors: Int? = null,
    val url: String
)