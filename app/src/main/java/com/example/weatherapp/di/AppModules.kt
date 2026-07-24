package com.example.weatherapp.di

import androidx.room.Room
import androidx.work.WorkManager
import com.example.weatherapp.BuildConfig
import com.example.weatherapp.core.domain.util.Clock
import com.example.weatherapp.core.domain.util.SystemClock
import com.example.weatherapp.data.datasource.RetrofitWeatherRemoteDataSource
import com.example.weatherapp.data.datasource.RoomWeatherLocalDataSource
import com.example.weatherapp.data.datasource.WeatherLocalDataSource
import com.example.weatherapp.data.datasource.WeatherRemoteDataSource
import com.example.weatherapp.data.local.WeatherDatabase
import com.example.weatherapp.data.remote.ApiKeyInterceptor
import com.example.weatherapp.data.remote.WeatherApi
import com.example.weatherapp.data.repository.OfflineFirstWeatherRepository
import com.example.weatherapp.data.sync.SyncScheduler
import com.example.weatherapp.data.sync.WeatherSyncWorker
import com.example.weatherapp.domain.policy.RefreshPolicy
import com.example.weatherapp.domain.repository.WeatherRepository
import com.example.weatherapp.presentation.cities.CitiesViewModel
import com.example.weatherapp.presentation.weather.WeatherViewModel
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory

val networkModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }
    }

    single {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }
        OkHttpClient.Builder()
            .addInterceptor(ApiKeyInterceptor(BuildConfig.WEATHER_API_KEY))
            .addInterceptor(logging)
            .build()
    }

    single<WeatherApi> {
        Retrofit.Builder()
            .baseUrl(BuildConfig.WEATHER_BASE_URL)
            .client(get())
            .addConverterFactory(
                get<Json>().asConverterFactory("application/json".toMediaType())
            )
            .build()
            .create(WeatherApi::class.java)
    }
}

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            WeatherDatabase::class.java,
            WeatherDatabase.NAME
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
    single { get<WeatherDatabase>().cityDao() }
    single { get<WeatherDatabase>().weatherDao() }
}

val dataModule = module {
    single<Clock> { SystemClock() }
    single { RefreshPolicy() }

    single<WeatherRemoteDataSource> { RetrofitWeatherRemoteDataSource(get()) }
    single<WeatherLocalDataSource> { RoomWeatherLocalDataSource(get(), get(), get()) }
    single<WeatherRepository> {
        OfflineFirstWeatherRepository(
            remote = get(),
            local = get(),
            refreshPolicy = get(),
            clock = get()
        )
    }

    single { WorkManager.getInstance(androidContext()) }
    single { SyncScheduler(get()) }
}

val presentationModule = module {
    viewModelOf(::WeatherViewModel)
    viewModelOf(::CitiesViewModel)
}

val workerModule = module {
    worker { WeatherSyncWorker(get(), get(), get()) }
}

/** All Koin modules, assembled here and registered in the Application. */
val appModules = listOf(
    networkModule,
    databaseModule,
    dataModule,
    presentationModule,
    workerModule
)
