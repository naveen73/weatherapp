package com.example.weatherapp.testutil

import com.example.weatherapp.domain.model.City
import com.example.weatherapp.domain.model.CityWeather
import com.example.weatherapp.domain.model.CurrentWeather
import com.example.weatherapp.domain.model.DailyForecast
import com.example.weatherapp.domain.model.HourlyForecast
import com.example.weatherapp.domain.model.WeatherCondition

object TestData {

    fun city(
        id: String = "51.5,-0.1",
        name: String = "London",
        isCurrentLocation: Boolean = false,
        orderIndex: Int = 0,
        lastFetchedEpoch: Long? = null
    ) = City(
        id = id,
        name = name,
        region = "England",
        country = "United Kingdom",
        lat = 51.5,
        lon = -0.1,
        isCurrentLocation = isCurrentLocation,
        orderIndex = orderIndex,
        lastFetchedEpoch = lastFetchedEpoch
    )

    fun current(
        tempC: Double = 18.0,
        code: Int = 1000,
        isDay: Boolean = true
    ) = CurrentWeather(
        tempC = tempC,
        condition = WeatherCondition(code, "Sunny", isDay),
        lastUpdatedEpoch = 1_700_000_000L
    )

    fun hourly(count: Int = 24, startEpoch: Long = 1_700_000_000L) = List(count) { i ->
        HourlyForecast(
            timeEpoch = startEpoch + i * 3600,
            tempC = 15.0 + i % 5,
            condition = WeatherCondition(1000, "Sunny", isDay = true)
        )
    }

    fun daily(
        count: Int = 7,
        maxTempC: Double = 22.0,
        minTempC: Double = 14.0
    ) = List(count) { i ->
        DailyForecast(
            dateEpoch = 1_700_000_000L + i * 86_400,
            maxTempC = maxTempC,
            minTempC = minTempC,
            condition = WeatherCondition(1000, "Sunny", isDay = true)
        )
    }

    fun cityWeather(
        city: City = city(),
        current: CurrentWeather = current(),
        hourly: List<HourlyForecast> = hourly(),
        daily: List<DailyForecast> = daily()
    ) = CityWeather(city, current, hourly, daily)
}
