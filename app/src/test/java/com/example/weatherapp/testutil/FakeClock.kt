package com.example.weatherapp.testutil

import com.example.weatherapp.core.domain.util.Clock

/** A controllable [Clock] for deterministic TTL / time-based tests. */
class FakeClock(var now: Long = 0L) : Clock {
    override fun nowEpochMillis(): Long = now
}
