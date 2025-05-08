package com.group13.weatherappfirstassignment.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.group13.weatherappfirstassignment.network.DmiRepository
import com.group13.weatherappfirstassignment.model.Feature
import com.group13.weatherappfirstassignment.model.parameterInfo
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

    var observations by remember { mutableStateOf<List<Feature>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                            popUpTo("stationDetail/$stationId") { inclusive = true }
                        }
                    }
                }) {
                    Text("Logout")
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
                    if (groupedObservations.isEmpty()) {
                        Text(
                            text = "No recent observations available.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
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
}
