package com.group13.weatherappfirstassignment.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.group13.weatherappfirstassignment.screens.LoginScreen
import com.group13.weatherappfirstassignment.screens.SignupScreen
import com.group13.weatherappfirstassignment.screens.HomeScreen
import com.group13.weatherappfirstassignment.viewmodels.AuthViewModel

@Composable
fun AppNavGraph() {
    val navController: NavHostController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(navController = navController, viewModel = authViewModel)
        }
        composable("signup") {
            SignupScreen(navController = navController, viewModel = authViewModel)
        }
        composable("home") {
            HomeScreen()
        }
    }
}
