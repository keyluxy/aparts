package com.example.apartapp.presentation.view.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.apartapp.domain.model.ListingsFilter
import androidx.compose.ui.graphics.Color


@Composable
fun FilterSection(
    filter: ListingsFilter,
    onFilterChange: (ListingsFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    var minPriceText by remember { mutableStateOf(filter.minPrice?.toString() ?: "") }
    var maxPriceText by remember { mutableStateOf(filter.maxPrice?.toString() ?: "") }
    var minRoomsText by remember { mutableStateOf(filter.minRooms?.toString() ?: "") }
    var maxRoomsText by remember { mutableStateOf(filter.maxRooms?.toString() ?: "") }
    var cityText by remember { mutableStateOf(filter.city ?: "") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text("Фильтр", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = minPriceText,
                onValueChange = {
                    minPriceText = it
                    onFilterChange(
                        filter.copy(
                            minPrice = it.toBigDecimalOrNull()
                        )
                    )
                },
                label = { Text("Мин. цена") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = maxPriceText,
                onValueChange = {
                    maxPriceText = it
                    onFilterChange(
                        filter.copy(
                            maxPrice = it.toBigDecimalOrNull()
                        )
                    )
                },
                label = { Text("Макс. цена") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = minRoomsText,
                onValueChange = {
                    minRoomsText = it
                    onFilterChange(
                        filter.copy(
                            minRooms = it.toIntOrNull()
                        )
                    )
                },
                label = { Text("Мин. комнаты") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = maxRoomsText,
                onValueChange = {
                    maxRoomsText = it
                    onFilterChange(
                        filter.copy(
                            maxRooms = it.toIntOrNull()
                        )
                    )
                },
                label = { Text("Макс. комнаты") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = cityText,
            onValueChange = {
                cityText = it
                onFilterChange(
                    filter.copy(
                        city = it.ifBlank { null }
                    )
                )
            },
            label = { Text("Город") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}


