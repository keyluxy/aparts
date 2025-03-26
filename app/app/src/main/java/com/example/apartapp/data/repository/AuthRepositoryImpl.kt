package com.example.apartapp.data.repository

import com.example.apartapp.data.remote.AuthApiService
import com.example.apartapp.data.remote.dto.LoginRequestDto
import com.example.apartapp.data.remote.dto.RegisterRequestDto
import com.example.apartapp.domain.repository.AuthRepository
import javax.inject.Inject
import retrofit2.HttpException
import retrofit2.Response

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
        try {
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
        } catch (e: HttpException) {
            when (e.code()) {
                409 -> throw Exception("User already exists")
                400 -> throw Exception("Invalid email format")
                else -> throw Exception("Registration failed: ${e.message()}")
            }
        }
    }

    override suspend fun login(
        email: String,
        password: String
    ): String {
        try {
            val response = apiService.login(
                LoginRequestDto(
                    email,
                    password
                )
            )
            return response.token
        } catch (e: HttpException) {
            when (e.code()) {
                404 -> throw Exception("User not found")
                401 -> throw Exception("Invalid password")
                403 -> throw Exception("Account is locked")
                else -> throw Exception("Login failed: ${e.message()}")
            }
        }
    }

}