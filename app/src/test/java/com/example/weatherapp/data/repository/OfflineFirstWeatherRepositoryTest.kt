package com.example.weatherapp.data.repository

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import com.example.weatherapp.core.domain.util.DataError
import com.example.weatherapp.core.domain.util.Result
import com.example.weatherapp.domain.model.CitySearchResult
import com.example.weatherapp.domain.policy.RefreshPolicy
import com.example.weatherapp.testutil.DtoTestData
import com.example.weatherapp.testutil.FakeClock
import com.example.weatherapp.testutil.FakeWeatherLocalDataSource
import com.example.weatherapp.testutil.FakeWeatherRemoteDataSource
import com.example.weatherapp.testutil.TestData
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.minutes

class OfflineFirstWeatherRepositoryTest {

    private val remote = FakeWeatherRemoteDataSource()
    private val local = FakeWeatherLocalDataSource()
    private val clock = FakeClock(now = 10_000_000L)
    private val repository = OfflineFirstWeatherRepository(
        remote = remote,
        local = local,
        refreshPolicy = RefreshPolicy(ttl = 30.minutes),
        clock = clock
    )

    private val ttlMillis = 30 * 60 * 1000L

    @Test
    fun `refresh skips network when cache is fresh`() = runTest {
        local.preloadCity(TestData.city(id = "c1", lastFetchedEpoch = clock.now - 1))

        val result = repository.refresh("c1", force = false)

        assertThat(result).isEqualTo(Result.Success(Unit))
        assertThat(remote.forecastQueries).hasSize(0)
    }

    @Test
    fun `refresh hits network when forced even if fresh`() = runTest {
        local.preloadCity(TestData.city(id = "c1", lastFetchedEpoch = clock.now))

        repository.refresh("c1", force = true)

        assertThat(remote.forecastQueries).hasSize(1)
        assertThat(local.replaceWeatherCalls).hasSize(1)
    }

    @Test
    fun `refresh hits network when cache is stale`() = runTest {
        local.preloadCity(TestData.city(id = "c1", lastFetchedEpoch = clock.now - ttlMillis))

        repository.refresh("c1", force = false)

        assertThat(remote.forecastQueries).hasSize(1)
    }

    @Test
    fun `refresh keeps cache on network failure`() = runTest {
        local.preloadCity(TestData.city(id = "c1", lastFetchedEpoch = null))
        remote.forecastResult = Result.Error(DataError.Network.NO_INTERNET)

        val result = repository.refresh("c1", force = true)

        assertThat(result).isEqualTo(Result.Error(DataError.Network.NO_INTERNET))
        assertThat(local.replaceWeatherCalls).hasSize(0)
        assertThat(local.getCity("c1")).isNotNull()
    }

    @Test
    fun `refresh unknown city returns local not found`() = runTest {
        val result = repository.refresh("missing", force = true)
        assertThat(result).isEqualTo(Result.Error(DataError.Local.NOT_FOUND))
    }

    @Test
    fun `ensureCurrentLocationCity creates a pinned current-location city`() = runTest {
        remote.forecastResult = Result.Success(DtoTestData.forecastResponse(lat = 40.0, lon = -74.0))

        val result = repository.ensureCurrentLocationCity()

        assertThat(result).isInstanceOf(Result.Success::class)
        assertThat(remote.forecastQueries.first()).isEqualTo("auto:ip")
        val city = local.getCurrentLocationCity()
        assertThat(city).isNotNull()
        assertThat(city!!.isCurrentLocation).isTrue()
        assertThat(city.id).isEqualTo("40.0,-74.0")
    }

    @Test
    fun `ensureCurrentLocationCity is a no-op when one already exists`() = runTest {
        local.preloadCity(TestData.city(id = "loc", isCurrentLocation = true))

        repository.ensureCurrentLocationCity()

        assertThat(remote.forecastQueries).hasSize(0)
    }

    @Test
    fun `addCity fetches and stores`() = runTest {
        val result = repository.addCity(
            CitySearchResult("paris", "Paris", "IDF", "France", 48.8, 2.3)
        )

        assertThat(result).isInstanceOf(Result.Success::class)
        assertThat(local.cityExists("paris")).isTrue()
    }

    @Test
    fun `addCity is idempotent`() = runTest {
        local.preloadCity(TestData.city(id = "paris"))

        repository.addCity(CitySearchResult("paris", "Paris", "IDF", "France", 48.8, 2.3))

        assertThat(remote.forecastQueries).hasSize(0)
    }

    @Test
    fun `removeCity deletes the city`() = runTest {
        local.preloadCity(TestData.city(id = "c1"))

        repository.removeCity("c1")

        assertThat(local.cityExists("c1")).isFalse()
    }

    @Test
    fun `searchCities maps results to domain`() = runTest {
        remote.searchResult = Result.Success(
            listOf(
                com.example.weatherapp.data.remote.dto.SearchLocationDto(
                    name = "Tokyo", region = "", country = "Japan", lat = 35.6, lon = 139.6, url = "tokyo-japan"
                )
            )
        )

        val result = repository.searchCities("Tok")

        assertThat(result).isInstanceOf(Result.Success::class)
        assertThat((result as Result.Success).data.first().id).isEqualTo("tokyo-japan")
    }
}
