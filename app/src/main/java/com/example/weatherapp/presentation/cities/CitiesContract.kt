package com.example.weatherapp.presentation.cities

import androidx.compose.runtime.Stable
import com.example.weatherapp.core.presentation.UiText
import com.example.weatherapp.domain.model.CitySearchResult

@Stable
data class SavedCityUi(
    val id: String,
    val name: String,
    val subtitle: String,
    val isCurrentLocation: Boolean
)

@Stable
data class CitiesState(
    val query: String = "",
    val isSearching: Boolean = false,
    val results: List<CitySearchResult> = emptyList(),
    val savedCities: List<SavedCityUi> = emptyList()
)

sealed interface CitiesAction {
    data class OnQueryChange(val query: String) : CitiesAction
    data class OnResultClick(val result: CitySearchResult) : CitiesAction
    data class OnRemoveCity(val cityId: String) : CitiesAction
    data object OnBack : CitiesAction
}

sealed interface CitiesEvent {
    data object NavigateBack : CitiesEvent
    data class ShowMessage(val message: UiText) : CitiesEvent
}
