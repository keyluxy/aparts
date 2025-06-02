package com.example.apartapp.domain.model

data class ListingsFilter(
    val city: String? = null,
    val minPrice: java.math.BigDecimal? = null,
    val maxPrice: java.math.BigDecimal? = null,
    val selectedRooms: Set<Int> = emptySet(),
    val selectedSources: Set<String> = emptySet()
)
