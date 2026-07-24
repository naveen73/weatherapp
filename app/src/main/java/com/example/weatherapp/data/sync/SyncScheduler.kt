package com.example.weatherapp.data.sync

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Schedules the periodic [WeatherSyncWorker]. Uses a unique periodic work request so repeated
 * scheduling (e.g. on every app start) does not stack up duplicate work.
 */
class SyncScheduler(private val workManager: WorkManager) {

    fun schedule(intervalMinutes: Long = DEFAULT_INTERVAL_MINUTES) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = PeriodicWorkRequestBuilder<WeatherSyncWorker>(
            intervalMinutes, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WeatherSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    companion object {
        /** WorkManager enforces a 15-minute minimum period; 90 min balances freshness and battery. */
        const val DEFAULT_INTERVAL_MINUTES = 90L
    }
}
