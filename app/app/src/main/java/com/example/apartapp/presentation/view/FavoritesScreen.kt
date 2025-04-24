package com.example.apartapp.presentation.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.apartapp.domain.model.Listing

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.unit.dp

@Composable
fun FavoritesScreen(
    favorites: List<Listing>,
    isLoading: Boolean,
    onFavoriteToggle: (Listing) -> Unit,
    onListingClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            favorites.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Нет избранных объявлений")
                }
            }
            else -> {
                LazyColumn {
                    items(favorites) { listing ->
                        ListingCard(
                            listing = listing,
                            isFavorite = true,
                            onFavoriteClick = onFavoriteToggle,
                            onListingClick = { onListingClick(listing.id) }
                        )
                    }
                }
            }
        }

        // Иконка назад в стиле Material Design
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Назад"
            )
        }
    }
}
