package com.example.apartapp.data.repository

import android.content.Context
import android.util.Log
import com.example.apartapp.data.remote.AuthApiService
import com.example.apartapp.data.remote.dto.AuthResponse
import com.example.apartapp.data.remote.dto.LoginRequestDto
import com.example.apartapp.data.remote.dto.RegisterRequestDto
import com.example.apartapp.domain.repository.AuthRepository
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import retrofit2.HttpException
import retrofit2.Response
import okhttp3.ResponseBody

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
            Log.d("AuthRepository", "Sending registration request for email: $email")
            val request = RegisterRequestDto(
                email,
                password,
                firstName,
                lastName,
                middleName
            )
            Log.d("AuthRepository", "Request body: $request")
            
            // Получаем сырой ответ от сервера
            val rawResponse = apiService.register(request)
            Log.d("AuthRepository", "Raw response from server: ${Gson().toJson(rawResponse)}")
            
            // Проверяем тип ответа
            Log.d("AuthRepository", "Response class type: ${rawResponse.javaClass.name}")
            Log.d("AuthRepository", "Response fields: ${rawResponse.javaClass.declaredFields.joinToString { it.name }}")
            
            if (rawResponse.token.isNullOrEmpty()) {
                Log.e("AuthRepository", "Token is null or empty in response. Full response: ${Gson().toJson(rawResponse)}")
                throw Exception("Invalid token received from server")
            }
            
            saveToken(rawResponse.token)
            val userId = getUserIdFromToken(rawResponse.token)
            Log.d("AuthRepository", "Successfully registered user with id: $userId")
            return userId
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Log.e("AuthRepository", "HTTP error during registration: ${e.code()} - ${e.message()}")
            Log.e("AuthRepository", "Error response body: $errorBody")
            when (e.code()) {
                409 -> throw Exception("User already exists")
                400 -> throw Exception("Invalid email format")
                else -> throw Exception("Registration failed: ${e.message()}")
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error during registration", e)
            throw e
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

    private fun getUserIdFromToken(token: String): Int {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) {
                throw IllegalArgumentException("Invalid token format")
            }
            
            val payload = parts[1]
            val decodedPayload = android.util.Base64.decode(payload, android.util.Base64.URL_SAFE)
            val jsonString = String(decodedPayload)
            
            val userIdRegex = "\"id\":(\\d+)".toRegex()
            val matchResult = userIdRegex.find(jsonString)
            
            if (matchResult != null) {
                val userId = matchResult.groupValues[1].toInt()
                Log.d("AuthRepository", "Extracted userId from token: $userId")
                userId
            } else {
                Log.e("AuthRepository", "UserId not found in token")
                throw IllegalArgumentException("UserId not found in token")
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error extracting userId from token", e)
            throw IllegalArgumentException("Invalid token format")
        }
    }
}