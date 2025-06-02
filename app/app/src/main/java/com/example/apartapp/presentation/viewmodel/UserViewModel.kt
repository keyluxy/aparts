package com.example.apartapp.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apartapp.data.remote.dto.UserDto
import com.example.apartapp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userInfo = MutableStateFlow<UserDto?>(null)
    val userInfo: StateFlow<UserDto?> = _userInfo

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadUserInfo() {
        viewModelScope.launch {
            Log.d("UserViewModel", "Starting to load user info...")
            _isLoading.value = true
            try {
                val info = userRepository.getUserInfo()
                Log.d("UserViewModel", "User info loaded successfully: $info")
                _userInfo.value = info
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error loading user info", e)
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
} 