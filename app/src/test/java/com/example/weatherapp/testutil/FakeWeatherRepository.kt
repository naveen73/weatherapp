package com.example.weatherapp.testutil

import com.example.weatherapp.core.domain.util.DataError
import com.example.weatherapp.core.domain.util.EmptyResult
import com.example.weatherapp.core.domain.util.Result
import com.example.weatherapp.domain.model.City
import com.example.weatherapp.domain.model.CitySearchResult
import com.example.weatherapp.domain.model.CityWeather
import com.example.weatherapp.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory fake repository for ViewModel tests (fakes over mocks). */
class FakeWeatherRepository : WeatherRepository {

    private val citiesFlow = MutableStateFlow<List<City>>(emptyList())
    private val weatherFlows = mutableMapOf<String, MutableStateFlow<CityWeather?>>()

    var searchResult: Result<List<CitySearchResult>, DataError.Network> = Result.Success(emptyList())
    var refreshResult: EmptyResult<DataError> = Result.Success(Unit)
    var ensureResult: EmptyResult<DataError> = Result.Success(Unit)
    var addResult: EmptyResult<DataError> = Result.Success(Unit)
    var removeResult: EmptyResult<DataError> = Result.Success(Unit)

    val refreshCalls = mutableListOf<Pair<String, Boolean>>()
    var refreshAllForce: Boolean? = null
    var ensureCalled = false
    val addedCities = mutableListOf<CitySearchResult>()
    val removedCityIds = mutableListOf<String>()
    var lastSearchQuery: String? = null

    fun seed(cityWeather: CityWeather) {
        val city = cityWeather.city
        citiesFlow.value = (citiesFlow.value.filterNot { it.id == city.id } + city)
            .sortedBy { it.orderIndex }
        flowFor(city.id).value = cityWeather
    }

    fun setCities(cities: List<City>) {
        citiesFlow.value = cities.sortedBy { it.orderIndex }
    }

    private fun flowFor(cityId: String) =
        weatherFlows.getOrPut(cityId) { MutableStateFlow(null) }

    override fun observeSavedCities(): Flow<List<City>> = citiesFlow

    override fun observeCityWeather(cityId: String): Flow<CityWeather?> = flowFor(cityId)

    override suspend fun getSavedCities(): List<City> = citiesFlow.value

    override suspend fun getCityWeather(cityId: String): CityWeather? = flowFor(cityId).value

    override suspend fun refresh(cityId: String, force: Boolean): EmptyResult<DataError> {
        refreshCalls += cityId to force
        return refreshResult
    }

    override suspend fun refreshAll(force: Boolean): EmptyResult<DataError> {
        refreshAllForce = force
        return refreshResult
    }

    override suspend fun ensureCurrentLocationCity(): EmptyResult<DataError> {
        ensureCalled = true
        return ensureResult
    }

    override suspend fun addCity(result: CitySearchResult): EmptyResult<DataError> {
        addedCities += result
        return addResult
    }

    override suspend fun removeCity(cityId: String): EmptyResult<DataError> {
        removedCityIds += cityId
        return removeResult
    }

    override suspend fun searchCities(
        query: String
    ): Result<List<CitySearchResult>, DataError.Network> {
        lastSearchQuery = query
        return searchResult
    }
}
