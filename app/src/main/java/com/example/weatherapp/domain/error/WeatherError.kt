package com.example.weatherapp.domain.error

import com.example.weatherapp.core.domain.util.Error

/** Feature-specific errors that are not plain data-source failures. */
enum class WeatherError : Error {
    NO_SAVED_CITIES,
    CITY_ALREADY_ADDED,
    LOCATION_UNAVAILABLE
}
