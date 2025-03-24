package com.example.routes.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterResponse(
    val status: String,
    val userId: Int
)
