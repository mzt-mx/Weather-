package com.example.myweather.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.myweather.network.WeatherApi

object RetrofitInstance {
    val api: WeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApi::class.java)
    }
}
