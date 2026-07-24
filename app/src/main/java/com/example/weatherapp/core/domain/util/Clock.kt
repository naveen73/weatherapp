package com.example.weatherapp.core.domain.util

/**
 * Abstraction over the system clock so TTL / staleness logic is deterministic in unit tests.
 * Production uses [SystemClock]; tests inject a fake returning a controlled time.
 */
interface Clock {
    fun nowEpochMillis(): Long
}

class SystemClock : Clock {
    override fun nowEpochMillis(): Long = System.currentTimeMillis()
}
