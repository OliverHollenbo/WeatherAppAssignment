package com.group13.weatherappfirstassignment.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.group13.weatherappfirstassignment.network.DmiRepository
import com.group13.weatherappfirstassignment.network.StationFeature
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import com.group13.weatherappfirstassignment.ui.components.AppTopBar
import com.group13.weatherappfirstassignment.viewmodels.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@Composable
fun NearbyStationsScreen(navController: NavController, viewModel: AuthViewModel) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val repository = remember { DmiRepository() }

    var stations by remember { mutableStateOf<List<StationFeature>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val response = repository.getStations()
                if (response.isSuccessful) {
                    stations = response.body()?.features ?: emptyList()
                    isLoading = false
                } else {
                    errorMessage = "Error: ${response.code()}"
                    isLoading = false
                }
            } catch (e: Exception) {
                errorMessage = "Exception: ${e.message}"
                isLoading = false
            }
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
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Menu", style = MaterialTheme.typography.h6)

                Spacer(modifier = Modifier.height(24.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = {
                    navController.navigate("home")
                    scope.launch { scaffoldState.drawerState.close() }
                }) {
                    Text("Home")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = {
                    viewModel.logout {
                        navController.navigate("login") {
                            popUpTo("nearby") { inclusive = true }
                        }
                    }
                }) {
                    Text("Logout")
                }
            }
        },
        content = { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
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
                            Card(
                                elevation = 4.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Station ID: ${station.properties.stationId}")
                                    Text("Name: ${station.properties.name}")
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
