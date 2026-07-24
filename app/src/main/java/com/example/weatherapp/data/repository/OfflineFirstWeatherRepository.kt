package com.example.weatherapp.data.repository

import com.example.weatherapp.core.domain.util.Clock
import com.example.weatherapp.core.domain.util.DataError
import com.example.weatherapp.core.domain.util.EmptyResult
import com.example.weatherapp.core.domain.util.Result
import com.example.weatherapp.core.domain.util.asEmptyResult
import com.example.weatherapp.core.domain.util.map
import com.example.weatherapp.data.datasource.WeatherLocalDataSource
import com.example.weatherapp.data.datasource.WeatherRemoteDataSource
import com.example.weatherapp.data.local.safeDbCall
import com.example.weatherapp.data.remote.WeatherApi
import com.example.weatherapp.data.remote.dto.LocationDto
import com.example.weatherapp.data.remote.mapper.toCity
import com.example.weatherapp.data.remote.mapper.toDomain
import com.example.weatherapp.data.remote.mapper.toSearchResult
import com.example.weatherapp.domain.model.City
import com.example.weatherapp.domain.model.CitySearchResult
import com.example.weatherapp.domain.model.CityWeather
import com.example.weatherapp.domain.policy.RefreshPolicy
import com.example.weatherapp.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow

/**
 * Offline-first repository. Room is the single source of truth: observers read the DB, and
 * refresh operations fetch from the network and persist. TTL (via [RefreshPolicy]) decides
 * whether a foreground refresh actually hits the network. On network failure the cache is
 * preserved and the typed error is surfaced.
 */
class OfflineFirstWeatherRepository(
    private val remote: WeatherRemoteDataSource,
    private val local: WeatherLocalDataSource,
    private val refreshPolicy: RefreshPolicy,
    private val clock: Clock
) : WeatherRepository {

    override fun observeSavedCities(): Flow<List<City>> = local.observeCities()

    override fun observeCityWeather(cityId: String): Flow<CityWeather?> =
        local.observeCityWeather(cityId)

    override suspend fun getSavedCities(): List<City> = local.getCities()

    override suspend fun getCityWeather(cityId: String): CityWeather? = local.getCityWeather(cityId)

    override suspend fun refresh(cityId: String, force: Boolean): EmptyResult<DataError> {
        val city = local.getCity(cityId)
            ?: return Result.Error(DataError.Local.NOT_FOUND)

        if (!force && !refreshPolicy.isStale(city.lastFetchedEpoch, clock.nowEpochMillis())) {
            // Cache is fresh — skip the network entirely (smart refresh).
            return Result.Success(Unit)
        }

        return fetchAndPersist(city.query) { location, fetchedEpoch ->
            city.copy(
                name = location.name,
                region = location.region,
                country = location.country,
                lat = location.lat,
                lon = location.lon,
                lastFetchedEpoch = fetchedEpoch
            )
        }
    }

    override suspend fun refreshAll(force: Boolean): EmptyResult<DataError> {
        var firstError: DataError? = null
        for (city in local.getCities()) {
            val result = refresh(city.id, force)
            if (result is Result.Error && firstError == null) {
                firstError = result.error
            }
        }
        return firstError?.let { Result.Error(it) } ?: Result.Success(Unit)
    }

    override suspend fun ensureCurrentLocationCity(): EmptyResult<DataError> {
        if (local.getCurrentLocationCity() != null) return Result.Success(Unit)

        val orderIndex = local.nextOrderIndex()
        return fetchAndPersist(WeatherApi.AUTO_IP_QUERY) { location, fetchedEpoch ->
            location.toCity(
                // Pin the auto-detected location to lat,lon so it is stable across IP changes.
                id = "${location.lat},${location.lon}",
                isCurrentLocation = true,
                orderIndex = orderIndex,
                lastFetchedEpoch = fetchedEpoch
            )
        }
    }

    override suspend fun addCity(result: CitySearchResult): EmptyResult<DataError> {
        if (local.cityExists(result.id)) return Result.Success(Unit) // idempotent add

        val orderIndex = local.nextOrderIndex()
        return fetchAndPersist("${result.lat},${result.lon}") { location, fetchedEpoch ->
            location.toCity(
                id = result.id,
                isCurrentLocation = false,
                orderIndex = orderIndex,
                lastFetchedEpoch = fetchedEpoch
            )
        }
    }

    override suspend fun removeCity(cityId: String): EmptyResult<DataError> =
        safeDbCall { local.deleteCity(cityId) }

    override suspend fun searchCities(
        query: String
    ): Result<List<CitySearchResult>, DataError.Network> =
        remote.searchCities(query).map { list -> list.map { it.toSearchResult() } }

    /**
     * Fetches the forecast for [query], maps it to domain models, and atomically persists
     * everything. [buildCity] produces the [City] to store (preserving id/order/current-location
     * flags depending on the caller).
     */
    private suspend fun fetchAndPersist(
        query: String,
        buildCity: (LocationDto, fetchedEpoch: Long) -> City
    ): EmptyResult<DataError> {
        return when (val response = remote.getForecast(query)) {
            is Result.Error -> Result.Error(response.error)
            is Result.Success -> {
                val dto = response.data
                val fetchedEpoch = clock.nowEpochMillis()
                val city = buildCity(dto.location, fetchedEpoch)
                val current = dto.current.toDomain()
                val hourly = dto.forecast.forecastDay.flatMap { it.hour }.map { it.toDomain() }
                val daily = dto.forecast.forecastDay.map { it.toDomain() }

                safeDbCall {
                    local.replaceWeather(city, current, hourly, daily, fetchedEpoch)
                }.asEmptyResult()
            }
        }
    }
}
