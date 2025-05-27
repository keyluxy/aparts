package com.example.routes.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Int,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val isAdmin: Boolean
) 