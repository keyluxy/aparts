package com.example.apartapp.data.repository

import com.example.apartapp.data.remote.ListingsApiService
import com.example.apartapp.domain.model.Listing
import com.example.apartapp.domain.repository.ListingsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ListingsRepositoryImpl @Inject constructor(
    private val apiService: ListingsApiService
) : ListingsRepository {
    private val _refreshTrigger = MutableSharedFlow<Unit>()
    override val refreshTrigger: SharedFlow<Unit> = _refreshTrigger

    override suspend fun getListings(): List<Listing> {
        val listingsDto = apiService.getListings()
        return listingsDto.map { dto ->
            Listing(
                id = dto.id,
                title = dto.title,
                description = dto.description,
                price = dto.price,
                district = dto.district,
                city = dto.city,
                rooms = dto.rooms,
                url = dto.url,
                imageUrls = dto.imageUrls ?: emptyList(),
                sourceName = dto.sourceName
            )
        }
    }

    override suspend fun triggerRefresh() {
        _refreshTrigger.emit(Unit)
    }
}

