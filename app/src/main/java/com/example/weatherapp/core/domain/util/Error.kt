package com.example.weatherapp.core.domain.util

/**
 * Marker supertype for every typed error in the app. Both [DataError] and feature-specific
 * error enums (e.g. WeatherError) implement this so they can flow through [Result].
 */
interface Error
