package com.example.apartapp.data.repository

import com.example.apartapp.data.remote.ListingsApiService
import com.example.apartapp.data.remote.dto.ListingDto
import com.example.apartapp.domain.model.Listing
import com.example.apartapp.domain.repository.ListingsRepository
import javax.inject.Inject

class ListingsRepositoryImpl @Inject constructor(
    private val apiService: ListingsApiService
) : ListingsRepository {
    override suspend fun getListings(): List<Listing> {
        // Получаем данные с сервера и мапим DTO в доменную модель
        return apiService.getListings().map { dto ->
            Listing(
                id = dto.id,
                title = dto.title,
                description = dto.description,
                price = dto.price,
                address = dto.address,
                url = dto.url
            )
        }
    }
}
