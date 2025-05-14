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
fun NearbyStationsScreen(navController: NavController, viewModel: AuthViewModel) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val repository = remember { DmiRepository() }

    var stations by remember { mutableStateOf<List<StationFeature>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val favorites = viewModel.favoriteStationIds.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        viewModel.loadFavorites()
        scope.launch {
            try {
                val response = repository.getStations()
                if (response.isSuccessful) {
                    stations = response.body()?.features ?: emptyList()
                } else {
                    errorMessage = "Error: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Exception: ${e.message}"
            }
            isLoading = false
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            AppTopBar(
                title = "Nearby Stations",
                onMenuClick = { scope.launch { scaffoldState.drawerState.open() } }
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
                            popUpTo("nearby") { inclusive = true }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {

            when {
                isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                errorMessage != null -> Text(
                    text = errorMessage ?: "Unknown error",
                    color = MaterialTheme.colors.error,
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(stations) { station ->
                        val stationId = station.properties.stationId ?: ""
                        val isFavorited = favorites.value.contains(stationId)

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
                                            if (stationId.isNotBlank()) {
                                                navController.navigate("stationDetail/$stationId")
                                            }
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
                                        imageVector = if (isFavorited)
                                            Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = if (isFavorited)
                                            "Remove from favorites" else "Add to favorites"
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
