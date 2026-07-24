package com.example.weatherapp

import android.app.Application
import com.example.weatherapp.data.sync.SyncScheduler
import com.example.weatherapp.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

class WeatherApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val koinApp = startKoin {
            androidLogger()
            androidContext(this@WeatherApp)
            // Register the Koin WorkManager factory so workers get their dependencies injected.
            workManagerFactory()
            modules(appModules)
        }

        // Schedule periodic background sync.
        koinApp.koin.get<SyncScheduler>().schedule()
    }
}
