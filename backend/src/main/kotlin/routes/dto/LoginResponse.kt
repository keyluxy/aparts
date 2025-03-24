package com.example.routes.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String
)
