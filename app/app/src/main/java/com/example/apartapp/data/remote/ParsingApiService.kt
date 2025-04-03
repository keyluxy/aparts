package com.example.apartapp.data.remote

import retrofit2.http.POST

interface ParsingApiService {
    @POST("parse")
    suspend fun startParsing(): Map<String, String>
} 