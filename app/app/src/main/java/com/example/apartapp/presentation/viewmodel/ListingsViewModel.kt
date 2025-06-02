package com.example.apartapp.presentation.viewmodel

import android.icu.math.BigDecimal
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apartapp.domain.model.Listing
import com.example.apartapp.domain.model.ListingsFilter
import com.example.apartapp.domain.repository.FavoritesRepository
import com.example.apartapp.domain.repository.ListingsRepository
import com.example.apartapp.domain.usecases.GetListingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class ListingsViewModel @Inject constructor(
    private val getListingsUseCase: GetListingsUseCase,
    private val favoritesRepository: FavoritesRepository,
    private val listingsRepository: ListingsRepository
) : ViewModel() {

    private val _allListings = MutableStateFlow<List<Listing>>(emptyList())
    private val _listings = MutableStateFlow<List<Listing>>(emptyList())
    val listings: StateFlow<List<Listing>> = _listings

    private val _filters = MutableStateFlow(ListingsFilter())
    val filters: StateFlow<ListingsFilter> = _filters

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _error

    private val _favoriteIds = MutableStateFlow<Set<Int>>(emptySet())
    val favoriteIds: StateFlow<Set<Int>> = _favoriteIds

    private var userId: Int? = null

    init {
        viewModelScope.launch {
            listingsRepository.refreshTrigger.collect {
                if (userId != null) {
                    fetchListings()
                }
            }
        }
    }

    fun setUserId(id: Int) {
        userId = id
        fetchListings()
        loadFavorites()
    }

    fun updateFilters(newFilters: ListingsFilter) {
        _filters.value = newFilters
        applyFilters()
    }

    fun fetchListings() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                getListingsUseCase().fold(
                    onSuccess = {
                        _allListings.value = it
                        applyFilters()
                    },
                    onFailure = { t -> 
                        Log.e("ListingsViewModel", "Error fetching listings", t)
                        _error.value = t.message 
                    }
                )
            } catch (e: Exception) {
                Log.e("ListingsViewModel", "Error in fetchListings", e)
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun applyFilters() {
        val all = _allListings.value
        val f = _filters.value

        _listings.value = all.filter { listing ->
            val priceOk = (f.minPrice == null || listing.price >= BigDecimal(f.minPrice.toString())) &&
                    (f.maxPrice == null || listing.price <= BigDecimal(f.maxPrice.toString()))

            val roomsOk = when {
                f.selectedRooms.isEmpty() -> true
                listing.rooms == null -> false
                else -> {
                    val roomCount = listing.rooms
                    f.selectedRooms.any { selected ->
                        when {
                            selected == 0 && roomCount == 0 -> true
                            selected == 6 && roomCount >= 5 -> true
                            selected == roomCount -> true
                            else -> false
                        }
                    }
                }
            }

            val cityOk = f.city.isNullOrBlank() || listing.city.equals(f.city, ignoreCase = true)
            val sourceOk = f.selectedSources.isEmpty() || f.selectedSources.any { sel ->
                listing.sourceName.equals(sel, ignoreCase = true)
            }

            priceOk && roomsOk && cityOk && sourceOk
        }
    }


    private fun loadFavorites() {
        viewModelScope.launch {
            userId?.let { uid ->
                _favoriteIds.value = favoritesRepository.getFavorites(uid).map { it.id }.toSet()
            }
        }
    }

    fun toggleFavorite(listing: Listing) {
        viewModelScope.launch {
            userId?.let { uid ->
                try {
                    if (listing.id in _favoriteIds.value) {
                        favoritesRepository.removeFavorite(uid, listing.id)
                        _favoriteIds.value -= listing.id
                    } else {
                        favoritesRepository.addFavorite(uid, listing.id)
                        _favoriteIds.value += listing.id
                    }
                } catch (e: HttpException) {
                    _error.value = e.message()
                }
            }
        }
    }
}
