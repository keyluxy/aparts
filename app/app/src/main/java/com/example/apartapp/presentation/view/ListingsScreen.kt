package com.example.apartapp.presentation.view

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.apartapp.presentation.viewmodel.ListingsViewModel
import com.example.apartapp.presentation.viewmodel.ParsingState

@Composable
fun ListingsScreen(
    listingsViewModel: ListingsViewModel = hiltViewModel()
) {
    val parsingState by listingsViewModel.parsingState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Список объявлений",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(
            onClick = { listingsViewModel.startParsing() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Запустить парсинг")
        }

        when (parsingState) {
            is ParsingState.Loading -> {
                CircularProgressIndicator()
            }
            is ParsingState.Success -> {
                Text(
                    text = (parsingState as ParsingState.Success).message,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            is ParsingState.Error -> {
                Text(
                    text = (parsingState as ParsingState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
            else -> {}
        }
    }
}

