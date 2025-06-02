package com.example.apartapp.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class AuthRequest(
    @SerialName("email")
    val email: String,
    @SerialName("password")
    val password: String,
    @SerialName("firstName")
    val firstName: String? = null,
    @SerialName("lastName")
    val lastName: String? = null,
    @SerialName("middleName")
    val middleName: String? = null
) 