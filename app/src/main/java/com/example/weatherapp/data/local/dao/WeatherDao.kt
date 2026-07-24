package com.example.weatherapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weatherapp.data.local.entity.CurrentWeatherEntity
import com.example.weatherapp.data.local.entity.DailyEntity
import com.example.weatherapp.data.local.entity.HourlyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    // --- Observed reads (DB as source of truth) ---

    @Query("SELECT * FROM current_weather WHERE cityId = :cityId")
    fun observeCurrent(cityId: String): Flow<CurrentWeatherEntity?>

    @Query("SELECT * FROM hourly_forecast WHERE cityId = :cityId ORDER BY timeEpoch ASC")
    fun observeHourly(cityId: String): Flow<List<HourlyEntity>>

    @Query("SELECT * FROM daily_forecast WHERE cityId = :cityId ORDER BY dateEpoch ASC")
    fun observeDaily(cityId: String): Flow<List<DailyEntity>>

    // --- One-shot reads (used by the sync worker) ---

    @Query("SELECT * FROM current_weather WHERE cityId = :cityId")
    suspend fun getCurrent(cityId: String): CurrentWeatherEntity?

    @Query("SELECT * FROM hourly_forecast WHERE cityId = :cityId ORDER BY timeEpoch ASC")
    suspend fun getHourly(cityId: String): List<HourlyEntity>

    @Query("SELECT * FROM daily_forecast WHERE cityId = :cityId ORDER BY dateEpoch ASC")
    suspend fun getDaily(cityId: String): List<DailyEntity>

    // --- Writes ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCurrent(current: CurrentWeatherEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourly(hourly: List<HourlyEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDaily(daily: List<DailyEntity>)

    @Query("DELETE FROM hourly_forecast WHERE cityId = :cityId")
    suspend fun clearHourly(cityId: String)

    @Query("DELETE FROM daily_forecast WHERE cityId = :cityId")
    suspend fun clearDaily(cityId: String)
}
