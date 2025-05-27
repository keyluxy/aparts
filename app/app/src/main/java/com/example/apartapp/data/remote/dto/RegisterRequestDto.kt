package com.example.apartapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RegisterRequestDto(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val middleName: String? = null
) 