package com.example.apartapp.presentation.view

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.apartapp.data.remote.dto.UserDto
import com.example.apartapp.presentation.viewmodel.AdminViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userInfo: UserDto?,
    onLogout: () -> Unit,
    onBackClick: () -> Unit,
    isLoading: Boolean = false,
    adminViewModel: AdminViewModel // Передаем ViewModel напрямую
) {
    var showCreateListingDialog by remember { mutableStateOf(false) }
    var showImportCsvDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Аватар пользователя
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                // Информация о пользователе
                userInfo?.let { user ->
                    user.email?.let { email ->
                        Text(
                            text = email,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    if (!user.firstName.isNullOrBlank() || !user.lastName.isNullOrBlank()) {
                        Text(
                            text = buildString {
                                user.firstName?.let { append(it) }
                                if (!user.firstName.isNullOrBlank() && !user.lastName.isNullOrBlank()) {
                                    append(" ")
                                }
                                user.lastName?.let { append(it) }
                            },
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    if (user.isAdmin) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                text = "Администратор",
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        // Кнопка добавления объявления
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

                Spacer(modifier = Modifier.height(32.dp))

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
            }
        }
    }

    // Диалог добавления объявления
    if (showCreateListingDialog) {
        CreateListingDialog(
            onDismiss = { showCreateListingDialog = false },
            onConfirm = { title, description, price, district, rooms, cityName, sourceName, publicationDate ->
                adminViewModel.createListing(
                    title = title,
                    description = description,
                    price = price,
                    district = district,
                    rooms = rooms,
                    cityName = cityName,
                    sourceName = sourceName,
                    publicationDate = publicationDate
                )
                showCreateListingDialog = false
            },
            adminViewModel = adminViewModel
        )
    }

    // Диалог импорта CSV
    if (showImportCsvDialog) {
        ImportCsvDialog(
            onDismiss = { showImportCsvDialog = false },
            onConfirm = { content ->
                adminViewModel.importListingsFromCsv(content)
                showImportCsvDialog = false
            }
        )
    }
}