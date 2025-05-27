package com.example.apartapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CityDto(
    val id: Int,
    val name: String
) 