package com.example.apartapp.domain.repository

interface ParsingRepository {
    suspend fun startParsing(): Result<String>
} 