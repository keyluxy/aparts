package com.example.routes.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterResponse(
    val token: String
) 