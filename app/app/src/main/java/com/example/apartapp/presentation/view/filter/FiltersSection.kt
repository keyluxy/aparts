package com.example.apartapp.presentation.view.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.SpaceBetween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.apartapp.domain.model.ListingsFilter
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text


@Composable
fun FilterSection(
    filter: ListingsFilter,
    onFilterChange: (ListingsFilter) -> Unit
) {
    var minPrice by remember { mutableStateOf(filter.minPrice?.toString() ?: "") }
    var maxPrice by remember { mutableStateOf(filter.maxPrice?.toString() ?: "") }
    var minRooms by remember { mutableStateOf(filter.minRooms?.toString() ?: "") }
    var maxRooms by remember { mutableStateOf(filter.maxRooms?.toString() ?: "") }
    var city by remember { mutableStateOf(filter.city ?: "") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Фильтр", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = minPrice,
                onValueChange = {
                    minPrice = it
                    onFilterChange(filter.copy(minPrice = it.toBigDecimalOrNull()))
                },
                label = { Text("Мин. цена") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = maxPrice,
                onValueChange = {
                    maxPrice = it
                    onFilterChange(filter.copy(maxPrice = it.toBigDecimalOrNull()))
                },
                label = { Text("Макс. цена") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = minRooms,
                onValueChange = {
                    minRooms = it
                    onFilterChange(filter.copy(minRooms = it.toIntOrNull()))
                },
                label = { Text("Мин. комнаты") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = maxRooms,
                onValueChange = {
                    maxRooms = it
                    onFilterChange(filter.copy(maxRooms = it.toIntOrNull()))
                },
                label = { Text("Макс. комнаты") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = city,
            onValueChange = {
                city = it
                onFilterChange(filter.copy(city = it.ifBlank { null }))
            },
            label = { Text("Город") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

