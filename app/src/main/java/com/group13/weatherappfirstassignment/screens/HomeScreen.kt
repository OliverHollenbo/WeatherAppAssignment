package com.group13.weatherappfirstassignment.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.group13.weatherappfirstassignment.ui.components.AppTopBar
import com.group13.weatherappfirstassignment.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController, viewModel: AuthViewModel) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            AppTopBar(
                title = "Home",
                onMenuClick = {
                    scope.launch { scaffoldState.drawerState.open() }
                }
            )
        },
        drawerContent = {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Menu", style = MaterialTheme.typography.h6)

                Spacer(modifier = Modifier.height(24.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                // Navigate to Nearby Stations
                TextButton(onClick = {
                    navController.navigate("nearby")
                    scope.launch { scaffoldState.drawerState.close() }
                }) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Nearby")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nearby Stations")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Logout
                TextButton(onClick = {
                    viewModel.logout {
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                }) {
                    Icon(Icons.Default.Logout, contentDescription = "Logout")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout")
                }
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Welcome to the Home Page!")
            }
        }
    )
}
