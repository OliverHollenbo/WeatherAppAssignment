package com.group13.weatherappfirstassignment.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.group13.weatherappfirstassignment.screens.LoginScreen
import com.group13.weatherappfirstassignment.screens.SignupScreen
import com.group13.weatherappfirstassignment.screens.HomeScreen

@Composable
fun AppNavGraph(auth: FirebaseAuth) {
    val navController: NavHostController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(navController = navController, auth = auth)
        }
        composable("signup") {
            SignupScreen(navController = navController, auth = auth)
        }
        composable("home") {
            HomeScreen()
        }
    }
}
