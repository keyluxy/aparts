package com.example.apartapp.data.remote.dto

data class AuthRequest(
    val email: String,
    val password: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val middleName: String? = null
) 