package com.example.apartapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    val id: Int,
    val username: String,
    @SerializedName("firstName")
    val firstName: String?,
    @SerializedName("lastName")
    val lastName: String?,
    val email: String?,
    @SerializedName("isAdmin")
    val isAdmin: Boolean
) 