package com.example.apartapp.domain.repository

import com.example.apartapp.domain.model.Listing

interface ListingsRepository {
    suspend fun getListings(): List<Listing>
}
