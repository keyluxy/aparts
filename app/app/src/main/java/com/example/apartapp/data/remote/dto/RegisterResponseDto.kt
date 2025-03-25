package com.example.apartapp.data.remote.dto

import kotlinx.serialization.Serializable

@Suppress("PLUGIN_IS_NOT_ENABLED")
@Serializable
class RegisterResponseDto (
    val status: String,
    val userId: Int
)