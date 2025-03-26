package com.example.apartapp.presentation.viewmodel

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



            when (val validationResult = AuthValidation.validateRegistration(
                email,
                password,
                firstName,
                lastName,
                middleName
            )) {
                is ValidationResult.Error -> {
                    _authState.value = AuthState.Error(validationResult.message)
                    return@launch
                }
                is ValidationResult.Success -> {
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
                        onFailure = { throwable ->
                            val errorMessage = when (throwable.message) {
                                "User already exists" -> "Пользователь с таким email уже существует"
                                "Invalid email format" -> "Некорректный формат email"
                                "Password is too weak" -> "Пароль слишком слабый"
                                else -> throwable.message ?: "Ошибка регистрации"
                            }
                            _authState.value = AuthState.Error(errorMessage)
                        }
                    )
                }
            }
        }
    }

    fun login(
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            when (val validationResult = AuthValidation.validateLogin(email, password)) {
                is ValidationResult.Error -> {
                    _authState.value = AuthState.Error(validationResult.message)
                    return@launch
                }
                is ValidationResult.Success -> {
                    val result = loginByEmailUseCase(
                        email,
                        password
                    )

                    result.fold(
                        onSuccess = { token ->
                            if (token != null) {
                                _authState.value = AuthState.LoggedIn(token)
                            } else {
                                _authState.value = AuthState.Error("Ошибка авторизации: токен не получен")
                            }
                        },
                        onFailure = { throwable ->
                            val errorMessage = when (throwable.message) {
                                "User not found" -> "Пользователь не найден"
                                "Invalid password" -> "Неверный пароль"
                                "Account is locked" -> "Аккаунт заблокирован"
                                "User doesn't exist" -> "Пользователь не существует"
                                else -> throwable.message ?: "Ошибка входа"
                            }
                            _authState.value = AuthState.Error(errorMessage)
                        }
                    )
                }
            }
        }
    }
}
