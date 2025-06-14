package com.example.routes.dto

import kotlinx.serialization.Serializable

@Serializable
data class CsvImportResponse(
    val importedCount: Int
) 