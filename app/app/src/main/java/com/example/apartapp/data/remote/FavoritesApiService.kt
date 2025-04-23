package com.example.apartapp.data.remote

import com.example.apartapp.data.remote.dto.ListingDto
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FavoritesApiService {
    @GET("favorites/{userId}")
    suspend fun getFavorites(@Path("userId") userId: Int): List<ListingDto>

    @POST("favorites/{userId}/{listingId}")
    suspend fun addFavorite(
        @Path("userId") userId: Int,
        @Path("listingId") listingId: Int
    )

    @DELETE("favorites/{userId}/{listingId}")
    suspend fun removeFavorite(
        @Path("userId") userId: Int,
        @Path("listingId") listingId: Int
    )
}
