package com.example.weatherapp.domain.model

/**
 * A saved location. [id] is the stable query key used for caching and refreshing:
 * for the auto-detected location it is the resolved `"lat,lon"` (never the literal
 * `auto:ip`, which would drift as the IP changes); for searched cities it is WeatherAPI's
 * `url` slug. [lastFetchedEpoch] is the cache timestamp that drives TTL / smart refresh.
 */
data class City(
    val id: String,
    val name: String,
    val region: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val isCurrentLocation: Boolean = false,
    val orderIndex: Int = 0,
    val lastFetchedEpoch: Long? = null
) {
    /** The value passed as the WeatherAPI `q` parameter when refreshing this city. */
    val query: String get() = "$lat,$lon"

    val displayName: String get() = if (isCurrentLocation) "My Location" else name
}
