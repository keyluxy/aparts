package com.example.apartapp.data.repository

import com.example.apartapp.data.remote.FavoritesApiService
import com.example.apartapp.domain.model.Listing
import com.example.apartapp.domain.repository.FavoritesRepository
import com.example.apartapp.domain.repository.ListingsRepository
import javax.inject.Inject

class FavoritesRepositoryImpl @Inject constructor(
    private val apiService: FavoritesApiService,
    private val listingsRepository: ListingsRepository
) : FavoritesRepository {

    override suspend fun getFavorites(userId: Int): List<Listing> {
        val favoritesDto = apiService.getFavorites(userId)
        return favoritesDto.map { dto ->
            Listing(
                id = dto.id,
                title = dto.title,
                description = dto.description,
                price = dto.price,
                address = dto.address,
                city = dto.city,
                rooms = null,
                url = dto.url,
                imageUrls = dto.imageUrls ?: emptyList(),
                sourceName = dto.sourceName
            )
        }
    }


    override suspend fun addFavorite(userId: Int, listingId: Int) {
        apiService.addFavorite(userId, listingId)
    }


    override suspend fun removeFavorite(userId: Int, listingId: Int) {
        apiService.removeFavorite(userId, listingId)
    }
}
