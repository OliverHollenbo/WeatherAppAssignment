package com.group13.weatherappfirstassignment.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder

@Composable
fun HomeScreen(navController: NavController, viewModel: AuthViewModel) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var stations by remember { mutableStateOf<List<StationFeature>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val favorites by viewModel.favoriteStationIds.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFavorites()
        scope.launch {
            try {
                val response = DmiRepository().getStations()
                if (response.isSuccessful) {
                    stations = response.body()?.features ?: emptyList()
                } else {
                    error = "Failed to load stations: ${response.code()}"
                }
            } catch (e: Exception) {
                error = "Error: ${e.message}"
            }
            isLoading = false
        }
    }

    val filteredStations = stations.filter {
        it.properties.name.contains(searchQuery, ignoreCase = true) ||
                it.properties.stationId.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            AppTopBar(
                title = "Home",
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
                            popUpTo("home") { inclusive = true }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Stations...") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                error != null -> Text(
                    text = error ?: "Unknown error",
                    color = MaterialTheme.colors.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredStations) { station ->
                        val stationId = station.properties.stationId ?: ""
                        val isFavorited = favorites.contains(stationId)

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
                                        imageVector = if (isFavorited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = if (isFavorited) "Unfavorite" else "Favorite"
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