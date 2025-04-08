package com.example.apartapp.data.repository

import com.example.apartapp.data.remote.ListingsApiService
import com.example.apartapp.domain.model.Listing
import com.example.apartapp.domain.repository.ListingsRepository
import javax.inject.Inject

class ListingsRepositoryImpl @Inject constructor(
    private val apiService: ListingsApiService
) : ListingsRepository {
    override suspend fun getListings(): List<Listing> {
        val listingsDto = apiService.getListings()

        return listingsDto.map { dto ->
            // Формируем URL-ы для изображений, если список imageUrls отсутствует (null), заменяем на emptyList()
            val imageUrls = dto.imageUrls ?: emptyList()
            Listing(
                id = dto.id,
                title = dto.title,
                description = dto.description,
                price = dto.price,
                address = dto.address,
                url = dto.url,
                imageUrls = imageUrls
            )
        }
    }
}
