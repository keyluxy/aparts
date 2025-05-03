package com.example.apartapp.presentation.view.filter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.apartapp.domain.model.ListingsFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(
    filter: ListingsFilter,
    onFilterChange: (ListingsFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    var cityText by remember { mutableStateOf(filter.city ?: "") }
    var minPriceText by remember { mutableStateOf(filter.minPrice?.toString() ?: "") }
    var maxPriceText by remember { mutableStateOf(filter.maxPrice?.toString() ?: "") }

    val sourceMap = mapOf(
        "avito" to "Авито",
        "domklik" to "ДомКлик",
        "cian" to "Циан"
    )
    val reverseSourceMap = sourceMap.entries.associate { (en, ru) -> ru to en }
    val allSourcesRu = sourceMap.values.toList()

    var expanded by remember { mutableStateOf(false) }
    var roomsDropdownExpanded by remember { mutableStateOf(false) }
    var sourcesDropdownExpanded by remember { mutableStateOf(false) }
    var sourceSearch by remember { mutableStateOf("") }

    val filteredSources = remember(sourceSearch) {
        if (sourceSearch.isBlank()) allSourcesRu
        else allSourcesRu.filter { ru ->
            val en = reverseSourceMap[ru] ?: ""
            ru.contains(sourceSearch, ignoreCase = true) ||
                    en.contains(sourceSearch, ignoreCase = true)
        }
    }

    val selectedSourcesRu = remember {
        mutableStateListOf<String>().apply {
            addAll(filter.selectedSources.mapNotNull { sourceMap[it.lowercase()] })
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp)) // Добавлен clip для закругления углов
    ) {
        Text("Фильтр", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = cityText,
            onValueChange = {
                cityText = it
                onFilterChange(filter.copy(city = it.ifBlank { null }))
            },
            label = { Text("Город") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = sourcesDropdownExpanded,
            onExpandedChange = { sourcesDropdownExpanded = !sourcesDropdownExpanded }
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                value = if (selectedSourcesRu.isEmpty())
                    "" else selectedSourcesRu.joinToString(", "),
                onValueChange = {},
                readOnly = true,
                label = { Text("Источник") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(sourcesDropdownExpanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors()
            )
            ExposedDropdownMenu(
                expanded = sourcesDropdownExpanded,
                onDismissRequest = { sourcesDropdownExpanded = false }
            ) {
                OutlinedTextField(
                    value = sourceSearch,
                    onValueChange = { sourceSearch = it },
                    label = { Text("Поиск источника") },
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                )
                filteredSources.forEach { srcRu ->
                    val srcEn = reverseSourceMap[srcRu] ?: ""
                    val selected =
                        filter.selectedSources.any { it.equals(srcEn, ignoreCase = true) }
                    DropdownMenuItem(
                        onClick = {
                            val updated = filter.selectedSources.toMutableSet().apply {
                                if (selected) remove(srcEn) else add(srcEn)
                            }
                            onFilterChange(filter.copy(selectedSources = updated))

                            if (selected) {
                                selectedSourcesRu.remove(srcRu)
                            } else {
                                selectedSourcesRu.add(srcRu)
                            }
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = selected, onCheckedChange = null)
                                Spacer(Modifier.width(8.dp))
                                Text(srcRu)
                            }
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        AnimatedVisibility(visible = expanded) {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = minPriceText,
                        onValueChange = {
                            minPriceText = it
                            onFilterChange(filter.copy(minPrice = it.toBigDecimalOrNull()))
                        },
                        label = { Text("Мин. цена") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = maxPriceText,
                        onValueChange = {
                            maxPriceText = it
                            onFilterChange(filter.copy(maxPrice = it.toBigDecimalOrNull()))
                        },
                        label = { Text("Макс. цена") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(Modifier.height(12.dp))

                val allRooms = listOf(0, 1, 2, 3, 4, 5)

                ExposedDropdownMenuBox(
                    expanded = roomsDropdownExpanded,
                    onExpandedChange = { roomsDropdownExpanded = !roomsDropdownExpanded }
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        value = if (filter.selectedRooms.isEmpty()) "Все" else {
                            filter.selectedRooms.sorted().joinToString(", ") {
                                when (it) {
                                    0 -> "Студия"
                                    6 -> "5+"
                                    else -> "$it"
                                }
                            }
                        },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Комнаты") },
                        trailingIcon = {
                            Icon(
                                imageVector = if (roomsDropdownExpanded)
                                    Icons.Default.KeyboardArrowUp
                                else Icons.Default.KeyboardArrowDown,
                                contentDescription = null
                            )
                        },
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = roomsDropdownExpanded,
                        onDismissRequest = { roomsDropdownExpanded = false }
                    ) {
                        allRooms.forEach { r ->
                            val roomName = when (r) {
                                0 -> "Студия"
                                else -> "$r"
                            }
                            val selected = r in filter.selectedRooms
                            DropdownMenuItem(
                                onClick = {
                                    val updated = filter.selectedRooms.toMutableSet().apply {
                                        if (selected) remove(r) else add(r)
                                    }
                                    onFilterChange(filter.copy(selectedRooms = updated))
                                },
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(checked = selected, onCheckedChange = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text(roomName)
                                    }
                                }
                            )
                        }
                        DropdownMenuItem(
                            onClick = {
                                val updated = filter.selectedRooms.toMutableSet().apply {
                                    if (contains(6)) remove(6) else add(6)
                                }
                                onFilterChange(filter.copy(selectedRooms = updated))
                            },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = filter.selectedRooms.contains(6),
                                        onCheckedChange = null
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("5+")
                                }
                            }
                        )
                    }

                }
            }
        }


        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = if (expanded)
                    Icons.Default.KeyboardArrowUp
                else Icons.Default.KeyboardArrowDown,
                contentDescription = null
            )
            Spacer(Modifier.width(4.dp))
            Text(if (expanded) "Свернуть" else "Показать ещё")
        }
    }
}
