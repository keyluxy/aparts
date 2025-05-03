package com.example.apartapp.presentation.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.example.apartapp.domain.model.Listing
import com.example.apartapp.domain.model.ListingsFilter
import com.example.apartapp.presentation.view.filter.FilterSection

@Composable
fun ListingsScreenContent(
    listings: List<Listing>,
    isLoading: Boolean,
    errorMessage: String?,
    filters: ListingsFilter,
    favoriteIds: Set<Int>,
    onFilterChange: (ListingsFilter) -> Unit,
    onFavoriteToggle: (Listing) -> Unit,
    scrollState: LazyListState,
    onListingClick: (Int) -> Unit,
    paddingValues: PaddingValues = PaddingValues(0.dp)
) {
    val isScrolledToTop = remember { derivedStateOf { scrollState.firstVisibleItemIndex == 0 } }
    var filterVisibility by remember { mutableStateOf(true) }

    LaunchedEffect(isScrolledToTop.value) {
        filterVisibility = isScrolledToTop.value
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        AnimatedVisibility(visible = filterVisibility) {
            FilterSection(
                filter = filters,
                onFilterChange = onFilterChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                    .background(MaterialTheme.colorScheme.surface)
            )
        }

        when {
            isLoading -> LoadingPlaceholder()
            errorMessage != null -> ErrorPlaceholder(message = errorMessage)
            listings.isEmpty() -> EmptyPlaceholder()
            else -> {
                ListingsList(
                    listings = listings,
                    favoriteIds = favoriteIds,
                    onFavoriteToggle = onFavoriteToggle,
                    scrollState = scrollState,
                    onListingClick = onListingClick
                )
            }
        }
    }
}

@Composable
fun LoadingPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorPlaceholder(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Ошибка: $message",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun EmptyPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Нет данных",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}
