package com.example.routes.dto

import kotlinx.serialization.Serializable

@Serializable
data class CsvImportRequest(
    val content: String
) 