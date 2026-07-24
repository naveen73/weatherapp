package com.example.weatherapp.presentation.cities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.core.domain.util.Result
import com.example.weatherapp.core.domain.util.onFailure
import com.example.weatherapp.core.presentation.toUiText
import com.example.weatherapp.domain.model.City
import com.example.weatherapp.domain.model.CitySearchResult
import com.example.weatherapp.domain.repository.WeatherRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CitiesViewModel(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CitiesState())
    val state = _state.asStateFlow()

    private val _events = Channel<CitiesEvent>()
    val events = _events.receiveAsFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        observeSavedCities()
        observeSearchQuery()
    }

    fun onAction(action: CitiesAction) {
        when (action) {
            is CitiesAction.OnQueryChange -> {
                _state.update { it.copy(query = action.query) }
                queryFlow.value = action.query
            }
            is CitiesAction.OnResultClick -> addCity(action.result)
            is CitiesAction.OnRemoveCity -> removeCity(action.cityId)
            CitiesAction.OnBack -> viewModelScope.launch { _events.send(CitiesEvent.NavigateBack) }
        }
    }

    private fun observeSavedCities() {
        viewModelScope.launch {
            repository.observeSavedCities().collectLatest { cities ->
                _state.update { it.copy(savedCities = cities.map { city -> city.toUi() }) }
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            queryFlow
                .debounce(SEARCH_DEBOUNCE_MS)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.length < MIN_QUERY_LENGTH) {
                        _state.update { it.copy(results = emptyList(), isSearching = false) }
                        return@collectLatest
                    }
                    _state.update { it.copy(isSearching = true) }
                    when (val result = repository.searchCities(query)) {
                        is Result.Success ->
                            _state.update { it.copy(results = result.data, isSearching = false) }
                        is Result.Error -> {
                            _state.update { it.copy(isSearching = false) }
                            _events.send(CitiesEvent.ShowMessage(result.error.toUiText()))
                        }
                    }
                }
        }
    }

    private fun addCity(result: CitySearchResult) {
        viewModelScope.launch {
            repository.addCity(result)
                .onFailure { error -> _events.send(CitiesEvent.ShowMessage(error.toUiText())) }
            // Clear the search once added so the saved list is front and centre.
            _state.update { it.copy(query = "", results = emptyList()) }
            queryFlow.value = ""
        }
    }

    private fun removeCity(cityId: String) {
        viewModelScope.launch {
            repository.removeCity(cityId)
                .onFailure { error -> _events.send(CitiesEvent.ShowMessage(error.toUiText())) }
        }
    }

    private fun City.toUi(): SavedCityUi = SavedCityUi(
        id = id,
        name = displayName,
        subtitle = listOf(region, country).filter { it.isNotBlank() }.joinToString(", "),
        isCurrentLocation = isCurrentLocation
    )

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 350L
        const val MIN_QUERY_LENGTH = 2
    }
}
