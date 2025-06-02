package com.example.apartapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SourceDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String
) 