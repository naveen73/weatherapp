package com.example.weatherapp.presentation.weather

import androidx.compose.runtime.Stable
import com.example.weatherapp.domain.model.CityWeather
import com.example.weatherapp.domain.model.WeatherConditionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/** Everything one weather page (one city) needs to render — deliberately minimal. */
@Stable
data class CityWeatherUi(
    val cityId: String,
    val cityName: String,
    val region: String,
    val conditionType: WeatherConditionType,
    val isDay: Boolean,
    val temperature: String,
    val conditionText: String,
    val highLow: String,
    val lastUpdated: String,
    val hourly: List<HourlyUi>,
    val daily: List<DailyUi>
)

@Stable
data class HourlyUi(
    val timeLabel: String,
    val temperature: String
)

@Stable
data class DailyUi(
    val dayLabel: String,
    val high: String,
    val low: String
)

private fun temp(c: Double): String = "${c.roundToInt()}°"

private fun fmt(pattern: String, epochSeconds: Long): String =
    SimpleDateFormat(pattern, Locale.getDefault()).format(Date(epochSeconds * 1000))

/**
 * Maps a domain [CityWeather] to its UI model. [nowEpochSeconds] slices the hourly forecast
 * to the upcoming 24 hours and labels "Today". All values are metric.
 */
fun CityWeather.toUi(nowEpochSeconds: Long): CityWeatherUi {
    val today = daily.firstOrNull()

    val upcomingHours = hourly
        .filter { it.timeEpoch >= nowEpochSeconds - 3600 }
        .take(24)
        .map { hour -> HourlyUi(timeLabel = fmt("HH:mm", hour.timeEpoch), temperature = temp(hour.tempC)) }

    val days = daily.mapIndexed { index, day ->
        DailyUi(
            dayLabel = if (index == 0) "Today" else fmt("EEE", day.dateEpoch),
            high = temp(day.maxTempC),
            low = temp(day.minTempC)
        )
    }

    return CityWeatherUi(
        cityId = city.id,
        cityName = city.displayName,
        region = listOf(city.name.takeIf { city.isCurrentLocation }, city.region)
            .firstOrNull { !it.isNullOrBlank() } ?: city.country,
        conditionType = current.condition.type,
        isDay = current.condition.isDay,
        temperature = temp(current.tempC),
        conditionText = current.condition.text,
        highLow = today?.let { "H:${temp(it.maxTempC)}   L:${temp(it.minTempC)}" } ?: "",
        lastUpdated = "Updated ${fmt("HH:mm", current.lastUpdatedEpoch)}",
        hourly = upcomingHours,
        daily = days
    )
}
