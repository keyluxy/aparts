package com.example.apartapp.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class CsvImportRequest(
    @SerialName("content")
    val content: String
) 