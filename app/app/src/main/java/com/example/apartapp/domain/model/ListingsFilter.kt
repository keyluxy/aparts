package com.example.apartapp.domain.model

import java.math.BigDecimal

data class ListingsFilter(
    val city: String? = null,
    val minPrice: BigDecimal? = null,
    val maxPrice: BigDecimal? = null,
    val selectedRooms: Set<Int> = emptySet(),
    val selectedSources: Set<String> = emptySet()
)
