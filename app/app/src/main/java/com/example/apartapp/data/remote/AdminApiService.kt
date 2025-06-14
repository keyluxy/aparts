package com.example.apartapp.data.remote

import com.example.apartapp.data.remote.dto.AdminListingRequest
import com.example.apartapp.data.remote.dto.CityDto
import com.example.apartapp.data.remote.dto.SourceDto
import com.example.apartapp.data.remote.dto.UserDto
import com.example.apartapp.data.remote.dto.CsvImportRequest
import com.example.apartapp.data.remote.dto.CsvImportResponse
import retrofit2.http.*

interface AdminApiService {
    @GET("admin/check")
    suspend fun checkAdminStatus(): Map<String, Boolean>

    @GET("admin/user")
    suspend fun getUserInfo(): UserDto

    @GET("admin/cities")
    suspend fun getCities(): List<CityDto>

    @GET("admin/sources")
    suspend fun getSources(): List<SourceDto>

    @POST("admin/listings")
    suspend fun createListing(@Body request: AdminListingRequest): Map<String, Int>

    @POST("admin/listings/import")
    suspend fun importListingsFromCsv(@Body request: CsvImportRequest): CsvImportResponse
} 