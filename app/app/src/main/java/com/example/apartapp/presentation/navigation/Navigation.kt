package com.example.apartapp.presentation.navigation

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.apartapp.presentation.view.AuthScreen
import com.example.apartapp.presentation.view.FavoritesScreen
import com.example.apartapp.presentation.view.ListingDetailScreen
import com.example.apartapp.presentation.view.ListingsScreenContent
import com.example.apartapp.presentation.viewmodel.FavoritesViewModel
import com.example.apartapp.presentation.viewmodel.ListingsViewModel

sealed class Screen(val route: String) {
    data object Auth : Screen("auth")
    data object Listings : Screen("listings")
    data object Favorites : Screen("favorites")
    data object ListingDetails : Screen("listing_details/{listingId}") {
        fun createRoute(listingId: Int) = "listing_details/$listingId"
    }
}

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val bottomNavController = rememberNavController()


    NavHost(
        navController = navController,
        startDestination = Screen.Auth.route
    ) {
        // Экран авторизации
        composable(Screen.Auth.route) {
            val authVM = hiltViewModel<com.example.apartapp.presentation.viewmodel.AuthViewModel>()
            AuthScreen(
                authViewModel = authVM,
                onAuthSuccess = { userId ->
                    navController.navigate("listings/$userId") {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        // Главный граф после логина
        composable(
            route = "listings/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { back ->
            val userId = back.arguments!!.getInt("userId")
            val listingsVM = hiltViewModel<ListingsViewModel>()
            val favoritesVM = hiltViewModel<FavoritesViewModel>()

            LaunchedEffect(userId) {
                listingsVM.setUserId(userId)
            }

            BottomNavGraph(
                listingsViewModel = listingsVM,
                favoritesViewModel = favoritesVM,
                userId = userId,
                navController = navController,
                bottomNavController = bottomNavController
            )

        }

        // Экран деталей объявления
        composable(
            route = Screen.ListingDetails.route,
            arguments = listOf(navArgument("listingId") { type = NavType.IntType })
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getInt("listingId") ?: 0
            val listingsVM = hiltViewModel<ListingsViewModel>()
//            val navController = rememberNavController() // или получите из внешнего контекста

            // Состояния из ViewModel
            val listingsState by listingsVM.listings.collectAsState()
            val isLoading by listingsVM.isLoading.collectAsState()
            val errorMessage by listingsVM.errorMessage.collectAsState()

            // Пытаемся найти объявление по ID
            val listing = listingsState.find { it.id == listingId }

            when {
                isLoading -> {
                    // Показать индикатор загрузки
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                listing != null -> {
                    // Если объявление найдено — показываем экран деталей
                    ListingDetailScreen(
                        listing = listing,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                errorMessage != null -> {
                    // Ошибка загрузки — показываем сообщение и кнопку назад
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Ошибка: $errorMessage")
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { navController.popBackStack() }) {
                                Text("Назад")
                            }
                        }
                    }
                }

                else -> {
                    // Объявление не найдено, но загрузка не идёт и ошибок нет
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Объявление не найдено")
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { navController.popBackStack() }) {
                                Text("Назад")
                            }
                        }
                    }
                }
            }
        }

    }

}

@Composable
fun BottomNavGraph(
    listingsViewModel: ListingsViewModel,
    favoritesViewModel: FavoritesViewModel,
    userId: Int,
    navController: NavHostController,
    bottomNavController: NavHostController
)
 {
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    LaunchedEffect(currentRoute) {
        if (currentRoute == Screen.Favorites.route) {
            favoritesViewModel.loadFavorites(userId)
        }
    }

    Scaffold(
        bottomBar = { ApartAppBottomBar(navController = bottomNavController) }
    ) { padding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Screen.Listings.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Listings.route) {
                val listings by listingsViewModel.listings.collectAsState()
                val favoriteIds by listingsViewModel.favoriteIds.collectAsState()
                val isLoading by listingsViewModel.isLoading.collectAsState()
                val errorMessage by listingsViewModel.errorMessage.collectAsState()
                val filters by listingsViewModel.filters.collectAsState()
                val scrollState = rememberLazyListState()

                ListingsScreenContent(
                    listings = listings,
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    filters = filters,
                    favoriteIds = favoriteIds,
                    onFilterChange = { listingsViewModel.updateFilters(it) },
                    onFavoriteToggle = { listing ->
                        listingsViewModel.toggleFavorite(listing)
                    },
                    scrollState = scrollState,
                    onListingClick = { listingId ->
                        navController.navigate(Screen.ListingDetails.createRoute(listingId))
                    }
                )
            }

            composable(Screen.Favorites.route) {
                val favorites by favoritesViewModel.favorites.collectAsState()
                val isLoadingF by favoritesViewModel.isLoading.collectAsState()

                FavoritesScreen(
                    favorites = favorites,
                    isLoading = isLoadingF,
                    onFavoriteToggle = { listing ->
                        favoritesViewModel.removeFavoriteAndRefresh(userId, listing.id)
                    },
                    onListingClick = { listingId ->
                        navController.navigate(Screen.ListingDetails.createRoute(listingId))
                    },
                    onBackClick = {
                        bottomNavController.navigate(Screen.Listings.route) {
                            popUpTo(Screen.Listings.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    }


                )

            }
        }
    }
}

@Composable
fun ApartAppBottomBar(navController: NavHostController) {
    val items = listOf(Screen.Listings, Screen.Favorites)
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 8.dp
    ) {
        items.forEach { screen ->
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
                        imageVector = if (screen == Screen.Listings) Icons.Filled.List else Icons.Filled.Favorite,
                        contentDescription = null
                    )
                },
                label = { Text(if (screen == Screen.Listings) "Listings" else "Favorites") },
                alwaysShowLabel = false
            )
        }
    }
}

