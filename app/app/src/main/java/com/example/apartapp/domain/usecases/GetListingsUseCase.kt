package com.example.apartapp.domain.usecases

import com.example.apartapp.domain.model.Listing
import com.example.apartapp.domain.repository.ListingsRepository
import javax.inject.Inject

class GetListingsUseCase @Inject constructor(
    private val listingsRepository: ListingsRepository
) {
    suspend operator fun invoke(): Result<List<Listing>> {
        return runCatching {
            listingsRepository.getListings()
        }
    }
}
