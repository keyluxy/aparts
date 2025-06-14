package com.example.apartapp.domain.repository

import com.example.apartapp.domain.model.Listing
import kotlinx.coroutines.flow.SharedFlow

interface ListingsRepository {
    suspend fun getListings(): List<Listing>
    suspend fun getListingById(id: Int): Listing
    val refreshTrigger: SharedFlow<Unit>
    suspend fun triggerRefresh()
}
