package com.example.apartapp.presentation.navigation

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.apartapp.presentation.view.AdminScreen
import com.example.apartapp.presentation.view.AuthScreen
import com.example.apartapp.presentation.view.FavoritesScreen
import com.example.apartapp.presentation.view.ListingDetailScreen
import com.example.apartapp.presentation.view.ListingsScreenContent
import com.example.apartapp.presentation.viewmodel.FavoritesViewModel
import com.example.apartapp.presentation.viewmodel.ListingsViewModel
import com.example.apartapp.presentation.viewmodel.AdminViewModel
import com.example.apartapp.presentation.view.ProfileScreen
import com.example.apartapp.presentation.viewmodel.UserViewModel

sealed class Screen(val route: String) {
    data object Auth : Screen("auth")
    data object Listings : Screen("listings")
    data object Favorites : Screen("favorites")
    data object Admin : Screen("admin")
    data object Profile : Screen("profile")
    data object ListingDetails : Screen("listing_details/{userId}/{listingId}") {
        fun createRoute(userId: Int, listingId: Int) = "listing_details/$userId/$listingId"
    }
}

@SuppressLint("UnrememberedMutableState")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Navigation() {
    val navController = rememberNavController()

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
                    Log.d("Navigation", "Auth success, userId: $userId")
                    navController.navigate("main/$userId") {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        // Главный граф после логина
        composable(
            route = "main/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments!!.getInt("userId")
            Log.d("Navigation", "Entering main graph, userId: $userId")
            
            val listingsVM = hiltViewModel<ListingsViewModel>()
            val userVM = hiltViewModel<UserViewModel>()
            val adminVM = hiltViewModel<AdminViewModel>()
            val userInfo by userVM.userInfo.collectAsState()
            val isLoading by userVM.isLoading.collectAsState()

            // Загружаем информацию о пользователе через UserRepository
            LaunchedEffect(userId) {
                Log.d("Navigation", "Setting up user data, userId: $userId")
                userVM.loadUserInfo()
            }

            if (isLoading) {
                Log.d("Navigation", "Loading user data...")
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // После получения информации о пользователе проверяем админский статус
                LaunchedEffect(userInfo) {
                    userInfo?.let { info ->
                        Log.d("Navigation", "User info received: $info")
                        if (info.isAdmin) {
                            Log.d("Navigation", "User is admin, checking admin status...")
                            adminVM.checkAdminStatus()
                        }
                        // Устанавливаем userId для ListingsViewModel в любом случае
                        listingsVM.setUserId(userId)
                        // Переходим на экран объявлений
                        Log.d("Navigation", "Navigating to Listings screen")
                        navController.navigate(Screen.Listings.route) {
                            popUpTo("main/$userId") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }

                // Если пользователь админ, ждем подтверждения админского статуса
                val isAdmin by adminVM.isAdmin.collectAsState()
                val isAdminLoading by adminVM.isLoading.collectAsState()

                if (userInfo?.isAdmin == true && isAdminLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (userInfo?.isAdmin == true && isAdmin) {
                    LaunchedEffect(Unit) {
                        Log.d("Navigation", "Navigating to Admin screen")
                        navController.navigate(Screen.Admin.route) {
                            popUpTo("main/$userId") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            }
        }

        // Экран списка объявлений
        composable(Screen.Listings.route) {
            val listingsVM = hiltViewModel<ListingsViewModel>()
            val userVM = hiltViewModel<UserViewModel>()
            val userInfo by userVM.userInfo.collectAsState()
            val userId = userInfo?.id ?: 0

            Log.d("Navigation", "ListingsScreen: userInfo=$userInfo, userId=$userId")

            // Загружаем информацию о пользователе, если её ещё нет
            LaunchedEffect(Unit) {
                if (userInfo == null) {
                    Log.d("Navigation", "ListingsScreen: Loading user info...")
                    userVM.loadUserInfo()
                }
            }

            Scaffold(
                bottomBar = {
                    if (userInfo?.isAdmin == true) {
                        AdminBottomBar(navController = navController)
                    } else {
                        UserBottomBar(navController = navController)
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    val listings by listingsVM.listings.collectAsState()
                    val favoriteIds by listingsVM.favoriteIds.collectAsState()
                    val isLoading by listingsVM.isLoading.collectAsState()
                    val errorMessage by listingsVM.errorMessage.collectAsState()
                    val filters by listingsVM.filters.collectAsState()
                    val scrollState = rememberLazyListState()

                    // Загружаем объявления только если у нас есть userId
                    LaunchedEffect(userId) {
                        Log.d("Navigation", "ListingsScreen: LaunchedEffect userId=$userId")
                        if (userId != 0) {
                            Log.d("Navigation", "ListingsScreen: Fetching listings...")
                            listingsVM.setUserId(userId)
                        }
                    }

                    ListingsScreenContent(
                        listings = listings,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        filters = filters,
                        favoriteIds = favoriteIds,
                        onFilterChange = { listingsVM.updateFilters(it) },
                        onFavoriteToggle = { listing -> listingsVM.toggleFavorite(listing) },
                        scrollState = scrollState,
                        onListingClick = { listingId -> 
                            navController.navigate(Screen.ListingDetails.createRoute(userId, listingId))
                        }
                    )
                }
            }
        }

        // Экран избранного
        composable(Screen.Favorites.route) {
            val favoritesVM = hiltViewModel<FavoritesViewModel>()
            val adminVM = hiltViewModel<AdminViewModel>()
            val userVM = hiltViewModel<UserViewModel>()
            val isAdmin by adminVM.isAdmin.collectAsState()
            val userInfo by userVM.userInfo.collectAsState()
            val userId = userInfo?.id ?: 0

            LaunchedEffect(Unit) {
                userVM.loadUserInfo()
            }

            Scaffold(
                bottomBar = {
                    if (isAdmin) {
                        AdminBottomBar(navController = navController)
                    } else {
                        UserBottomBar(navController = navController)
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    val favorites by favoritesVM.favorites.collectAsState()
                    val isLoading by favoritesVM.isLoading.collectAsState()

                    LaunchedEffect(userId) {
                        if (userId != 0) {
                            favoritesVM.loadFavorites(userId)
                        }
                    }

                    FavoritesScreen(
                        favorites = favorites,
                        isLoading = isLoading,
                        onFavoriteToggle = { listing -> favoritesVM.removeFavoriteAndRefresh(userId, listing.id) },
                        onListingClick = { listingId -> 
                            navController.navigate(Screen.ListingDetails.createRoute(userId, listingId))
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }

        // Экран админа
        composable(Screen.Admin.route) {
            val adminVM = hiltViewModel<AdminViewModel>()
            val isAdmin by adminVM.isAdmin.collectAsState()

            // Убираем повторную проверку статуса админа, так как мы уже на экране админа
            Scaffold(
                bottomBar = {
                    AdminBottomBar(navController = navController)
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    AdminScreen(
                        adminViewModel = adminVM,
                        onBackClick = { navController.popBackStack() },
                        onLogout = {
                            navController.navigate(Screen.Auth.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }

        // Экран профиля
        composable(Screen.Profile.route) {
            val adminVM = hiltViewModel<AdminViewModel>()
            val userVM = hiltViewModel<UserViewModel>()
            val isAdmin by adminVM.isAdmin.collectAsState()

            LaunchedEffect(Unit) {
                userVM.loadUserInfo()
            }

            Scaffold(
                bottomBar = {
                    if (isAdmin) {
                        AdminBottomBar(navController = navController)
                    } else {
                        UserBottomBar(navController = navController)
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    ProfileScreen(
                        userInfo = userVM.userInfo.collectAsState().value,
                        onLogout = {
                            navController.navigate(Screen.Auth.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onBackClick = { navController.popBackStack() },
                        isLoading = userVM.isLoading.collectAsState().value,
                        adminViewModel = adminVM // Передаем ViewModel напрямую
                    )
                }
            }
        }

        // Экран деталей объявления
        composable(
            route = Screen.ListingDetails.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType },
                navArgument("listingId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val listingId = backStackEntry.arguments?.getInt("listingId") ?: 0
            val listingsVM = hiltViewModel<ListingsViewModel>()
            val selectedListing by listingsVM.selectedListing.collectAsState()
            val isLoading by listingsVM.isLoading.collectAsState()
            val errorMessage by listingsVM.errorMessage.collectAsState()
            val favoriteIds by listingsVM.favoriteIds.collectAsState()

            // Устанавливаем userId для ListingsViewModel
            LaunchedEffect(userId) {
                Log.d("Navigation", "ListingDetails: Setting userId: $userId")
                listingsVM.setUserId(userId)
            }

            LaunchedEffect(listingId) {
                listingsVM.getListingById(listingId)
            }

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                selectedListing != null -> {
                    ListingDetailScreen(
                        listing = selectedListing!!,
                        isFavorite = selectedListing!!.id in favoriteIds,
                        onFavoriteClick = { listing -> 
                            Log.d("Navigation", "ListingDetails: Favorite clicked for listing ${listing.id}")
                            listingsVM.toggleFavorite(listing)
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }
                errorMessage != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Ошибка: $errorMessage")
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
fun AdminBottomBar(navController: NavHostController) {
    val items = listOf(Screen.Listings, Screen.Favorites, Screen.Admin)
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
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = when (screen) {
                            Screen.Listings -> Icons.Filled.List
                            Screen.Favorites -> Icons.Filled.Favorite
                            Screen.Admin -> Icons.Filled.AccountCircle
                            else -> Icons.Filled.List
                        },
                        contentDescription = null
                    )
                },
                label = {
                    Text(
                        when (screen) {
                            Screen.Listings -> "Объявления"
                            Screen.Favorites -> "Избранное"
                            Screen.Admin -> "Админ"
                            else -> ""
                        }
                    )
                },
                alwaysShowLabel = false
            )
        }
    }
}

@Composable
fun UserBottomBar(navController: NavHostController) {
    val items = listOf(Screen.Listings, Screen.Favorites, Screen.Profile)
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
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = when (screen) {
                            Screen.Listings -> Icons.Filled.List
                            Screen.Favorites -> Icons.Filled.Favorite
                            Screen.Profile -> Icons.Filled.Person
                            else -> Icons.Filled.List
                        },
                        contentDescription = null
                    )
                },
                label = {
                    Text(
                        when (screen) {
                            Screen.Listings -> "Объявления"
                            Screen.Favorites -> "Избранное"
                            Screen.Profile -> "Профиль"
                            else -> ""
                        }
                    )
                },
                alwaysShowLabel = false
            )
        }
    }
}

