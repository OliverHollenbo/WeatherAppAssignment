package com.group13.weatherappfirstassignment.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.group13.weatherappfirstassignment.screens.*
import com.group13.weatherappfirstassignment.viewmodels.AuthViewModel

@Composable
fun AppNavGraph(startDestination: String) {
    val navController: NavHostController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginScreen(navController = navController, viewModel = authViewModel)
        }
        composable("signup") {
            SignupScreen(navController = navController, viewModel = authViewModel)
        }
        composable("home") {
            HomeScreen(navController = navController, viewModel = authViewModel)
        }
        composable("nearby") {
            NearbyStationsScreen(navController = navController, viewModel = authViewModel)
        }
        composable("stationDetail/{stationId}") { backStackEntry ->
            val stationId = backStackEntry.arguments?.getString("stationId") ?: ""
            StationDetailScreen(
                navController = navController,
                stationId = stationId,
                viewModel = authViewModel
            )
        }
        composable("favorites") {
            FavoritesScreen(navController = navController, viewModel = authViewModel)
        }
    }
}
