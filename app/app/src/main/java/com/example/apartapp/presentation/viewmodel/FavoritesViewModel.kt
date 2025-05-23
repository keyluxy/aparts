package com.example.apartapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apartapp.domain.model.Listing
import com.example.apartapp.domain.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _favorites = MutableStateFlow<List<Listing>>(emptyList())
    val favorites: StateFlow<List<Listing>> = _favorites

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage


    fun loadFavorites(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _favorites.value = favoritesRepository.getFavorites(userId)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Ошибка загрузки избранного"
            }
            _isLoading.value = false
        }
    }

    /** Удаляет из избранного, а затем перезагружает список */
    fun removeFavoriteAndRefresh(userId: Int, listingId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                favoritesRepository.removeFavorite(userId, listingId)
                // заново забираем актуальный список
                _favorites.value = favoritesRepository.getFavorites(userId)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Ошибка удаления из избранного"
            }
            _isLoading.value = false
        }
    }
}
