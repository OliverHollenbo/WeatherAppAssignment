package com.group13.weatherappfirstassignment.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://dmigw.govcloud.dk/v2/"

object ApiClient {
    private val client = OkHttpClient.Builder().build()

    val apiService: DmiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DmiApiService::class.java)
    }
}
