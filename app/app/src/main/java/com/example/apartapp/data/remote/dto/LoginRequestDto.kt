package com.example.apartapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequestDto(
    val email: String,
    val password: String
) 