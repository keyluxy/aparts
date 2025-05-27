package com.example.apartapp.domain.repository

import com.example.apartapp.data.remote.dto.UserDto

interface UserRepository {
    suspend fun getUserInfo(): UserDto
} 