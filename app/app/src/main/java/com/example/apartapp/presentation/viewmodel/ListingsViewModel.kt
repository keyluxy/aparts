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
import retrofit2.HttpException           // <- обязательно
import javax.inject.Inject

@HiltViewModel
class ListingsViewModel @Inject constructor(
    private val getListingsUseCase: GetListingsUseCase,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _allListings   = MutableStateFlow<List<Listing>>(emptyList())
    private val _listings      = MutableStateFlow<List<Listing>>(emptyList())
    val listings: StateFlow<List<Listing>> = _listings

    private val _isLoading     = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _filters       = MutableStateFlow(ListingsFilter())
    val filters: StateFlow<ListingsFilter> = _filters

    private val _errorMessage  = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _favoriteIds   = MutableStateFlow<Set<Int>>(emptySet())
    val favoriteIds: StateFlow<Set<Int>> = _favoriteIds

    private var userId: Int? = null

    init {
        fetchListings()
    }

    /** Устанавливаем userId и сразу подгружаем избранное */
    fun setUserId(userId: Int) {
        this.userId = userId
        loadFavorites()
    }

    private fun fetchListings() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            getListingsUseCase().fold(
                onSuccess = { fetched ->
                    _allListings.value = fetched
                    applyFilters()
                },
                onFailure = { t ->
                    _errorMessage.value = t.message ?: "Ошибка загрузки объявлений"
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
        val all = _allListings.value
        val f   = _filters.value

        _listings.value = all.filter { listing ->
            val priceMatch = (f.minPrice == null || listing.price >= f.minPrice) &&
                    (f.maxPrice == null || listing.price <= f.maxPrice)
            val roomsMatch = (f.minRooms == null || (listing.rooms ?: 0) >= f.minRooms) &&
                    (f.maxRooms == null || (listing.rooms ?: 0) <= f.maxRooms)
            val cityMatch  = f.city.isNullOrBlank() || listing.city.equals(f.city, ignoreCase = true)
            val srcMatch   = f.source.isNullOrBlank() || listing.sourceName.equals(f.source, ignoreCase = true)
            priceMatch && roomsMatch && cityMatch && srcMatch
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

    /**
     * Переключает статус избранного: POST или DELETE на сервере,
     * затем обновляет локальный state (_favoriteIds).
     */
    fun toggleFavorite(listing: Listing) {
        viewModelScope.launch {
            val user = userId ?: return@launch

            try {
                if (_favoriteIds.value.contains(listing.id)) {
                    favoritesRepository.removeFavorite(user, listing.id)
                    _favoriteIds.value = _favoriteIds.value - listing.id
                } else {
                    favoritesRepository.addFavorite(user, listing.id)
                    _favoriteIds.value = _favoriteIds.value + listing.id
                }
            } catch (e: HttpException) {
                val msg = e.response()?.errorBody()?.string() ?: e.message()
                _errorMessage.value = "Ошибка сервера: $msg"
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Неизвестная ошибка"
            }
        }
    }

}
