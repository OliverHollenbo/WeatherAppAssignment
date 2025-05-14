package com.group13.weatherappfirstassignment.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.group13.weatherappfirstassignment.network.DmiRepository
import com.group13.weatherappfirstassignment.network.StationFeature
import com.group13.weatherappfirstassignment.ui.components.AppDrawer
import com.group13.weatherappfirstassignment.ui.components.AppTopBar
import com.group13.weatherappfirstassignment.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun FavoritesScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    val favoriteIds by viewModel.favoriteStationIds.collectAsState()
    var allStations by remember { mutableStateOf<List<StationFeature>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadFavorites()
        scope.launch {
            try {
                val response = DmiRepository().getStations()
                if (response.isSuccessful) {
                    allStations = response.body()?.features ?: emptyList()
                } else {
                    errorMessage = "DMI API Error: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error fetching stations: ${e.message}"
            }
            isLoading = false
        }
    }

    val favoriteStations = allStations.filter { it.properties.stationId in favoriteIds }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            AppTopBar(
                title = "Favorite Stations",
                onMenuClick = { scope.launch { scaffoldState.drawerState.open() } },
                onLogoutClick = {
                    viewModel.logout {
                        navController.navigate("login") {
                            popUpTo("favorites") { inclusive = true }
                        }
                    }
                }
            )
        },
        drawerContent = {
            AppDrawer(
                navController = navController,
                scope = scope,
                scaffoldState = scaffoldState,
                onLogout = {
                    viewModel.logout {
                        navController.navigate("login") {
                            popUpTo("favorites") { inclusive = true }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                errorMessage != null -> Text(
                    text = errorMessage ?: "Unknown error",
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colors.error
                )

                favoriteStations.isEmpty() -> Text(
                    "No favorites found.",
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(favoriteStations) { station ->
                        val stationId = station.properties.stationId ?: ""
                        val isFavorited = favoriteIds.contains(stationId)

                        Card(
                            elevation = 4.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            navController.navigate("stationDetail/$stationId")
                                        }
                                ) {
                                    Text("Station ID: $stationId")
                                    Text("Name: ${station.properties.name}")
                                }

                                IconButton(onClick = {
                                    if (isFavorited) {
                                        viewModel.removeStationFromFavorites(stationId)
                                    } else {
                                        viewModel.addStationToFavorites(stationId)
                                    }
                                }) {
                                    Icon(
                                        imageVector = if (isFavorited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = if (isFavorited) "Remove from favorites" else "Add to favorites"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
