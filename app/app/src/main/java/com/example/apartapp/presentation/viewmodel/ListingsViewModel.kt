package com.example.apartapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apartapp.domain.model.Listing
import com.example.apartapp.domain.model.ListingsFilter
import com.example.apartapp.domain.usecases.GetListingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListingsViewModel @Inject constructor(
    private val getListingsUseCase: GetListingsUseCase
) : ViewModel() {

    private val _listings = MutableStateFlow<List<Listing>>(emptyList())
    val listings: StateFlow<List<Listing>> = _listings

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _filters = MutableStateFlow(ListingsFilter())
    val filters: StateFlow<ListingsFilter> = _filters

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        fetchListings()
    }

    private fun fetchListings() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            getListingsUseCase().fold(
                onSuccess = { fetchedListings ->
                    _listings.value = fetchedListings
                },
                onFailure = { throwable ->
                    _errorMessage.value = throwable.message ?: "Ошибка загрузки объявлений"
                }
            )
            _isLoading.value = false
        }
    }

    fun updateFilters(newFilters: ListingsFilter) {
        _filters.value = newFilters
        applyFilters()
    }

    private fun applyFilters() {
        val allListings = _listings.value
        val currentFilters = _filters.value

        _listings.value = allListings.filter { listing ->
            val priceMatch = (currentFilters.minPrice == null || listing.price >= currentFilters.minPrice) &&
                    (currentFilters.maxPrice == null || listing.price <= currentFilters.maxPrice)

            val roomsMatch = (currentFilters.minRooms == null || (listing.rooms ?: 0) >= currentFilters.minRooms) &&
                    (currentFilters.maxRooms == null || (listing.rooms ?: 0) <= currentFilters.maxRooms)

            val cityMatch = currentFilters.city.isNullOrBlank() || listing.city.equals(currentFilters.city, ignoreCase = true)

            priceMatch && roomsMatch && cityMatch
        }
    }
}
