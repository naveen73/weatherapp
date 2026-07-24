package com.example.weatherapp.data.remote

import com.example.weatherapp.data.remote.dto.ForecastResponseDto
import com.example.weatherapp.data.remote.dto.SearchLocationDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * WeatherAPI.com endpoints. The `key` query parameter is added transparently by
 * [ApiKeyInterceptor], so it is intentionally absent here. A single `forecast.json` call
 * returns current + hourly + multi-day, so no separate `current.json` call is made.
 */
interface WeatherApi {

    /**
     * `alerts=no` and `aqi=no` suppress the alert and air-quality blocks — the app renders
     * neither. See the README for how alerts would be re-enabled.
     */
    @GET("v1/forecast.json")
    suspend fun getForecast(
        @Query("q") query: String,
        @Query("days") days: Int = FORECAST_DAYS,
        @Query("alerts") alerts: String = "no",
        @Query("aqi") aqi: String = "no"
    ): ForecastResponseDto

    @GET("v1/search.json")
    suspend fun searchCities(@Query("q") query: String): List<SearchLocationDto>

    companion object {
        /** WeatherAPI resolves `auto:ip` to the caller's approximate location via GeoIP. */
        const val AUTO_IP_QUERY = "auto:ip"
        const val FORECAST_DAYS = 7
    }
}
