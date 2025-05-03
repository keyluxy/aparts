package com.example.apartapp.presentation.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.example.apartapp.domain.model.Listing
import kotlinx.coroutines.launch

@Composable
fun ListingsList(
    listings: List<Listing>,
    favoriteIds: Set<Int>,
    onFavoriteToggle: (Listing) -> Unit,
    scrollState: LazyListState,
    onListingClick: (Int) -> Unit
) {
    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = listings,
            key = { it.id }
        ) { listing ->
            ListingCard(
                listing = listing,
                isFavorite = favoriteIds.contains(listing.id),
                onFavoriteClick = { onFavoriteToggle(it) },
                onListingClick = { onListingClick(listing.id) }
            )
        }
    }
}


