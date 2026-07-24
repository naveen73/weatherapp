package com.example.weatherapp.presentation.weather

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotEmpty
import assertk.assertions.isTrue
import com.example.weatherapp.core.domain.util.DataError
import com.example.weatherapp.core.domain.util.Result
import com.example.weatherapp.testutil.FakeClock
import com.example.weatherapp.testutil.FakeWeatherRepository
import com.example.weatherapp.testutil.TestData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WeatherViewModelTest {

    private val repository = FakeWeatherRepository()
    private val clock = FakeClock(now = 1_700_000_000_000L)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = WeatherViewModel(repository, clock)

    @Test
    fun `bootstrap seeds location and refreshes stale caches`() {
        viewModel()
        assertThat(repository.ensureCalled).isTrue()
        assertThat(repository.refreshAllForce).isEqualTo(false)
    }

    @Test
    fun `seeded city is exposed as a page`() {
        repository.seed(TestData.cityWeather(city = TestData.city(name = "London")))

        val vm = viewModel()

        val state = vm.state.value
        assertThat(state.pages).isNotEmpty()
        assertThat(state.pages.first().cityName).isEqualTo("London")
        assertThat(state.pages.first().temperature).isEqualTo("18°")
        assertThat(state.isInitialLoading).isFalse()
    }

    @Test
    fun `refresh targets the current page city with force`() {
        repository.seed(TestData.cityWeather(city = TestData.city(id = "c1")))
        val vm = viewModel()

        vm.onAction(WeatherAction.OnRefresh)

        assertThat(repository.refreshCalls).contains("c1" to true)
    }

    @Test
    fun `manage cities click emits navigation event`() = runTest {
        val vm = viewModel()
        vm.events.test {
            vm.onAction(WeatherAction.OnManageCitiesClick)
            assertThat(awaitItem()).isEqualTo(WeatherEvent.NavigateToCities)
        }
    }

    @Test
    fun `refresh failure emits a message event`() = runTest {
        repository.seed(TestData.cityWeather(city = TestData.city(id = "c1")))
        repository.refreshResult = Result.Error(DataError.Network.NO_INTERNET)
        val vm = viewModel()

        vm.events.test {
            vm.onAction(WeatherAction.OnRefresh)
            assertThat(awaitItem()).isInstanceOf(WeatherEvent.ShowMessage::class)
        }
    }
}
