package com.example.weatherapp.data.sync

import android.app.Application
import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.example.weatherapp.core.domain.util.DataError
import com.example.weatherapp.core.domain.util.EmptyResult
import com.example.weatherapp.core.domain.util.Result
import com.example.weatherapp.domain.model.City
import com.example.weatherapp.domain.model.CitySearchResult
import com.example.weatherapp.domain.model.CityWeather
import com.example.weatherapp.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Robolectric test for [WeatherSyncWorker] — runs on the JVM. Uses a plain [Application]
 * (not [com.example.weatherapp.WeatherApp]) so Koin/WorkManager are not started by the app.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class WeatherSyncWorkerRobolectricTest {

    private val context: Context get() = RuntimeEnvironment.getApplication()

    private class StubRepository(
        private val refresh: EmptyResult<DataError>
    ) : WeatherRepository {
        override fun observeSavedCities(): Flow<List<City>> = flowOf(emptyList())
        override fun observeCityWeather(cityId: String): Flow<CityWeather?> = flowOf(null)
        override suspend fun getSavedCities(): List<City> = emptyList()
        override suspend fun getCityWeather(cityId: String): CityWeather? = null
        override suspend fun refresh(cityId: String, force: Boolean) = refresh
        override suspend fun refreshAll(force: Boolean) = refresh
        override suspend fun ensureCurrentLocationCity(): EmptyResult<DataError> = Result.Success(Unit)
        override suspend fun addCity(result: CitySearchResult): EmptyResult<DataError> = Result.Success(Unit)
        override suspend fun removeCity(cityId: String): EmptyResult<DataError> = Result.Success(Unit)
        override suspend fun searchCities(query: String) = Result.Success(emptyList<CitySearchResult>())
    }

    private fun buildWorker(repository: WeatherRepository): WeatherSyncWorker {
        val factory = object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters
            ) = WeatherSyncWorker(appContext, workerParameters, repository)
        }
        return TestListenableWorkerBuilder<WeatherSyncWorker>(context)
            .setWorkerFactory(factory)
            .build()
    }

    @Test
    fun successfulSync_returnsSuccess() = runBlocking {
        val worker = buildWorker(StubRepository(Result.Success(Unit)))
        assertEquals(ListenableWorker.Result.success(), worker.doWork())
    }

    @Test
    fun networkFailure_retries() = runBlocking {
        val worker = buildWorker(StubRepository(Result.Error(DataError.Network.NO_INTERNET)))
        assertEquals(ListenableWorker.Result.retry(), worker.doWork())
    }
}
