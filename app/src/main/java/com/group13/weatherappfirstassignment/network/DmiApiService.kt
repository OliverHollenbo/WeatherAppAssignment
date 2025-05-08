package com.group13.weatherappfirstassignment.network

import com.group13.weatherappfirstassignment.model.WeatherObservation
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

data class Station(
    val stationId: String,
    val name: String
)

data class StationFeature(
    val properties: Station
)

data class StationResponse(
    val features: List<StationFeature>
)

interface DmiApiService {
    @GET("metObs/collections/station/items")
    suspend fun getStations(
        @Query("api-key") apiKey: String,
        @Query("limit") limit: Int = 100
    ): Response<StationResponse>

    @GET("metObs/collections/observation/items")
    suspend fun getObservationsForStation(
        @Query("api-key") apiKey: String,
        @Query("stationId") stationId: String,
        @Query("limit") limit: Int = 100
    ): Response<WeatherObservation>
}
