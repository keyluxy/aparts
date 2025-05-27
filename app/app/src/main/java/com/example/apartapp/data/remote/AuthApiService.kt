package com.example.apartapp.data.remote

import com.example.apartapp.data.remote.dto.LoginRequestDto
import com.example.apartapp.data.remote.dto.LoginResponseDto
import com.example.apartapp.data.remote.dto.RegisterRequestDto
import com.example.apartapp.data.remote.dto.RegisterResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("/register")
    suspend fun register(@Body request: RegisterRequestDto): RegisterResponseDto

    @POST("/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto
}