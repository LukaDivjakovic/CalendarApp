package com.example.rmaapp

import com.squareup.moshi.Json

data class WeatherResponse(
    @Json(name = "location") val location: Location,
    @Json(name = "current") val current: CurrentWeather
)

data class Location(
    @Json(name = "name") val name: String,
    @Json(name = "localtime") val localtime: String
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
