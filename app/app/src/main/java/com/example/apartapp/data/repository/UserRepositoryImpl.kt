package com.example.apartapp.data.repository

import android.util.Log
import com.example.apartapp.data.remote.AdminApiService
import com.example.apartapp.data.remote.dto.UserDto
import com.example.apartapp.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val apiService: AdminApiService
) : UserRepository {
    override suspend fun getUserInfo(): UserDto {
        return try {
            Log.d("UserRepository", "Fetching user info...")
            val userInfo = apiService.getUserInfo()
            Log.d("UserRepository", "Fetched user info: $userInfo")
            userInfo
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching user info", e)
            throw e
        }
    }
} 