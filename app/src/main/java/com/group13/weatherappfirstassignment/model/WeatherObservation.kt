package com.group13.weatherappfirstassignment.model

import com.google.gson.annotations.SerializedName

data class WeatherObservation(
    @SerializedName("features")
    val features: List<Feature>
)

data class Feature(
    @SerializedName("properties")
    val properties: Properties,

    @SerializedName("geometry")
    val geometry: Geometry
)

data class Properties(
    @SerializedName("stationId")
    val stationId: String?,

    @SerializedName("parameterId")
    val parameterId: String?,

    @SerializedName("value")
    val value: Double?,

    @SerializedName("timeObserved")
    val timeObserved: String?
)

data class Geometry(
    @SerializedName("coordinates")
    val coordinates: List<Double>
)
