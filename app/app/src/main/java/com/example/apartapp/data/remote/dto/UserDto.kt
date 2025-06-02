package com.example.apartapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("email")
    val email: String,
    @SerializedName("firstName")
    val firstName: String?,
    @SerializedName("lastName")
    val lastName: String?,
    @SerializedName("isAdmin")
    val isAdmin: Boolean
) 