package com.group13.weatherappfirstassignment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import com.group13.weatherappfirstassignment.navigation.AppNavGraph
import com.group13.weatherappfirstassignment.ui.theme.WeatherAppTheme
import com.group13.weatherappfirstassignment.viewmodels.AuthViewModel
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase once
        FirebaseApp.initializeApp(this)

        setContent {
            WeatherAppTheme {
                Surface(color = MaterialTheme.colors.background) {
                    val authViewModel: AuthViewModel = viewModel()

                    // Get startDestination from the ViewModel
                    val startDestination = if (authViewModel.isUserLoggedIn()) "home" else "login"

                    AppNavGraph(startDestination = startDestination)
                }
            }
        }
    }
}
