package com.group13.weatherappfirstassignment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import com.group13.weatherappfirstassignment.navigation.AppNavGraph
import com.group13.weatherappfirstassignment.ui.theme.WeatherAppTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        val auth = FirebaseAuth.getInstance()

        setContent {
            WeatherAppTheme {
                Surface(color = MaterialTheme.colors.background) {
                    AppNavGraph(auth = auth)
                }
            }
        }
    }
}
