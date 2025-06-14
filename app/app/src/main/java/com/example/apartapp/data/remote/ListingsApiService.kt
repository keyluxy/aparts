package com.example.apartapp.data.remote

import com.example.apartapp.data.remote.dto.ListingDto
import retrofit2.http.GET
import retrofit2.http.Path

interface ListingsApiService {
    @GET("listings")
    suspend fun getListings(): List<ListingDto>

    @GET("listings/{id}")
    suspend fun getListingById(@Path("id") id: Int): ListingDto
}
