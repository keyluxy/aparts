package com.example.apartapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apartapp.domain.usecases.StartParsingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ParsingState {
    object Idle : ParsingState()
    object Loading : ParsingState()
    data class Success(val message: String) : ParsingState()
    data class Error(val message: String) : ParsingState()
}

@HiltViewModel
class ListingsViewModel @Inject constructor(
    private val startParsingUseCase: StartParsingUseCase
) : ViewModel() {

    private val _parsingState = MutableStateFlow<ParsingState>(ParsingState.Idle)
    val parsingState: StateFlow<ParsingState> = _parsingState

    fun startParsing() {
        viewModelScope.launch {
            _parsingState.value = ParsingState.Loading

            startParsingUseCase().fold(
                onSuccess = { message ->
                    _parsingState.value = ParsingState.Success(message)
                },
                onFailure = { throwable ->
                    _parsingState.value = ParsingState.Error(throwable.message ?: "Ошибка при запуске парсера")
                }
            )
        }
    }
} 