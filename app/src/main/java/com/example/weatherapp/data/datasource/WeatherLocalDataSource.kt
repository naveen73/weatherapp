package com.example.weatherapp.data.datasource

import androidx.room.withTransaction
import com.example.weatherapp.data.local.WeatherDatabase
import com.example.weatherapp.data.local.dao.CityDao
import com.example.weatherapp.data.local.dao.WeatherDao
import com.example.weatherapp.data.local.mapper.toDomain
import com.example.weatherapp.data.local.mapper.toEntity
import com.example.weatherapp.domain.model.City
import com.example.weatherapp.domain.model.CityWeather
import com.example.weatherapp.domain.model.CurrentWeather
import com.example.weatherapp.domain.model.DailyForecast
import com.example.weatherapp.domain.model.HourlyForecast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/** Local (Room) source of truth for weather data. */
interface WeatherLocalDataSource {
    fun observeCities(): Flow<List<City>>
    fun observeCityWeather(cityId: String): Flow<CityWeather?>
    suspend fun getCities(): List<City>
    suspend fun getCityWeather(cityId: String): CityWeather?
    suspend fun getCity(cityId: String): City?
    suspend fun getCurrentLocationCity(): City?
    suspend fun cityExists(cityId: String): Boolean
    suspend fun nextOrderIndex(): Int
    suspend fun upsertCity(city: City)
    suspend fun deleteCity(cityId: String)

    /**
     * Atomically replace all cached weather for a city and stamp its fetch time. The city row
     * is upserted first (so foreign keys resolve), then forecast rows are cleared and inserted.
     */
    suspend fun replaceWeather(
        city: City,
        current: CurrentWeather,
        hourly: List<HourlyForecast>,
        daily: List<DailyForecast>,
        fetchedEpoch: Long
    )
}

class RoomWeatherLocalDataSource(
    private val database: WeatherDatabase,
    private val cityDao: CityDao,
    private val weatherDao: WeatherDao
) : WeatherLocalDataSource {

    override fun observeCities(): Flow<List<City>> =
        cityDao.observeCities().map { list -> list.map { it.toDomain() } }

    override fun observeCityWeather(cityId: String): Flow<CityWeather?> = combine(
        cityDao.observeCity(cityId),
        weatherDao.observeCurrent(cityId),
        weatherDao.observeHourly(cityId),
        weatherDao.observeDaily(cityId)
    ) { city, current, hourly, daily ->
        if (city == null || current == null) {
            null
        } else {
            CityWeather(
                city = city.toDomain(),
                current = current.toDomain(),
                hourly = hourly.map { it.toDomain() },
                daily = daily.map { it.toDomain() }
            )
        }
    }

    override suspend fun getCities(): List<City> = cityDao.getCities().map { it.toDomain() }

    override suspend fun getCityWeather(cityId: String): CityWeather? {
        val city = cityDao.getCity(cityId) ?: return null
        val current = weatherDao.getCurrent(cityId) ?: return null
        return CityWeather(
            city = city.toDomain(),
            current = current.toDomain(),
            hourly = weatherDao.getHourly(cityId).map { it.toDomain() },
            daily = weatherDao.getDaily(cityId).map { it.toDomain() }
        )
    }

    override suspend fun getCity(cityId: String): City? = cityDao.getCity(cityId)?.toDomain()

    override suspend fun getCurrentLocationCity(): City? =
        cityDao.getCurrentLocationCity()?.toDomain()

    override suspend fun cityExists(cityId: String): Boolean = cityDao.getCity(cityId) != null

    override suspend fun nextOrderIndex(): Int = cityDao.maxOrderIndex() + 1

    override suspend fun upsertCity(city: City) = cityDao.upsert(city.toEntity())

    override suspend fun deleteCity(cityId: String) = cityDao.deleteById(cityId)

    override suspend fun replaceWeather(
        city: City,
        current: CurrentWeather,
        hourly: List<HourlyForecast>,
        daily: List<DailyForecast>,
        fetchedEpoch: Long
    ) {
        database.withTransaction {
            cityDao.upsert(city.toEntity())
            weatherDao.upsertCurrent(current.toEntity(city.id))
            weatherDao.clearHourly(city.id)
            weatherDao.insertHourly(hourly.map { it.toEntity(city.id) })
            weatherDao.clearDaily(city.id)
            weatherDao.insertDaily(daily.map { it.toEntity(city.id) })
            cityDao.updateLastFetched(city.id, fetchedEpoch)
        }
    }
}
