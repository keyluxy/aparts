package com.example.apartapp.data.repository

import android.util.Log
import com.example.apartapp.data.remote.AdminApiService
import com.example.apartapp.data.remote.dto.AdminListingRequest
import com.example.apartapp.data.remote.dto.CityDto
import com.example.apartapp.data.remote.dto.SourceDto
import com.example.apartapp.data.remote.dto.UserDto
import com.example.apartapp.domain.repository.AdminRepository
import javax.inject.Inject

class AdminRepositoryImpl @Inject constructor(
    private val apiService: AdminApiService
) : AdminRepository {
    private var citiesCache: List<CityDto> = emptyList()
    private var sourcesCache: List<SourceDto> = emptyList()

    override suspend fun checkAdminStatus(): Boolean {
        return try {
            Log.d("AdminRepository", "Checking admin status...")
            val response = apiService.checkAdminStatus()
            val isAdmin = response["isAdmin"] ?: false
            Log.d("AdminRepository", "Admin status response: $response, isAdmin: $isAdmin")
            isAdmin
        } catch (e: Exception) {
            Log.e("AdminRepository", "Error checking admin status", e)
            false
        }
    }

    override suspend fun getUserInfo(): UserDto {
        return try {
            Log.d("AdminRepository", "Fetching user info...")
            val userInfo = apiService.getUserInfo()
            Log.d("AdminRepository", "Fetched user info: $userInfo")
            userInfo
        } catch (e: Exception) {
            Log.e("AdminRepository", "Error fetching user info", e)
            throw e
        }
    }

    override suspend fun getCities(): List<CityDto> {
        citiesCache = apiService.getCities()
        return citiesCache
    }

    override suspend fun getSources(): List<SourceDto> {
        sourcesCache = apiService.getSources()
        return sourcesCache
    }

    override suspend fun createListing(
        title: String,
        description: String?,
        price: String,
        district: String?,
        rooms: Int?,
        cityName: String,
        sourceName: String,
        publicationDate: String?,
        images: List<String>?
    ): Int {
        Log.d("AdminRepository", "Creating listing with params: title=$title, price=$price, cityName=$cityName, sourceName=$sourceName")
        Log.d("AdminRepository", "Optional params: description=${description?.take(50)}, district=$district, rooms=$rooms, publicationDate=$publicationDate")
        Log.d("AdminRepository", "Images count: ${images?.size ?: 0}")
        
        if (images != null) {
            images.forEachIndexed { index, image ->
                Log.d("AdminRepository", "Image $index size: ${image.length} chars")
            }
        }

        val request = AdminListingRequest(
            title = title,
            description = description,
            price = price,
            district = district,
            rooms = rooms,
            cityName = cityName,
            sourceName = sourceName,
            publicationDate = publicationDate,
            images = images
        )

        try {
            val response = apiService.createListing(request)
            val id = response["id"] ?: throw IllegalStateException("No ID returned")
            Log.d("AdminRepository", "Listing created successfully with ID: $id")
            return id
        } catch (e: Exception) {
            Log.e("AdminRepository", "Error creating listing", e)
            Log.e("AdminRepository", "Request details: $request")
            throw e
        }
    }

    override suspend fun importListingsFromCsv(csvContent: String): Int {
        // Пропускаем загрузку кеша городов и источников, если не используется
        // if (citiesCache.isEmpty()) getCities()
        // if (sourcesCache.isEmpty()) getSources()

        // Преобразуем CSV контент
        val lines = csvContent.lines()
        if (lines.size < 2) throw IllegalArgumentException("CSV файл пуст или содержит только заголовки")

        // Проверяем только базовые заголовки, остальную валидацию на сервере
        val headers = lines[0].split(",").map { it.trim().replace("\"", "") }
        Log.d("AdminRepository", "Заголовки CSV: ${headers.joinToString(", ")}")
        Log.d("AdminRepository", "Первая строка CSV: ${lines[0]}")

        // Проверяем только наличие минимально необходимых колонок на клиенте
        val minimalRequiredColumns = listOf("title", "price")
        val missingMinimalColumns = minimalRequiredColumns.filter { it !in headers }

        if (missingMinimalColumns.isNotEmpty()) {
            throw IllegalArgumentException("В CSV файле отсутствуют обязательные колонки: ${missingMinimalColumns.joinToString()}")
        }

        // Добавляем логирование перед отправкой на сервер
        Log.d("AdminRepository", "Import CSV: Sending CSV content to server.")
        Log.d("AdminRepository", "Import CSV: Content size: ${csvContent.length} chars")
        Log.d("AdminRepository", "Import CSV: First 200 chars of content: ${csvContent.take(200)}")

        // Удаляем всю логику преобразования строк на клиенте
        // Оставляем только отправку исходного контента на сервер
        val response = apiService.importListingsFromCsv(csvContent)
        return response["importedCount"] as? Int ?: throw IllegalStateException("Не удалось получить количество импортированных объявлений")
    }
} 