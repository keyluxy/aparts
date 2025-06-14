package com.example.apartapp.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class CsvImportResponse(
    @SerialName("importedCount")
    val importedCount: Int
) 