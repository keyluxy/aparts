package com.example.apartapp.data.repository

import android.content.Context
import android.util.Log
import com.example.apartapp.data.remote.AuthApiService
import com.example.apartapp.data.remote.dto.LoginRequestDto
import com.example.apartapp.data.remote.dto.RegisterRequestDto
import com.example.apartapp.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import retrofit2.HttpException

class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    @ApplicationContext private val context: Context
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

    override fun saveToken(token: String) {
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("auth_token", token)
            .apply()
        Log.d("AuthRepository", "Token saved to SharedPreferences")
    }

    override fun getToken(): String? {
        return context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            .getString("auth_token", null)
    }

    override fun clearToken() {
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            .edit()
            .remove("auth_token")
            .apply()
        Log.d("AuthRepository", "Token cleared from SharedPreferences")
    }
}