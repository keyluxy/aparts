package com.example.apartapp.presentation.navigation

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.apartapp.domain.model.Listing
import com.example.apartapp.presentation.view.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.apartapp.presentation.viewmodel.FavoritesViewModel
import com.example.apartapp.presentation.viewmodel.ListingsViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import com.example.apartapp.presentation.view.filter.FilterSection

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Main : Screen("main") // Главный экран с навигацией
    object Listings : Screen("listings")
    object Favorites : Screen("favorites")
}

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Auth.route) {
        composable(Screen.Auth.route) {
            AuthScreen(
                onAuthSuccess = {
                    // После успешной авторизации переходим на главный экран
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Main.route) {
            // Передаем userId на главный экран (пока заглушка)
            MainScreen(userId = 1)
        }
    }
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(
    userId: Int,
    listingsViewModel: ListingsViewModel = hiltViewModel(),
    favoritesViewModel: FavoritesViewModel = hiltViewModel(),
    navController: NavHostController = rememberNavController()
) {
    LaunchedEffect(Unit) {
        listingsViewModel.setUserId(userId)
    }

    Scaffold(
        bottomBar = {
            ApartAppBottomBar(navController = navController)
        }
    ) { _ ->
        BottomNavGraph(
            navController = navController,
            listingsViewModel = listingsViewModel,
            favoritesViewModel = favoritesViewModel,
            userId = userId
        )
    }
}


@Composable
fun ApartAppBottomBar(navController: NavHostController) {

    val screens = listOf(
        Screen.Listings,
        Screen.Favorites,
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 8.dp
    ) {
        screens.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (screen.route == Screen.Listings.route) {
                            Icons.Filled.List
                        } else {
                            Icons.Filled.Favorite
                        },
                        contentDescription = "Navigation Icon"
                    )
                },
                label = {
                    Text(
                        text = if (screen.route == Screen.Listings.route) {
                            "Listings"
                        } else {
                            "Favorites"
                        }
                    )
                },
                alwaysShowLabel = false
            )
        }
    }
}

@Composable
fun BottomNavGraph(navController: NavHostController, listingsViewModel: ListingsViewModel, favoritesViewModel: FavoritesViewModel, userId: Int) {
    NavHost(
        navController = navController,
        startDestination = Screen.Listings.route
    ) {
        composable(route = Screen.Listings.route) {
            //Переносим код из ListingsScreen
            val listings by listingsViewModel.listings.collectAsState()
            val favoriteIds by listingsViewModel.favoriteIds.collectAsState()
            val isLoading by listingsViewModel.isLoading.collectAsState()
            val errorMessage by listingsViewModel.errorMessage.collectAsState()
            val filters by listingsViewModel.filters.collectAsState()

            val scrollState = rememberLazyListState()

            val isFilterVisible by remember {
                derivedStateOf {
                    scrollState.firstVisibleItemIndex == 0
                }
            }

            LaunchedEffect(isFilterVisible) {
                Log.d("ListingsScreen", "isFilterVisible: $isFilterVisible")
            }

            /*
              Вызываем ListingsScreenContent и передаём все
              необходимые параметры
            */
            ListingsScreenContent(
                listings = listings,
                favoriteIds = favoriteIds,
                isLoading = isLoading,
                errorMessage = errorMessage,
                filters = filters,
                onFilterChange = { listingsViewModel.updateFilters(it) },
                onFavoriteToggle = { listingsViewModel.toggleFavorite(it) },
                scrollState = scrollState
            )
        }

        composable(route = Screen.Favorites.route) {
            LaunchedEffect(Unit) {
                favoritesViewModel.loadFavorites(userId)
            }
            val favorites by favoritesViewModel.favorites.collectAsState()
            val isLoading by favoritesViewModel.isLoading.collectAsState()

            FavoritesScreen(favorites = favorites, isLoading = isLoading, onFavoriteToggle = { listing ->
                // Удаляем из избранного
            })
        }
    }
}








