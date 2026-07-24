package com.example.weatherapp.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Appends the WeatherAPI `key` query parameter to every request, so the API interface and
 * data source never deal with the secret. The key is sourced from BuildConfig (backed by
 * local.properties).
 */
class ApiKeyInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val url = original.url.newBuilder()
            .addQueryParameter("key", apiKey)
            .build()
        val request = original.newBuilder().url(url).build()
        return chain.proceed(request)
    }
}
