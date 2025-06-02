package com.example.apartapp.data.repository

import android.icu.math.BigDecimal
import android.util.Log
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
        Log.d("ListingsRepository", "Starting to fetch listings...")
        try {
            val listingsDto = apiService.getListings()
            Log.d("ListingsRepository", "Received ${listingsDto.size} listings from API")
            
            val listings = listingsDto.map { dto ->
                Listing(
                    id = dto.id,
                    title = dto.title,
                    description = dto.description,
                    price = BigDecimal(dto.price),
                    district = dto.district,
                    city = dto.cityName,
                    rooms = dto.rooms,
                    url = dto.sourceUrl,
                    imageUrls = dto.imageUrls ?: emptyList(),
                    sourceName = dto.sourceName
                )
            }
            Log.d("ListingsRepository", "Successfully mapped ${listings.size} listings")
            return listings
        } catch (e: Exception) {
            Log.e("ListingsRepository", "Error fetching listings", e)
            throw e
        }
    }

    override suspend fun triggerRefresh() {
        Log.d("ListingsRepository", "Triggering refresh...")
        _refreshTrigger.emit(Unit)
    }
}

