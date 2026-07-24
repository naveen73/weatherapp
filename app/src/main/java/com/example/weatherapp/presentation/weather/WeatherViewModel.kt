package com.example.weatherapp.presentation.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.core.domain.util.Clock
import com.example.weatherapp.core.domain.util.onFailure
import com.example.weatherapp.core.presentation.toUiText
import com.example.weatherapp.domain.model.CityWeather
import com.example.weatherapp.domain.repository.WeatherRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WeatherViewModel(
    private val repository: WeatherRepository,
    private val clock: Clock
) : ViewModel() {

    private val _state = MutableStateFlow(WeatherState())
    val state = _state.asStateFlow()

    private val _events = Channel<WeatherEvent>()
    val events = _events.receiveAsFlow()


    init {
        observeWeather()
        bootstrap()
    }

    fun onAction(action: WeatherAction) {
        when (action) {
            is WeatherAction.OnPageChanged -> _state.update { it.copy(currentPage = action.page) }
            WeatherAction.OnRefresh -> refreshCurrent()
            WeatherAction.OnRetry -> bootstrap()
            WeatherAction.OnManageCitiesClick -> viewModelScope.launch {
                _events.send(WeatherEvent.NavigateToCities)
            }
        }
    }

    /** Seed the auto-location city on first launch, then refresh any stale caches. */
    private fun bootstrap() {
        viewModelScope.launch {
            _state.update { it.copy(isInitialLoading = true) }
            repository.ensureCurrentLocationCity()
                .onFailure { error -> _events.send(WeatherEvent.ShowMessage(error.toUiText())) }
            repository.refreshAll(force = false)
            _state.update { it.copy(isInitialLoading = false) }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observeWeather() {
        val weatherFlow = repository.observeSavedCities()
            .flatMapLatest { cities ->
                if (cities.isEmpty()) {
                    flowOf(emptyList<CityWeather?>())
                } else {
                    combine(cities.map { repository.observeCityWeather(it.id) }) { it.toList() }
                }
            }

        viewModelScope.launch {
            weatherFlow.map { weathers ->
                val nowSeconds = clock.nowEpochMillis() / 1000
                weathers.filterNotNull().map { it.toUi(nowSeconds) }
            }.collect { pages ->
                _state.update { current ->
                    current.copy(
                        pages = pages,
                        currentPage = current.currentPage
                            .coerceIn(0, (pages.size - 1).coerceAtLeast(0))
                    )
                }
            }
        }
    }

    private fun refreshCurrent() {
        val cityId = _state.value.pages.getOrNull(_state.value.currentPage)?.cityId
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            val result = if (cityId != null) {
                repository.refresh(cityId, force = true)
            } else {
                repository.refreshAll(force = true)
            }
            result.onFailure { error -> _events.send(WeatherEvent.ShowMessage(error.toUiText())) }
            _state.update { it.copy(isRefreshing = false) }
        }
    }
}
