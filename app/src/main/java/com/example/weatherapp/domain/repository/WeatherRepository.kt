package com.example.weatherapp.domain.repository

import com.example.weatherapp.core.domain.util.DataError
import com.example.weatherapp.core.domain.util.EmptyResult
import com.example.weatherapp.core.domain.util.Result
import com.example.weatherapp.domain.model.City
import com.example.weatherapp.domain.model.CitySearchResult
import com.example.weatherapp.domain.model.CityWeather
import kotlinx.coroutines.flow.Flow

/**
 * Offline-first weather repository. Room is the single source of truth: reads are exposed as
 * [Flow]s backed by the database, and refresh operations fetch from the network then persist,
 * so the UI is always driven by the DB — never by raw network responses.
 */
interface WeatherRepository {

    /** All saved cities, ordered, emitting on every DB change. */
    fun observeSavedCities(): Flow<List<City>>

    /** The full weather snapshot for a city, or `null` if nothing is cached yet. */
    fun observeCityWeather(cityId: String): Flow<CityWeather?>

    /** One-shot read of saved cities (used by the background sync worker). */
    suspend fun getSavedCities(): List<City>

    /** One-shot read of a city's cached snapshot (used by the background sync worker). */
    suspend fun getCityWeather(cityId: String): CityWeather?

    /**
     * Refresh a single city. When [force] is false the network is skipped if the cache is
     * still fresh per the TTL policy (smart refresh). On network failure the cache is kept.
     */
    suspend fun refresh(cityId: String, force: Boolean): EmptyResult<DataError>

    /** Refresh every saved city. Returns the first error encountered, if any. */
    suspend fun refreshAll(force: Boolean): EmptyResult<DataError>

    /**
     * Ensure the auto-detected "My Location" city exists. On first launch this resolves the
     * device location via `auto:ip`, pins it by `lat,lon`, and caches its weather.
     * No-op if a current-location city already exists.
     */
    suspend fun ensureCurrentLocationCity(): EmptyResult<DataError>

    /** Add a searched city and fetch its initial weather. */
    suspend fun addCity(result: CitySearchResult): EmptyResult<DataError>

    /** Remove a saved city and all its cached weather. */
    suspend fun removeCity(cityId: String): EmptyResult<DataError>

    /** Search for cities by free-text query (city autocomplete). */
    suspend fun searchCities(query: String): Result<List<CitySearchResult>, DataError.Network>
}
