package com.example.routes.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val error: String
)
