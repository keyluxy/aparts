package com.example.apartapp.domain.model

import java.math.BigDecimal

data class ListingsFilter(
    val minPrice: BigDecimal? = null,
    val maxPrice: BigDecimal? = null,
    val minRooms: Int? = null,
    val maxRooms: Int? = null,
    val city: String? = null
)