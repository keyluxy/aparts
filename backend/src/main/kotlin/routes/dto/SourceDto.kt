package com.example.routes.dto

import kotlinx.serialization.Serializable

@Serializable
data class SourceDto(
    val id: Int,
    val name: String
) 