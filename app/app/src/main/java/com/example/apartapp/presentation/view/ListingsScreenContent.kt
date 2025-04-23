package com.example.apartapp.presentation.view


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.apartapp.domain.model.Listing
import com.example.apartapp.domain.model.ListingsFilter
import com.example.apartapp.presentation.view.filter.FilterSection

@Composable
fun ListingsScreenContent(
    listings: List<Listing>,
    favoriteIds: Set<Int>,
    isLoading: Boolean,
    errorMessage: String?,
    filters: ListingsFilter,
    onFilterChange: (ListingsFilter) -> Unit,
    onFavoriteToggle: (Listing) -> Unit,
    scrollState: LazyListState,
    paddingValues: PaddingValues = PaddingValues(0.dp)
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        //Фильтры
        FilterSection(
            filter = filters,
            onFilterChange = onFilterChange
        )

        //Контент
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = errorMessage ?: "Ошибка")
                }
            }

            listings.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Нет объявлений")
                }
            }

            else -> {
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(listings) { listing ->
                        ListingCard(
                            listing = listing,
                            isFavorite = favoriteIds.contains(listing.id),
                            onFavoriteClick = onFavoriteToggle
                        )
                    }
                }
            }
        }
    }
}





@Composable
fun PagerIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(pagerState.pageCount) { iteration ->
            val color = if (pagerState.currentPage == iteration)
                MaterialTheme.colorScheme.primary
            else
                Color.LightGray
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(color)
                    .size(8.dp)
            )
        }
    }
}
