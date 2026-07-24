package com.example.weatherapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.weatherapp.data.local.entity.CityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {

    @Query("SELECT * FROM cities ORDER BY orderIndex ASC")
    fun observeCities(): Flow<List<CityEntity>>

    @Query("SELECT * FROM cities WHERE id = :cityId")
    fun observeCity(cityId: String): Flow<CityEntity?>

    @Query("SELECT * FROM cities ORDER BY orderIndex ASC")
    suspend fun getCities(): List<CityEntity>

    @Query("SELECT * FROM cities WHERE id = :cityId")
    suspend fun getCity(cityId: String): CityEntity?

    @Query("SELECT * FROM cities WHERE isCurrentLocation = 1 LIMIT 1")
    suspend fun getCurrentLocationCity(): CityEntity?

    @Query("SELECT COUNT(*) FROM cities")
    suspend fun count(): Int

    @Query("SELECT COALESCE(MAX(orderIndex), -1) FROM cities")
    suspend fun maxOrderIndex(): Int

    @Upsert
    suspend fun upsert(city: CityEntity)

    @Query("UPDATE cities SET lastFetchedEpoch = :epoch WHERE id = :cityId")
    suspend fun updateLastFetched(cityId: String, epoch: Long)

    @Query("DELETE FROM cities WHERE id = :cityId")
    suspend fun deleteById(cityId: String)
}
