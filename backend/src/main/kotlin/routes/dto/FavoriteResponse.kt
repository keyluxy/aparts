// FavoriteResponse.kt
package com.example.routes.dto

import kotlinx.serialization.Serializable

@Serializable
data class FavoriteResponse(
    val userId: Int,
    val listingId: Int
)

@Serializable
data class FavoritesListResponse(
    val favorites: List<FavoriteResponse>
)
