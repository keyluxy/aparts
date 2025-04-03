package com.example.apartapp.data.repository

import com.example.apartapp.data.remote.ParsingApiService
import com.example.apartapp.domain.repository.ParsingRepository
import javax.inject.Inject
import retrofit2.HttpException

class ParsingRepositoryImpl @Inject constructor(
    private val apiService: ParsingApiService
) : ParsingRepository {
    override suspend fun startParsing(): Result<String> {
        return try {
            val response = apiService.startParsing()
            Result.success(response["message"] ?: "Парсинг запущен")
        } catch (e: HttpException) {
            Result.failure(Exception("Ошибка при запуске парсера: ${e.message()}"))
        }
    }
} 