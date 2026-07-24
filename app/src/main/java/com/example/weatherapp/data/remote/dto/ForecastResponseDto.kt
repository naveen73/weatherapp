package com.example.weatherapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTOs for `GET v1/forecast.json`. Only the fields the app actually consumes are declared; the
 * JSON config uses `ignoreUnknownKeys = true`, so the large unused portions of the payload
 * (air quality, imperial units, radiation, astronomy, wind/humidity/UV metrics, etc.) are
 * simply skipped rather than parsed and stored.
 */
@Serializable
data class ForecastResponseDto(
    @SerialName("location") val location: LocationDto,
    @SerialName("current") val current: CurrentDto,
    @SerialName("forecast") val forecast: ForecastDto
)

@Serializable
data class LocationDto(
    @SerialName("name") val name: String,
    @SerialName("region") val region: String = "",
    @SerialName("country") val country: String = "",
    @SerialName("lat") val lat: Double,
    @SerialName("lon") val lon: Double
)

@Serializable
data class ConditionDto(
    @SerialName("code") val code: Int,
    @SerialName("text") val text: String = ""
)

@Serializable
data class CurrentDto(
    @SerialName("temp_c") val tempC: Double,
    @SerialName("condition") val condition: ConditionDto,
    @SerialName("is_day") val isDay: Int,
    @SerialName("last_updated_epoch") val lastUpdatedEpoch: Long
)

@Serializable
data class ForecastDto(
    @SerialName("forecastday") val forecastDay: List<ForecastDayDto> = emptyList()
)

@Serializable
data class ForecastDayDto(
    @SerialName("date_epoch") val dateEpoch: Long,
    @SerialName("day") val day: DayDto,
    @SerialName("hour") val hour: List<HourDto> = emptyList()
)

@Serializable
data class DayDto(
    @SerialName("maxtemp_c") val maxTempC: Double,
    @SerialName("mintemp_c") val minTempC: Double,
    @SerialName("condition") val condition: ConditionDto
)

@Serializable
data class HourDto(
    @SerialName("time_epoch") val timeEpoch: Long,
    @SerialName("temp_c") val tempC: Double,
    @SerialName("condition") val condition: ConditionDto,
    @SerialName("is_day") val isDay: Int
)

