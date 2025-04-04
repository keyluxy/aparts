package com.example.apartapp.data.remote

import com.example.apartapp.data.remote.dto.ListingDto
import retrofit2.http.GET

interface ListingsApiService {
    @GET("listings")
    suspend fun getListings(): List<ListingDto>
}
