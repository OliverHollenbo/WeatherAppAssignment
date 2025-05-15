package com.group13.weatherappfirstassignment.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.group13.weatherappfirstassignment.network.DmiRepository
import com.group13.weatherappfirstassignment.network.StationFeature
import com.group13.weatherappfirstassignment.ui.components.AppDrawer
import com.group13.weatherappfirstassignment.ui.components.AppTopBar
import com.group13.weatherappfirstassignment.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@Composable
fun MapScreen(navController: NavController, viewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    var stations by remember { mutableStateOf<List<StationFeature>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedStationId by remember { mutableStateOf<String?>(null) }

    val favorites by viewModel.favoriteStationIds.collectAsState()

    val aarhus = LatLng(56.1629, 10.2039)

    LaunchedEffect(Unit) {
        try {
            val response = DmiRepository().getStations()
            if (response.isSuccessful) {
                stations = response.body()?.features ?: emptyList()
            } else {
                errorMessage = "DMI Error: ${response.code()}"
            }
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            AppTopBar(
                title = "All Stations Map",
                onMenuClick = { scope.launch { scaffoldState.drawerState.open() } }
            )
        },
        drawerContent = {
            AppDrawer(
                navController = navController,
                scope = scope,
                scaffoldState = scaffoldState,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("map") { inclusive = true }
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.fillMaxSize())
        } else if (errorMessage != null) {
            Text(text = errorMessage ?: "Unknown error", color = MaterialTheme.colors.error)
        } else {
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(aarhus, 6f)
            }

            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                cameraPositionState = cameraPositionState
            ) {
                stations.forEach { station ->
                    val coords = station.geometry.coordinates
                    val stationId = station.properties.stationId ?: return@forEach
                    val isFavorite = favorites.contains(stationId)

                    if (coords.size == 2) {
                        val position = LatLng(coords[1], coords[0])
                        Marker(
                            state = MarkerState(position = position),
                            title = station.properties.name,
                            snippet = "ID: $stationId",
                            icon = if (isFavorite) BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                            else BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                            onInfoWindowClick = {
                                navController.navigate("stationDetail/$stationId")
                            },
                            onClick = {
                                selectedStationId = stationId
                                false // show info window
                            }
                        )
                    }
                }
            }
        }
    }
}
