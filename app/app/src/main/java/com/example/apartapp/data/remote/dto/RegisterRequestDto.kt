package com.example.apartapp.data.remote.dto

import kotlinx.serialization.Serializable

@Suppress("PLUGIN_IS_NOT_ENABLED")
@Serializable
class RegisterRequestDto (
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val middleName: String? = null
)