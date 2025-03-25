package com.example.apartapp.data.repository

import com.example.apartapp.data.remote.AuthApiService
import com.example.apartapp.data.remote.dto.LoginRequestDto
import com.example.apartapp.data.remote.dto.RegisterRequestDto
import com.example.apartapp.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService
): AuthRepository {

    override suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        middleName: String?
    ): Int {
        val response = apiService.register(
            RegisterRequestDto(
                email,
                password,
                firstName,
                lastName,
                middleName
            )
        )

        return response.userId
    }

    override suspend fun login(
        email: String,
        password: String
    ): String {
        val response = apiService.login(
            LoginRequestDto(
                email,
                password
            )
        )

        return response.token
    }

}