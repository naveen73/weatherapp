package com.example.weatherapp.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.weatherapp.core.domain.util.Result
import com.example.weatherapp.domain.repository.WeatherRepository

/**
 * Periodic background sync. Refreshes every saved city (forcing a network fetch) so caches
 * never go stale and the app always opens on recent data, online or off.
 */
class WeatherSyncWorker(
    context: Context,
    params: WorkerParameters,
    private val repository: WeatherRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): androidx.work.ListenableWorker.Result {
        val refreshResult = repository.refreshAll(force = true)

        return if (refreshResult is Result.Error && runAttemptCount < MAX_RETRIES) {
            // Transient failure (e.g. no connectivity during the window) — let WorkManager retry.
            androidx.work.ListenableWorker.Result.retry()
        } else {
            androidx.work.ListenableWorker.Result.success()
        }
    }

    companion object {
        const val WORK_NAME = "weather_periodic_sync"
        private const val MAX_RETRIES = 3
    }
}
