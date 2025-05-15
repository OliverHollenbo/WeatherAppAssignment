package com.group13.weatherappfirstassignment.screens

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.group13.weatherappfirstassignment.network.DmiRepository
import com.group13.weatherappfirstassignment.network.StationFeature
import com.group13.weatherappfirstassignment.ui.components.AppDrawer
import com.group13.weatherappfirstassignment.ui.components.AppTopBar
import com.group13.weatherappfirstassignment.viewmodels.AuthViewModel
import kotlinx.coroutines.launch
import kotlin.math.*

fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
}

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun NearbyStationsScreen(navController: NavController, viewModel: AuthViewModel = viewModel()) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var userLocation by remember { mutableStateOf<Location?>(null) }
    var nearestStation by remember { mutableStateOf<StationFeature?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val favorites by viewModel.favoriteStationIds.collectAsState()
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(locationPermission.status) {
        viewModel.loadFavorites()

        if (locationPermission.status.isGranted) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        userLocation = location
                    } else {
                        errorMessage = "Location unavailable. Try again shortly."
                        isLoading = false
                    }
                }
                .addOnFailureListener {
                    errorMessage = "Failed to get location."
                    isLoading = false
                }
        } else {
            locationPermission.launchPermissionRequest()
        }
    }

    LaunchedEffect(userLocation) {
        if (userLocation != null) {
            scope.launch {
                try {
                    val response = DmiRepository().getStations()
                    if (response.isSuccessful) {
                        val stations = response.body()?.features ?: emptyList()
                        nearestStation = stations
                            .filter { it.geometry.coordinates.size == 2 }
                            .minByOrNull {
                                val lon = it.geometry.coordinates[0]
                                val lat = it.geometry.coordinates[1]
                                haversineDistance(userLocation!!.latitude, userLocation!!.longitude, lat, lon)
                            }
                    } else {
                        errorMessage = "Error: ${response.code()}"
                    }
                } catch (e: Exception) {
                    errorMessage = "Exception: ${e.message}"
                }
                isLoading = false
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            AppTopBar(
                title = "Nearest Station",
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when {
                isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                errorMessage != null -> Text(
                    text = errorMessage ?: "Unknown error",
                    color = MaterialTheme.colors.error,
                    modifier = Modifier.align(Alignment.Center)
                )

                nearestStation == null -> Text(
                    "No nearby station found.",
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> {
                    val station = nearestStation!!
                    val stationId = station.properties.stationId ?: ""
                    val isFavorited = favorites.contains(stationId)

                    val coords = station.geometry.coordinates
                    val stationLon = coords[0]
                    val stationLat = coords[1]
                    val distanceKm = haversineDistance(
                        userLocation!!.latitude,
                        userLocation!!.longitude,
                        stationLat,
                        stationLon
                    )

                    Card(
                        elevation = 4.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate("stationDetail/$stationId")
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Station ID: $stationId")
                                Text("Name: ${station.properties.name}")
                                Text("Distance: %.1f km".format(distanceKm))
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
