package com.example.weatherapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** One entry from `GET v1/search.json`. */
@Serializable
data class SearchLocationDto(
    @SerialName("id") val id: Long = 0,
    @SerialName("name") val name: String,
    @SerialName("region") val region: String = "",
    @SerialName("country") val country: String = "",
    @SerialName("lat") val lat: Double,
    @SerialName("lon") val lon: Double,
    @SerialName("url") val url: String = ""
)
