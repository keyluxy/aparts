package com.example.apartapp.presentation.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    ) {
        AnimatedVisibility(visible = filterVisibility) {
            FilterSection(
                filter = filters,
                onFilterChange = onFilterChange
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
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorPlaceholder(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Ошибка: $message")
    }
}

@Composable
fun EmptyPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Нет данных")
    }
}
