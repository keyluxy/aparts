package com.example.apartapp.presentation.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.apartapp.data.remote.dto.CityDto
import com.example.apartapp.data.remote.dto.SourceDto
import com.example.apartapp.data.remote.dto.UserDto
import com.example.apartapp.domain.repository.AdminRepository
import com.example.apartapp.domain.repository.ListingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    application: Application,
    private val adminRepository: AdminRepository,
    private val listingsRepository: ListingsRepository
) : AndroidViewModel(application) {

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage

    private val _userInfo = MutableStateFlow<UserDto?>(null)
    val userInfo: StateFlow<UserDto?> = _userInfo

    private val _cities = MutableStateFlow<List<CityDto>>(emptyList())
    val cities: StateFlow<List<CityDto>> = _cities

    private val _sources = MutableStateFlow<List<SourceDto>>(emptyList())
    val sources: StateFlow<List<SourceDto>> = _sources

    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages: StateFlow<List<Uri>> = _selectedImages

    init {
        loadCitiesAndSources()
        loadUserInfo()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _userInfo.value = adminRepository.getUserInfo()
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading user info", e)
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadCitiesAndSources() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val citiesDeferred = viewModelScope.launch {
                    _cities.value = adminRepository.getCities()
                }
                val sourcesDeferred = viewModelScope.launch {
                    _sources.value = adminRepository.getSources()
                }
                citiesDeferred.join()
                sourcesDeferred.join()
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error loading cities and sources", e)
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkAdminStatus() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val isAdminResult = adminRepository.checkAdminStatus()
                _isAdmin.value = isAdminResult
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error checking admin status", e)
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addImage(uri: Uri) {
        _selectedImages.value = _selectedImages.value + uri
    }

    fun removeImage(uri: Uri) {
        _selectedImages.value = _selectedImages.value - uri
    }

    fun clearImages() {
        _selectedImages.value = emptyList()
    }

    @SuppressLint("ServiceCast")
    fun createListing(
        title: String,
        description: String?,
        price: String,
        district: String?,
        rooms: Int?,
        cityName: String,
        sourceName: String,
        publicationDate: String?
    ) {
        // Валидация обязательных полей
        if (title.isBlank()) {
            _error.value = "Название объявления не может быть пустым"
            return
        }
        if (price.isBlank()) {
            _error.value = "Цена не может быть пустой"
            return
        }
        if (cityName.isBlank()) {
            _error.value = "Город не может быть пустым"
            return
        }
        if (sourceName.isBlank()) {
            _error.value = "Источник не может быть пустым"
            return
        }

        // Валидация цены
        try {
            price.toBigDecimal()
        } catch (e: NumberFormatException) {
            _error.value = "Некорректный формат цены"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _successMessage.value = null
            try {
                val context = getApplication<Application>()
                val images = mutableListOf<String>()

                // Обработка изображений
                Log.d(
                    "AdminViewModel",
                    "Starting image processing. Selected images: ${_selectedImages.value.size}"
                )
                for (uri in _selectedImages.value) {
                    try {
                        Log.d("AdminViewModel", "Processing image: $uri")
                        val base64Image =
                            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                val bytes = inputStream.readBytes()
                                if (bytes.isEmpty()) {
                                    Log.e("AdminViewModel", "Image is empty: $uri")
                                    null
                                } else {
                                    // Проверяем размер изображения (максимум 5MB)
                                    if (bytes.size > 5 * 1024 * 1024) {
                                        Log.e(
                                            "AdminViewModel",
                                            "Image too large: ${bytes.size} bytes"
                                        )
                                        _error.value = "Изображение слишком большое (максимум 5MB)"
                                        null
                                    } else {
                                        val encoded = android.util.Base64.encodeToString(
                                            bytes,
                                            android.util.Base64.NO_WRAP
                                        )
                                        if (encoded.isBlank()) {
                                            Log.e("AdminViewModel", "Failed to encode image: $uri")
                                            null
                                        } else {
                                            Log.d(
                                                "AdminViewModel",
                                                "Image encoded successfully: ${encoded.length} chars"
                                            )
                                            encoded
                                        }
                                    }
                                }
                            } ?: run {
                                Log.e(
                                    "AdminViewModel",
                                    "Failed to open input stream for image: $uri"
                                )
                                null
                            }

                        base64Image?.let {
                            images.add(it)
                            Log.d("AdminViewModel", "Image added to list: $uri")
                        }
                    } catch (e: Exception) {
                        Log.e("AdminViewModel", "Error processing image: $uri", e)
                        _error.value = "Ошибка при обработке изображения: ${e.message}"
                    }
                }

                if (_error.value != null) {
                    return@launch
                }

                Log.d(
                    "AdminViewModel",
                    "Creating listing with params: title=$title, price=$price, cityName=$cityName, sourceName=$sourceName, imagesCount=${images.size}"
                )

                val listingId = adminRepository.createListing(
                    title = title,
                    description = description?.takeIf { it.isNotBlank() },
                    price = price,
                    district = district?.takeIf { it.isNotBlank() },
                    rooms = rooms,
                    cityName = cityName,
                    sourceName = sourceName,
                    publicationDate = publicationDate?.takeIf { it.isNotBlank() },
                    images = images.takeIf { it.isNotEmpty() }
                )

                Log.d("AdminViewModel", "Listing created successfully with ID: $listingId")
                _successMessage.value = "Объявление успешно создано (ID: $listingId)"
                clearImages()

                // Добавляем небольшую задержку перед обновлением списка
                delay(500)

                // Триггерим обновление списка объявлений через репозиторий
                listingsRepository.triggerRefresh()

            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error creating listing", e)
                _error.value = e.message ?: "Ошибка при создании объявления"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun importListingsFromCsv(csvContent: String) {
        viewModelScope.launch {
            Log.d("AdminViewModel", "Starting CSV import process")
            _isLoading.value = true
            _error.value = null
            _successMessage.value = null
            try {
                Log.d("AdminViewModel", "CSV content received (first 100 chars): ${csvContent.take(100)}")
                val importedCount = adminRepository.importListingsFromCsv(csvContent)
                Log.d("AdminViewModel", "CSV import successful, imported $importedCount listings")
                _successMessage.value = "Успешно импортировано $importedCount объявлений"
            } catch (e: Exception) {
                Log.e("AdminViewModel", "Error during CSV import", e)
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearMessages() {
        _error.value = null
        _successMessage.value = null
    }
}
