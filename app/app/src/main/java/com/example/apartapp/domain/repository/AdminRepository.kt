package com.example.apartapp.domain.repository

import com.example.apartapp.data.remote.dto.CityDto
import com.example.apartapp.data.remote.dto.SourceDto
import com.example.apartapp.data.remote.dto.UserDto

interface AdminRepository {
    suspend fun checkAdminStatus(): Boolean
    suspend fun getUserInfo(): UserDto
    suspend fun getCities(): List<CityDto>
    suspend fun getSources(): List<SourceDto>
    suspend fun createListing(
        title: String,
        description: String?,
        price: String,
        district: String?,
        rooms: Int?,
        cityName: String,
        sourceName: String,
        publicationDate: String?,
        images: List<String>?
    ): Int
    suspend fun importListingsFromCsv(csvContent: String): Int
} 