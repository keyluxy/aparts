package com.example.apartapp.domain.repository

interface AuthRepository {
    suspend fun login(email: String, password: String): String
    suspend fun register(email: String, password: String, firstName: String, lastName: String, middleName: String?): Int
    fun saveToken(token: String)
    fun getToken(): String?
    fun clearToken()
}