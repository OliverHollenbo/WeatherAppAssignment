package com.group13.weatherappfirstassignment.network

import com.group13.weatherappfirstassignment.model.WeatherObservation
import retrofit2.Response

class DmiRepository {
    private val api = ApiClient.apiService
    private val apiKey = "8a9dd7ae-4c7b-4039-b4c9-25c78b72061c" // TODO: Secure this

    suspend fun getStations(): Response<StationResponse> {
        val rawResponse = api.getStations(apiKey)

        // Only modify response if successful
        return if (rawResponse.isSuccessful) {
            val uniqueFeatures = rawResponse.body()
                ?.features
                ?.distinctBy { it.properties.stationId }
                ?: emptyList()

            Response.success(StationResponse(features = uniqueFeatures))
        } else {
            rawResponse // Pass through errors unchanged
        }
    }

    suspend fun getLatestObservationsForStation(stationId: String): Response<WeatherObservation> {
        return api.getObservationsForStation(apiKey, stationId)
    }
}
