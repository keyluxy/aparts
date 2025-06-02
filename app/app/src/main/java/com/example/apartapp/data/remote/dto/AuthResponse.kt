package com.example.apartapp.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class AuthResponse(
    @SerialName("token")
    val token: String,
    @SerialName("userId")
    val userId: Int
) 