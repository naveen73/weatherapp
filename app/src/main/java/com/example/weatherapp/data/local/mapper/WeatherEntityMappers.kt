package com.example.weatherapp.data.local.mapper

import com.example.weatherapp.data.local.entity.CityEntity
import com.example.weatherapp.data.local.entity.CurrentWeatherEntity
import com.example.weatherapp.data.local.entity.DailyEntity
import com.example.weatherapp.data.local.entity.HourlyEntity
import com.example.weatherapp.domain.model.City
import com.example.weatherapp.domain.model.CurrentWeather
import com.example.weatherapp.domain.model.DailyForecast
import com.example.weatherapp.domain.model.HourlyForecast
import com.example.weatherapp.domain.model.WeatherCondition

// --- City ---

fun CityEntity.toDomain(): City = City(
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

fun City.toEntity(): CityEntity = CityEntity(
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

// --- Current ---

fun CurrentWeatherEntity.toDomain(): CurrentWeather = CurrentWeather(
    tempC = tempC,
    condition = WeatherCondition(conditionCode, conditionText, isDay),
    lastUpdatedEpoch = lastUpdatedEpoch
)

fun CurrentWeather.toEntity(cityId: String): CurrentWeatherEntity = CurrentWeatherEntity(
    cityId = cityId,
    tempC = tempC,
    conditionCode = condition.code,
    conditionText = condition.text,
    isDay = condition.isDay,
    lastUpdatedEpoch = lastUpdatedEpoch
)

// --- Hourly ---

fun HourlyEntity.toDomain(): HourlyForecast = HourlyForecast(
    timeEpoch = timeEpoch,
    tempC = tempC,
    condition = WeatherCondition(conditionCode, conditionText, isDay)
)

fun HourlyForecast.toEntity(cityId: String): HourlyEntity = HourlyEntity(
    cityId = cityId,
    timeEpoch = timeEpoch,
    tempC = tempC,
    conditionCode = condition.code,
    conditionText = condition.text,
    isDay = condition.isDay
)

// --- Daily ---

fun DailyEntity.toDomain(): DailyForecast = DailyForecast(
    dateEpoch = dateEpoch,
    maxTempC = maxTempC,
    minTempC = minTempC,
    condition = WeatherCondition(conditionCode, conditionText, isDay = true)
)

fun DailyForecast.toEntity(cityId: String): DailyEntity = DailyEntity(
    cityId = cityId,
    dateEpoch = dateEpoch,
    maxTempC = maxTempC,
    minTempC = minTempC,
    conditionCode = condition.code,
    conditionText = condition.text
)
