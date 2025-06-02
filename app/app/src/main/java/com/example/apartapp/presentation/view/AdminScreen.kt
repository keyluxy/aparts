package com.example.apartapp.presentation.view

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.apartapp.data.remote.dto.CityDto
import com.example.apartapp.data.remote.dto.SourceDto
import com.example.apartapp.data.remote.dto.UserDto
import com.example.apartapp.presentation.viewmodel.AdminViewModel
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    adminViewModel: AdminViewModel,
    onBackClick: () -> Unit,
    onLogout: () -> Unit
) {
    var showCreateListingDialog by remember { mutableStateOf(false) }
    var showImportCsvDialog by remember { mutableStateOf(false) }
    val cities by adminViewModel.cities.collectAsState()
    val sources by adminViewModel.sources.collectAsState()
    val userInfo by adminViewModel.userInfo.collectAsState()
    val isLoading by adminViewModel.isLoading.collectAsState()
    val error by adminViewModel.error.collectAsState()
    val successMessage by adminViewModel.successMessage.collectAsState()

    LaunchedEffect(Unit) {
        adminViewModel.clearMessages()
        adminViewModel.loadCitiesAndSources()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Админ-панель") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Информация о пользователе
                userInfo?.let { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Пользователь: ${user.email}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Администратор",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Секция админ-функционала
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Управление объявлениями",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        // Кнопка создания объявления
                        Button(
                            onClick = { showCreateListingDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Добавить объявление")
                        }

                        // Кнопка импорта из CSV
                        Button(
                            onClick = { showImportCsvDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Импорт из CSV")
                        }
                    }
                }

                // Кнопка выхода
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Выйти из аккаунта")
                }

                // Сообщения об ошибках и успехе
                error?.let { errorMessage ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = errorMessage,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                successMessage?.let { message ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        if (showCreateListingDialog) {
            CreateListingDialog(
                onDismiss = { showCreateListingDialog = false },
                onConfirm = { title, description, price, district, rooms, cityName, sourceName, publicationDate ->
                    adminViewModel.createListing(
                        title,
                        description,
                        price,
                        district,
                        rooms,
                        cityName,
                        sourceName,
                        publicationDate
                    )
                    showCreateListingDialog = false
                },
                adminViewModel = adminViewModel
            )
        }

        if (showImportCsvDialog) {
            ImportCsvDialog(
                onDismiss = { showImportCsvDialog = false },
                onConfirm = { content ->
                    adminViewModel.importListingsFromCsv(content)
                    showImportCsvDialog = false
                },
                isLoading = isLoading,
                error = error
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListingDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        description: String?,
        price: String,
        district: String?,
        rooms: Int?,
        cityName: String,
        sourceName: String,
        publicationDate: String?
    ) -> Unit,
    adminViewModel: AdminViewModel
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var rooms by remember { mutableStateOf("") }
    var cityName by remember { mutableStateOf("") }
    var sourceName by remember { mutableStateOf("") }
    var showErrors by remember { mutableStateOf(false) }
    
    val cities by adminViewModel.cities.collectAsState()
    val sources by adminViewModel.sources.collectAsState()
    val selectedImages by adminViewModel.selectedImages.collectAsState()
    val error by adminViewModel.error.collectAsState()
    val successMessage by adminViewModel.successMessage.collectAsState()
    val isLoading by adminViewModel.isLoading.collectAsState()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { adminViewModel.addImage(it) }
    }

    // Валидация полей
    val titleError = if (showErrors && title.isBlank()) "Обязательное поле" else null
    
    val priceError = if (showErrors) {
        when {
            price.isBlank() -> "Обязательное поле"
            else -> try {
                price.toBigDecimal()
                null
            } catch (e: NumberFormatException) {
                "Некорректный формат"
            }
        }
    } else null
    
    val cityError = if (showErrors && cityName.isBlank()) "Обязательное поле" else null
    val sourceError = if (showErrors && sourceName.isBlank()) "Обязательное поле" else null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Создать объявление") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Заголовок
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Заголовок") },
                    isError = titleError != null,
                    supportingText = { titleError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                // Описание
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                // Цена
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Цена") },
                    isError = priceError != null,
                    supportingText = { priceError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Район
                OutlinedTextField(
                    value = district,
                    onValueChange = { district = it },
                    label = { Text("Район") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Количество комнат
                OutlinedTextField(
                    value = rooms,
                    onValueChange = { rooms = it },
                    label = { Text("Количество комнат") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Город (текстовое поле)
                OutlinedTextField(
                    value = cityName,
                    onValueChange = { cityName = it },
                    label = { Text("Город") },
                    isError = cityError != null,
                    supportingText = { cityError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                // Источник (текстовое поле)
                OutlinedTextField(
                    value = sourceName,
                    onValueChange = { sourceName = it },
                    label = { Text("Источник") },
                    isError = sourceError != null,
                    supportingText = { sourceError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                // Кнопка добавления изображений
                Button(
                    onClick = { 
                        try {
                            imagePicker.launch("image/*")
                        } catch (e: Exception) {
                            Log.e("AdminScreen", "Error launching image picker", e)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Добавить изображение")
                }

                // Сетка изображений
                if (selectedImages.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedImages) { uri ->
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .padding(4.dp)
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { 
                                        try {
                                            adminViewModel.removeImage(uri)
                                        } catch (e: Exception) {
                                            Log.e("AdminScreen", "Error removing image", e)
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Удалить",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                // Сообщения об ошибках и успехе
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                successMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    showErrors = true
                    if (titleError == null && priceError == null && cityError == null && sourceError == null) {
                        onConfirm(
                            title,
                            description.takeIf { it.isNotBlank() },
                            price,
                            district.takeIf { it.isNotBlank() },
                            rooms.toIntOrNull(),
                            cityName,
                            sourceName,
                            null
                        )
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Создать")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Отмена")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportCsvDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    isLoading: Boolean = false,
    error: String? = null
) {
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }

    val csvFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            selectedFileUri = selectedUri
            errorMessage = null
        }
    }

    // Запрос разрешения на чтение файлов
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            csvFilePicker.launch("*/*")
        } else {
            errorMessage = "Для импорта CSV файлов необходимо разрешение на чтение файлов"
        }
    }

    // Функция для проверки и запроса разрешений
    fun checkPermissionAndPickFile() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                csvFilePicker.launch("*/*")
            }
            hasPermission -> {
                csvFilePicker.launch("*/*")
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    // Проверяем разрешение при первом запуске
    LaunchedEffect(Unit) {
        hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Импорт объявлений из CSV") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Инструкции по формату CSV
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Требования к CSV файлу:",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            "• Первая строка должна содержать заголовки",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            "• Обязательные колонки: title, price, url",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            "• Опциональные колонки: description, district, rooms, publication_date, image_urls",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                // Кнопка выбора файла
                Button(
                    onClick = { checkPermissionAndPickFile() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Icon(
                        if (selectedFileUri == null) Icons.Outlined.Add else Icons.Outlined.Edit,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedFileUri == null) "Выбрать CSV файл" else "Изменить файл")
                }

                // Отображение выбранного файла
                selectedFileUri?.let { uri ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Выбран файл:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = uri.lastPathSegment ?: "Неизвестный файл",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = {
                                    if (!isLoading) {
                                        selectedFileUri = null
                                        errorMessage = null
                                    }
                                },
                                enabled = !isLoading
                            ) {
                                Icon(
                                    Icons.Outlined.Clear,
                                    contentDescription = "Очистить выбор",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                // Отображение ошибок
                (errorMessage ?: error)?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedFileUri?.let { uri ->
                        try {
                            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                                val content = BufferedReader(InputStreamReader(inputStream))
                                    .use { it.readText() }
                                if (content.isBlank()) {
                                    errorMessage = "Файл пуст"
                                } else {
                                    onConfirm(content)
                                }
                            } ?: run {
                                errorMessage = "Не удалось прочитать файл"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Ошибка при чтении файла: ${e.message}"
                        }
                    } ?: run {
                        errorMessage = "Пожалуйста, выберите файл"
                    }
                },
                enabled = selectedFileUri != null && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Импортировать")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Отмена")
            }
        }
    )
} 