package com.example.apartapp.data.remote

import com.example.apartapp.data.remote.dto.UserDto
import retrofit2.http.GET

interface UserApiService {
    @GET("user/info")
    suspend fun getUserInfo(): UserDto
} 