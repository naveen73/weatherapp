package com.example.weatherapp.testutil

import com.example.weatherapp.data.remote.dto.ConditionDto
import com.example.weatherapp.data.remote.dto.CurrentDto
import com.example.weatherapp.data.remote.dto.DayDto
import com.example.weatherapp.data.remote.dto.ForecastDto
import com.example.weatherapp.data.remote.dto.ForecastDayDto
import com.example.weatherapp.data.remote.dto.ForecastResponseDto
import com.example.weatherapp.data.remote.dto.HourDto
import com.example.weatherapp.data.remote.dto.LocationDto

object DtoTestData {

    fun forecastResponse(
        name: String = "London",
        lat: Double = 51.5,
        lon: Double = -0.1,
        tempC: Double = 18.0,
        conditionCode: Int = 1000,
        maxTempC: Double = 22.0,
        days: Int = 7,
        hoursPerDay: Int = 24
    ) = ForecastResponseDto(
        location = LocationDto(name, "England", "United Kingdom", lat, lon),
        current = CurrentDto(
            tempC = tempC,
            condition = ConditionDto(conditionCode, "Sunny"),
            isDay = 1,
            lastUpdatedEpoch = 1_700_000_000L
        ),
        forecast = ForecastDto(
            forecastDay = List(days) { d ->
                ForecastDayDto(
                    dateEpoch = 1_700_000_000L + d * 86_400,
                    day = DayDto(
                        maxTempC = maxTempC,
                        minTempC = 14.0,
                        condition = ConditionDto(conditionCode, "Sunny")
                    ),
                    hour = List(hoursPerDay) { h ->
                        HourDto(
                            timeEpoch = 1_700_000_000L + d * 86_400 + h * 3600,
                            tempC = 15.0 + h % 5,
                            condition = ConditionDto(conditionCode, "Sunny"),
                            isDay = 1
                        )
                    }
                )
            }
        )
    )
}
