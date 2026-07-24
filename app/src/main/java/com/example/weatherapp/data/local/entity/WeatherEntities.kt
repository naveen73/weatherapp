package com.example.weatherapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** A saved city. [lastFetchedEpoch] is the cache timestamp that drives TTL / smart refresh. */
@Entity(tableName = "cities")
data class CityEntity(
    @PrimaryKey val id: String,
    val name: String,
    val region: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val isCurrentLocation: Boolean,
    val orderIndex: Int,
    val lastFetchedEpoch: Long?
)

@Entity(
    tableName = "current_weather",
    foreignKeys = [
        ForeignKey(
            entity = CityEntity::class,
            parentColumns = ["id"],
            childColumns = ["cityId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CurrentWeatherEntity(
    @PrimaryKey val cityId: String,
    val tempC: Double,
    val conditionCode: Int,
    val conditionText: String,
    val isDay: Boolean,
    val lastUpdatedEpoch: Long
)

@Entity(
    tableName = "hourly_forecast",
    foreignKeys = [
        ForeignKey(
            entity = CityEntity::class,
            parentColumns = ["id"],
            childColumns = ["cityId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("cityId")]
)
data class HourlyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cityId: String,
    val timeEpoch: Long,
    val tempC: Double,
    val conditionCode: Int,
    val conditionText: String,
    val isDay: Boolean
)

@Entity(
    tableName = "daily_forecast",
    foreignKeys = [
        ForeignKey(
            entity = CityEntity::class,
            parentColumns = ["id"],
            childColumns = ["cityId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("cityId")]
)
data class DailyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cityId: String,
    val dateEpoch: Long,
    val maxTempC: Double,
    val minTempC: Double,
    val conditionCode: Int,
    val conditionText: String
)
