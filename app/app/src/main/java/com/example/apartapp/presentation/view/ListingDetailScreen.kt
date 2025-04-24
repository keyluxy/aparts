package com.example.apartapp.presentation.view

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.apartapp.domain.model.Listing

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListingDetailScreen(
    listing: Listing,
    onBackClick: () -> Unit // Добавляем параметр для кнопки назад
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val pagerState = rememberPagerState(pageCount = { listing.imageUrls.size })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                AsyncImage(
                    model = listing.imageUrls[page],
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Кнопка назад поверх изображений, слева сверху
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.TopStart)
                    .background(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small
                    )
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Назад",
                    tint = Color.White
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(listing.imageUrls.size) { index ->
                val color = if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = listing.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\$${listing.price}",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Город: ${listing.city ?: "не указан"}")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Количество комнат: ${listing.rooms ?: "не указано"}")
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Описание:")
            Text(text = listing.description ?: "Описание отсутствует")
            Spacer(modifier = Modifier.height(8.dp))
            if (!listing.sourceName.isNullOrEmpty()) {
                Text(text = "Источник: ${listing.sourceName}")
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (!listing.url.isNullOrEmpty()) {
                Button(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(listing.url))
                    context.startActivity(intent)
                }) {
                    Text("Перейти к объявлению")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
