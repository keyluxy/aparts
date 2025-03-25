package com.example.apartapp.domain.repository

interface AuthRepository {

    suspend fun register(email: String, password: String, firstName: String, lastName: String, middleName: String?): Int
    suspend fun login(email: String, password: String): String
}