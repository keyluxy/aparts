@file:Suppress("UNUSED_EXPRESSION")

package com.example.apartapp.presentation.viewmodel

import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apartapp.domain.usecases.LoginByEmailUseCase
import com.example.apartapp.domain.usecases.RegistrByEmailUseCase
import com.example.apartapp.presentation.validation.AuthValidation
import com.example.apartapp.presentation.validation.ValidationResult
import com.example.apartapp.presentation.viewmodel.state.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


// AuthViewModel.kt
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginByEmailUseCase: LoginByEmailUseCase,
    private val registrByEmailUseCase: RegistrByEmailUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun register(email: String, password: String, firstName: String, lastName: String, middleName: String?) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            when (val validationResult = AuthValidation.validateRegistration(email, password, firstName, lastName, middleName)) {
                is ValidationResult.Error -> {
                    _authState.value = AuthState.Error(validationResult.message)
                    return@launch
                }
                is ValidationResult.Success -> {
                    val result = registrByEmailUseCase(email, password, firstName, lastName, middleName)
                    result.fold(
                        onSuccess = { userId -> _authState.value = AuthState.Registered(userId) },
                        onFailure = { throwable -> _authState.value = AuthState.Error(throwable.message ?: "Ошибка регистрации") }
                    )
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            when (val validationResult = AuthValidation.validateLogin(email, password)) {
                is ValidationResult.Error -> {
                    _authState.value = AuthState.Error(validationResult.message)
                    return@launch
                }
                is ValidationResult.Success -> {
                    val result = loginByEmailUseCase(email, password)
                    result.fold(
                        onSuccess = { token -> _authState.value = AuthState.LoggedIn(token ?: "") },
                        onFailure = { throwable -> _authState.value = AuthState.Error(throwable.message ?: "Ошибка входа") }
                    )
                }
            }
        }
    }

    fun getUserIdFromToken(token: String): Int {
        return try {
            val parts = token.split(".")
            val payload = parts[1]
            val decoded = String(Base64.decode(payload, Base64.URL_SAFE), Charsets.UTF_8)
            val json = kotlinx.serialization.json.Json
                .decodeFromString<kotlinx.serialization.json.JsonObject>(decoded)
            // ← Здесь меняем "userId" на "id":
            json["id"]?.toString()?.replace("\"", "")?.toInt() ?: 0
        } catch (e: Exception) {
            0
        }
    }

}
