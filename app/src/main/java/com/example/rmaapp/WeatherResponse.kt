package com.example.rmaapp

import com.squareup.moshi.Json

data class WeatherResponse(
    @Json(name = "current") val current: CurrentWeather
)

data class CurrentWeather(
    @Json(name = "temp_c") val tempC: Double,
    @Json(name = "condition") val condition: WeatherCondition,
    @Json(name = "precip_mm") val precipMm: Double
)

data class WeatherCondition(
    @Json(name = "text") val text: String,
    @Json(name = "icon") val icon: String
)
