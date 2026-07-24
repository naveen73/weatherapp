package com.example.weatherapp.domain.model

/** Current conditions for a city. Temperatures are metric (°C). */
data class CurrentWeather(
    val tempC: Double,
    val condition: WeatherCondition,
    val lastUpdatedEpoch: Long
)

/** A single hour in the hourly forecast. */
data class HourlyForecast(
    val timeEpoch: Long,
    val tempC: Double,
    val condition: WeatherCondition
)

/** A single day in the multi-day forecast. */
data class DailyForecast(
    val dateEpoch: Long,
    val maxTempC: Double,
    val minTempC: Double,
    val condition: WeatherCondition
)

/**
 * The full weather snapshot for one city — the aggregate the UI renders and the repository
 * assembles from Room. This is the single object a ViewModel observes.
 */
data class CityWeather(
    val city: City,
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>
)
