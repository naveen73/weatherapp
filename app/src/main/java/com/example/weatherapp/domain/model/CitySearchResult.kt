package com.example.weatherapp.domain.model

/** A city suggestion returned by the search endpoint, before it is saved. */
data class CitySearchResult(
    val id: String,
    val name: String,
    val region: String,
    val country: String,
    val lat: Double,
    val lon: Double
) {
    val label: String
        get() = listOf(name, region, country)
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(", ")
}
