package com.example.apartapp.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class RegisterRequestDto(
    @SerialName("email")
    val email: String,
    @SerialName("password")
    val password: String,
    @SerialName("firstName")
    val firstName: String,
    @SerialName("lastName")
    val lastName: String,
    @SerialName("middleName")
    val middleName: String? = null
) 