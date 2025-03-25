package com.example.apartapp.presentation.viewmodel.state

sealed class AuthState {
    data object Idle: AuthState()
    data object Loading: AuthState()

    data class Registered (val userId: Int): AuthState()
    data class LoggedIn (val token: String): AuthState()
    data class Error (val message: String): AuthState()
}