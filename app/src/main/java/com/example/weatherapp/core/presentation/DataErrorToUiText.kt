package com.example.weatherapp.core.presentation

import com.example.weatherapp.R
import com.example.weatherapp.core.domain.util.DataError

/** Maps data-layer errors to user-facing, localisable messages. */
fun DataError.toUiText(): UiText = when (this) {
    DataError.Network.NO_INTERNET -> UiText.StringResource(R.string.error_no_internet)
    DataError.Network.REQUEST_TIMEOUT -> UiText.StringResource(R.string.error_timeout)
    DataError.Network.NOT_FOUND -> UiText.StringResource(R.string.error_location_not_found)
    DataError.Network.TOO_MANY_REQUESTS -> UiText.StringResource(R.string.error_too_many_requests)
    DataError.Network.UNAUTHORIZED,
    DataError.Network.FORBIDDEN -> UiText.StringResource(R.string.error_unauthorized)
    DataError.Network.SERVER_ERROR,
    DataError.Network.SERVICE_UNAVAILABLE -> UiText.StringResource(R.string.error_server)
    DataError.Network.SERIALIZATION -> UiText.StringResource(R.string.error_serialization)
    DataError.Local.DISK_FULL -> UiText.StringResource(R.string.error_disk_full)
    DataError.Local.NOT_FOUND -> UiText.StringResource(R.string.error_no_cached_data)
    else -> UiText.StringResource(R.string.error_unknown)
}
