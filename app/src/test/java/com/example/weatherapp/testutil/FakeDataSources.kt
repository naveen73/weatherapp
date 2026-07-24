package com.example.weatherapp.testutil

import com.example.weatherapp.core.domain.util.DataError
import com.example.weatherapp.core.domain.util.Result
import com.example.weatherapp.data.datasource.WeatherLocalDataSource
import com.example.weatherapp.data.datasource.WeatherRemoteDataSource
import com.example.weatherapp.data.remote.dto.ForecastResponseDto
import com.example.weatherapp.data.remote.dto.SearchLocationDto
import com.example.weatherapp.domain.model.City
import com.example.weatherapp.domain.model.CityWeather
import com.example.weatherapp.domain.model.CurrentWeather
import com.example.weatherapp.domain.model.DailyForecast
import com.example.weatherapp.domain.model.HourlyForecast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeWeatherRemoteDataSource : WeatherRemoteDataSource {
    var forecastResult: Result<ForecastResponseDto, DataError.Network> =
        Result.Success(DtoTestData.forecastResponse())
    var searchResult: Result<List<SearchLocationDto>, DataError.Network> = Result.Success(emptyList())

    val forecastQueries = mutableListOf<String>()
    var searchQuery: String? = null

    override suspend fun getForecast(query: String): Result<ForecastResponseDto, DataError.Network> {
        forecastQueries += query
        return forecastResult
    }

    override suspend fun searchCities(query: String): Result<List<SearchLocationDto>, DataError.Network> {
        searchQuery = query
        return searchResult
    }
}

class FakeWeatherLocalDataSource : WeatherLocalDataSource {
    private val citiesFlow = MutableStateFlow<List<City>>(emptyList())
    private val weatherFlows = mutableMapOf<String, MutableStateFlow<CityWeather?>>()

    val replaceWeatherCalls = mutableListOf<City>()
    var replaceShouldThrow = false

    private fun flowFor(cityId: String) = weatherFlows.getOrPut(cityId) { MutableStateFlow(null) }

    fun preloadCity(city: City) {
        citiesFlow.value = (citiesFlow.value.filterNot { it.id == city.id } + city)
            .sortedBy { it.orderIndex }
    }

    override fun observeCities(): Flow<List<City>> = citiesFlow

    override fun observeCityWeather(cityId: String): Flow<CityWeather?> = flowFor(cityId)

    override suspend fun getCities(): List<City> = citiesFlow.value

    override suspend fun getCityWeather(cityId: String): CityWeather? = flowFor(cityId).value

    override suspend fun getCity(cityId: String): City? = citiesFlow.value.find { it.id == cityId }

    override suspend fun getCurrentLocationCity(): City? =
        citiesFlow.value.find { it.isCurrentLocation }

    override suspend fun cityExists(cityId: String): Boolean =
        citiesFlow.value.any { it.id == cityId }

    override suspend fun nextOrderIndex(): Int =
        (citiesFlow.value.maxOfOrNull { it.orderIndex } ?: -1) + 1

    override suspend fun upsertCity(city: City) = preloadCity(city)

    override suspend fun deleteCity(cityId: String) {
        citiesFlow.value = citiesFlow.value.filterNot { it.id == cityId }
        weatherFlows.remove(cityId)
    }

    override suspend fun replaceWeather(
        city: City,
        current: CurrentWeather,
        hourly: List<HourlyForecast>,
        daily: List<DailyForecast>,
        fetchedEpoch: Long
    ) {
        if (replaceShouldThrow) throw RuntimeException("disk error")
        replaceWeatherCalls += city
        preloadCity(city.copy(lastFetchedEpoch = fetchedEpoch))
        flowFor(city.id).value = CityWeather(
            city.copy(lastFetchedEpoch = fetchedEpoch), current, hourly, daily
        )
    }
}
