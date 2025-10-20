package com.example.rmaapp

import com.squareup.moshi.Json

data class WeatherResponse(
    @Json(name = "current") val current: CurrentWeather
)

data class CurrentWeather(
    @Json(name = "temp_c") val tempC: Double,
    @Json(name = "condition") val condition: WeatherCondition
)

data class WeatherCondition(
    @Json(name = "text") val text: String,
    @Json(name = "icon") val icon: String
)
