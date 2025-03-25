package com.example.apartapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apartapp.domain.usecases.LoginByEmailUseCase
import com.example.apartapp.domain.usecases.RegistrByEmailUseCase
import com.example.apartapp.presentation.viewmodel.state.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginByEmailUseCase: LoginByEmailUseCase,
    private val registrByEmailUseCase: RegistrByEmailUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        middleName: String?
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = registrByEmailUseCase(
                email,
                password,
                firstName,
                lastName,
                middleName
            )

            result.fold(
                onSuccess = { userId ->
                    _authState.value = AuthState.Registered(userId)
                },

                onFailure = { throlable ->
                    _authState.value = AuthState.Error(throlable.message ?: "Registration failed")
                }
            )
        }
    }

    fun login(
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = loginByEmailUseCase(
                email,
                password
            )

            result.fold(
                onSuccess = { token ->
                    _authState.value = AuthState.LoggedIn(token)
                },
                onFailure = { throlable ->
                    _authState.value = AuthState.Error(throlable.message ?: "Login failed")
                }
            )
        }
    }
}
