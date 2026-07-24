package com.example.weatherapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.weatherapp.data.local.dao.CityDao
import com.example.weatherapp.data.local.dao.WeatherDao
import com.example.weatherapp.data.local.entity.CityEntity
import com.example.weatherapp.data.local.entity.CurrentWeatherEntity
import com.example.weatherapp.data.local.entity.DailyEntity
import com.example.weatherapp.data.local.entity.HourlyEntity

@Database(
    entities = [
        CityEntity::class,
        CurrentWeatherEntity::class,
        HourlyEntity::class,
        DailyEntity::class
    ],
    // v2: dropped metrics nothing rendered (feels-like, wind, humidity, UV, precip, rain
    // chance, sunrise/sunset). v3: dropped the `weather_alerts` table with the alerts feature.
    version = 3,
    exportSchema = true
)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun cityDao(): CityDao
    abstract fun weatherDao(): WeatherDao

    companion object {
        const val NAME = "weather.db"
    }
}
