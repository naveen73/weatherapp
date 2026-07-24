package com.example.weatherapp.data.datasource

import com.example.weatherapp.core.domain.util.DataError
import com.example.weatherapp.core.domain.util.Result
import com.example.weatherapp.data.remote.WeatherApi
import com.example.weatherapp.data.remote.dto.ForecastResponseDto
import com.example.weatherapp.data.remote.dto.SearchLocationDto
import com.example.weatherapp.data.remote.safeApiCall

/** Remote (network) source of weather data. */
interface WeatherRemoteDataSource {
    suspend fun getForecast(query: String): Result<ForecastResponseDto, DataError.Network>
    suspend fun searchCities(query: String): Result<List<SearchLocationDto>, DataError.Network>
}

class RetrofitWeatherRemoteDataSource(
    private val api: WeatherApi
) : WeatherRemoteDataSource {

    override suspend fun getForecast(query: String): Result<ForecastResponseDto, DataError.Network> =
        safeApiCall { api.getForecast(query = query) }

    override suspend fun searchCities(query: String): Result<List<SearchLocationDto>, DataError.Network> =
        safeApiCall { api.searchCities(query = query) }
}
