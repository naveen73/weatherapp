package com.example.weatherapp.data.remote.mapper

import com.example.weatherapp.data.remote.dto.CurrentDto
import com.example.weatherapp.data.remote.dto.ForecastDayDto
import com.example.weatherapp.data.remote.dto.HourDto
import com.example.weatherapp.data.remote.dto.LocationDto
import com.example.weatherapp.data.remote.dto.SearchLocationDto
import com.example.weatherapp.domain.model.City
import com.example.weatherapp.domain.model.CitySearchResult
import com.example.weatherapp.domain.model.CurrentWeather
import com.example.weatherapp.domain.model.DailyForecast
import com.example.weatherapp.domain.model.HourlyForecast
import com.example.weatherapp.domain.model.WeatherCondition

fun CurrentDto.toDomain(): CurrentWeather = CurrentWeather(
    tempC = tempC,
    condition = WeatherCondition(code = condition.code, text = condition.text, isDay = isDay == 1),
    lastUpdatedEpoch = lastUpdatedEpoch
)

fun HourDto.toDomain(): HourlyForecast = HourlyForecast(
    timeEpoch = timeEpoch,
    tempC = tempC,
    condition = WeatherCondition(code = condition.code, text = condition.text, isDay = isDay == 1)
)

fun ForecastDayDto.toDomain(): DailyForecast = DailyForecast(
    dateEpoch = dateEpoch,
    maxTempC = day.maxTempC,
    minTempC = day.minTempC,
    // Daily conditions are always shown with the day-time icon.
    condition = WeatherCondition(code = day.condition.code, text = day.condition.text, isDay = true)
)

/**
 * Builds a [City] from the resolved location. [id] is supplied by the caller (the stable
 * cache key: `lat,lon` for the auto-location, the search `url` slug otherwise).
 */
fun LocationDto.toCity(
    id: String,
    isCurrentLocation: Boolean,
    orderIndex: Int,
    lastFetchedEpoch: Long?
): City = City(
    id = id,
    name = name,
    region = region,
    country = country,
    lat = lat,
    lon = lon,
    isCurrentLocation = isCurrentLocation,
    orderIndex = orderIndex,
    lastFetchedEpoch = lastFetchedEpoch
)

fun SearchLocationDto.toSearchResult(): CitySearchResult = CitySearchResult(
    id = url.ifBlank { "$lat,$lon" },
    name = name,
    region = region,
    country = country,
    lat = lat,
    lon = lon
)
