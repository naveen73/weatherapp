package com.example.weatherapp.presentation.weather

import androidx.compose.runtime.Stable
import com.example.weatherapp.core.presentation.UiText

/** UI state for the home weather screen (a pager over saved cities). */
@Stable
data class WeatherState(
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val pages: List<CityWeatherUi> = emptyList(),
    val currentPage: Int = 0
) {
    val hasNoCities: Boolean get() = !isInitialLoading && pages.isEmpty()
}

/** User-triggered actions on the weather screen. */
sealed interface WeatherAction {
    data class OnPageChanged(val page: Int) : WeatherAction
    data object OnRefresh : WeatherAction
    data object OnRetry : WeatherAction
    data object OnManageCitiesClick : WeatherAction
}

/** One-time side effects emitted by the weather ViewModel. */
sealed interface WeatherEvent {
    data object NavigateToCities : WeatherEvent
    data class ShowMessage(val message: UiText) : WeatherEvent
}
