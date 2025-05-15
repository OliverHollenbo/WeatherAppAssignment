package com.group13.weatherappfirstassignment.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.group13.weatherappfirstassignment.network.DmiRepository
import com.group13.weatherappfirstassignment.network.StationFeature
import com.group13.weatherappfirstassignment.ui.components.AppDrawer
import com.group13.weatherappfirstassignment.ui.components.AppTopBar
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@Composable
fun MapScreen(navController: NavController) {
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    var stations by remember { mutableStateOf<List<StationFeature>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val aarhus = LatLng(56.1629, 10.2039) // fallback center

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
                    if (coords.size == 2) {
                        Marker(
                            state = MarkerState(position = LatLng(coords[1], coords[0])),
                            title = station.properties.name,
                            snippet = "ID: ${station.properties.stationId}"
                        )
                    }
                }
            }
        }
    }
}
