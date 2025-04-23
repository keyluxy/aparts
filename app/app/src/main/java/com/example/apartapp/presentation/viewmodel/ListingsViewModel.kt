package com.example.apartapp.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apartapp.domain.model.Listing
import com.example.apartapp.domain.model.ListingsFilter
import com.example.apartapp.domain.repository.FavoritesRepository
import com.example.apartapp.domain.usecases.GetListingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListingsViewModel @Inject constructor(
    private val getListingsUseCase: GetListingsUseCase,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _allListings = MutableStateFlow<List<Listing>>(emptyList())
    private val _listings = MutableStateFlow<List<Listing>>(emptyList())
    val listings: StateFlow<List<Listing>> = _listings

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _filters = MutableStateFlow(ListingsFilter())
    val filters: StateFlow<ListingsFilter> = _filters

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Состояние избранного
    private val _favoriteIds = MutableStateFlow<Set<Int>>(emptySet())
    val favoriteIds: StateFlow<Set<Int>> = _favoriteIds

    var userId: Int? = null

    init {
        fetchListings()
    }

    fun setUserId(userId: Int) {
        this.userId = userId
        loadFavorites()
    }

    private fun fetchListings() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            getListingsUseCase().fold(
                onSuccess = { fetchedListings ->
                    _allListings.value = fetchedListings
                    applyFilters()
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
        val allListings = _allListings.value
        val currentFilters = _filters.value

        _listings.value = allListings.filter { listing ->
            val priceMatch = (currentFilters.minPrice == null || listing.price >= currentFilters.minPrice) &&
                    (currentFilters.maxPrice == null || currentFilters.maxPrice.let { listing.price <= it })

            val roomsMatch = (currentFilters.minRooms == null || (listing.rooms ?: 0) >= currentFilters.minRooms) &&
                    (currentFilters.maxRooms == null || (listing.rooms ?: 0) <= currentFilters.maxRooms)

            val cityMatch = currentFilters.city.isNullOrBlank() || listing.city.equals(currentFilters.city, ignoreCase = true)

            val sourceMatch = currentFilters.source.isNullOrBlank() || listing.sourceName.equals(currentFilters.source, ignoreCase = true)

            priceMatch && roomsMatch && cityMatch && sourceMatch
        }
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            try {
                val favs = favoritesRepository.getFavorites(userId ?: return@launch)
                _favoriteIds.value = favs.map { it.id }.toSet()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Ошибка загрузки избранного"
            }
        }
    }

    fun toggleFavorite(listing: Listing) {
        viewModelScope.launch {
            try {
                val user = userId ?: return@launch
                val currentFavorites = _favoriteIds.value.toMutableSet()
                if (currentFavorites.contains(listing.id)) {
                    favoritesRepository.removeFavorite(user, listing.id)
                    currentFavorites.remove(listing.id)
                } else {
                    favoritesRepository.addFavorite(user, listing.id)
                    currentFavorites.add(listing.id)
                }
                _favoriteIds.value = currentFavorites.toSet()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Ошибка изменения избранного"
            }
        }
    }
}



