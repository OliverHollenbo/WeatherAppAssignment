package com.group13.weatherappfirstassignment.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.group13.weatherappfirstassignment.model.Feature
import com.group13.weatherappfirstassignment.model.parameterInfo
import com.group13.weatherappfirstassignment.network.DmiRepository
import com.group13.weatherappfirstassignment.ui.components.AppDrawer
import com.group13.weatherappfirstassignment.ui.components.AppTopBar
import com.group13.weatherappfirstassignment.viewmodels.AuthViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StationDetailScreen(
    navController: NavController,
    stationId: String,
    viewModel: AuthViewModel
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val repository = remember { DmiRepository() }
    val context = LocalContext.current

    var observations by remember { mutableStateOf<List<Feature>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val favorites by viewModel.favoriteStationIds.collectAsState()

    fun formatTimestamp(raw: String?): String {
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = isoFormat.parse(raw ?: "") ?: return "Unknown time"
            val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            outputFormat.format(date)
        } catch (e: Exception) {
            "Unknown time"
        }
    }

    LaunchedEffect(stationId) {
        scope.launch {
            try {
                val response = repository.getLatestObservationsForStation(stationId)
                if (response.isSuccessful) {
                    observations = response.body()?.features ?: emptyList()
                } else {
                    errorMessage = "Error: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Exception: ${e.message}"
            }
            isLoading = false
        }
    }

    val groupedObservations = observations
        .filter { it.properties.parameterId != null && it.properties.value != null }
        .groupBy { it.properties.parameterId!! }

    val isFavorited = favorites.contains(stationId)

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            AppTopBar(
                title = "Station $stationId",
                onMenuClick = { scope.launch { scaffoldState.drawerState.open() } },
                onLogoutClick = {
                    viewModel.logout {
                        navController.navigate("login") {
                            popUpTo("stationDetail/$stationId") { inclusive = true }
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
                            popUpTo("home") { inclusive = true }
                        }
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Go Back")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (isFavorited) {
                            viewModel.removeStationFromFavorites(stationId)
                            Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.addStationToFavorites(stationId)
                            Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (isFavorited) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isFavorited) "Unfavorite" else "Favorite"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isFavorited) "Remove from Favorites" else "Add to Favorites")
                }
            }
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
                    color = MaterialTheme.colors.error,
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        groupedObservations.forEach { (paramId, entries) ->
                            val (label, unit) = parameterInfo[paramId] ?: paramId to ""
                            item {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.h6
                                )
                            }
                            items(entries) { obs ->
                                val value = obs.properties.value
                                val observedTime = formatTimestamp(obs.properties.observed)
                                Card(
                                    elevation = 4.dp,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Value: $value $unit")
                                        Text("Observed at: $observedTime")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
