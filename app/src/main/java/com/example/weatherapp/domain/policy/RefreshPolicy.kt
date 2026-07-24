package com.example.weatherapp.domain.policy

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Smart-refresh (TTL) policy. A city's cache is considered stale when it has never been
 * fetched, or when more than [ttl] has elapsed since [City.lastFetchedEpoch]. The repository
 * consults this before hitting the network so we "refresh only when needed".
 */
class RefreshPolicy(private val ttl: Duration = DEFAULT_TTL) {

    fun isStale(lastFetchedEpoch: Long?, nowEpochMillis: Long): Boolean {
        if (lastFetchedEpoch == null) return true
        val age = nowEpochMillis - lastFetchedEpoch
        return age >= ttl.inWholeMilliseconds
    }

    companion object {
        /** Current + forecast rarely change faster than this on WeatherAPI. */
        val DEFAULT_TTL: Duration = 30.minutes
    }
}
