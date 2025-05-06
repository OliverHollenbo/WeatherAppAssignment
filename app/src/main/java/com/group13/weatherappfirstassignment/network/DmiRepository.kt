package com.group13.weatherappfirstassignment.network

import com.group13.weatherappfirstassignment.model.WeatherObservation
import retrofit2.Response

class DmiRepository {
    private val api = ApiClient.apiService
    private val apiKey = "8a9dd7ae-4c7b-4039-b4c9-25c78b72061c" // Secure this later!

    suspend fun getStations(): Response<StationResponse> {
        return api.getStations(apiKey)
    }

    suspend fun getLatestObservations(): Response<WeatherObservation> {
        return api.getObservations(apiKey)
    }
}
