package com.example.apartapp.presentation.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.apartapp.domain.model.Listing

@Composable
fun FavoritesScreen(
    favorites: List<Listing>,
    isLoading: Boolean,
    onFavoriteToggle: (Listing) -> Unit
) {
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (favorites.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Нет избранных объявлений")
        }
    } else {
        LazyColumn {
            items(favorites) { listing ->
                ListingCard(listing = listing, isFavorite = true, onFavoriteClick = onFavoriteToggle)
            }
        }
    }
}