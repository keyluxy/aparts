@file:Suppress("UNUSED_EXPRESSION")

package com.example.apartapp.presentation.viewmodel

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apartapp.data.remote.dto.AuthRequest
import com.example.apartapp.data.remote.dto.AuthResponse
import com.example.apartapp.domain.repository.AuthRepository
import com.example.apartapp.domain.usecases.LoginByEmailUseCase
import com.example.apartapp.domain.usecases.RegistrByEmailUseCase
import com.example.apartapp.presentation.validation.AuthValidation
import com.example.apartapp.presentation.validation.ValidationResult
import com.example.apartapp.presentation.viewmodel.state.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


// AuthViewModel.kt
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val loginByEmailUseCase: LoginByEmailUseCase,
    private val registrByEmailUseCase: RegistrByEmailUseCase
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun register(email: String, password: String, firstName: String, lastName: String, middleName: String?, onSuccess: (Int) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                when (val validationResult = AuthValidation.validateRegistration(email, password, firstName, lastName, middleName)) {
                    is ValidationResult.Error -> {
                        _error.value = validationResult.message
                        return@launch
                    }
                    is ValidationResult.Success -> {
                        registrByEmailUseCase(email, password, firstName, lastName, middleName).fold(
                            onSuccess = { userId ->
                                Log.d("AuthViewModel", "Registration successful, userId: $userId")
                                onSuccess(userId)
                            },
                            onFailure = { e ->
                                Log.e("AuthViewModel", "Registration failed", e)
                                _error.value = e.message ?: "Ошибка регистрации"
                            }
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Registration failed", e)
                _error.value = e.message ?: "Ошибка регистрации"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun login(email: String, password: String, onSuccess: (Int) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                loginByEmailUseCase(email, password).fold(
                    onSuccess = { token ->
                        saveToken(token)
                        val userId = getUserIdFromToken(token)
                        Log.d("AuthViewModel", "Login successful, userId: $userId")
                        onSuccess(userId)
                    },
                    onFailure = { e ->
                        Log.e("AuthViewModel", "Login failed", e)
                        _error.value = e.message ?: "Ошибка авторизации"
                    }
                )
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login failed", e)
                _error.value = e.message ?: "Ошибка авторизации"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun saveToken(token: String) {
        try {
            authRepository.saveToken(token)
            Log.d("AuthViewModel", "Token saved successfully")
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error saving token", e)
            _error.value = "Ошибка сохранения токена"
        }
    }

    private fun getUserIdFromToken(token: String): Int {
        return try {
            // Предполагаем, что токен в формате JWT и содержит userId в payload
            val parts = token.split(".")
            if (parts.size != 3) {
                throw IllegalArgumentException("Invalid token format")
            }
            
            val payload = parts[1]
            val decodedPayload = android.util.Base64.decode(payload, android.util.Base64.URL_SAFE)
            val jsonString = String(decodedPayload)
            
            // Извлекаем id из JSON payload
            val userIdRegex = "\"id\":(\\d+)".toRegex()
            val matchResult = userIdRegex.find(jsonString)
            
            if (matchResult != null) {
                val userId = matchResult.groupValues[1].toInt()
                Log.d("AuthViewModel", "Extracted userId from token: $userId")
                userId
            } else {
                Log.e("AuthViewModel", "UserId not found in token")
                throw IllegalArgumentException("UserId not found in token")
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error extracting userId from token", e)
            throw IllegalArgumentException("Invalid token format")
        }
    }
}
