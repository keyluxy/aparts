package com.example.apartapp.domain.repository

import com.example.apartapp.domain.model.Listing

interface FavoritesRepository {
    suspend fun getFavorites(userId: Int): List<Listing>
    suspend fun addFavorite(userId: Int, listingId: Int)
    suspend fun removeFavorite(userId: Int, listingId: Int)
}
