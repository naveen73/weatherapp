package com.example.weatherapp.data.remote

import com.example.weatherapp.core.domain.util.DataError
import com.example.weatherapp.core.domain.util.Result
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Wraps a Retrofit suspend call, converting thrown exceptions into typed
 * [DataError.Network] values so callers never see raw exceptions for expected failures.
 * [CancellationException] is rethrown so coroutine cancellation keeps working.
 */
suspend fun <T> safeApiCall(execute: suspend () -> T): Result<T, DataError.Network> {
    return try {
        Result.Success(execute())
    } catch (e: CancellationException) {
        throw e
    } catch (e: UnknownHostException) {
        Result.Error(DataError.Network.NO_INTERNET)
    } catch (e: SocketTimeoutException) {
        Result.Error(DataError.Network.REQUEST_TIMEOUT)
    } catch (e: IOException) {
        // Connection reset / no route / offline, etc.
        Result.Error(DataError.Network.NO_INTERNET)
    } catch (e: HttpException) {
        Result.Error(e.code().toNetworkError())
    } catch (e: SerializationException) {
        Result.Error(DataError.Network.SERIALIZATION)
    } catch (e: Exception) {
        Result.Error(DataError.Network.UNKNOWN)
    }
}

/**
 * Maps an HTTP status code to a [DataError.Network]. WeatherAPI returns 400 with error code
 * 1006 for an unknown location, so 400 is treated as "not found".
 */
fun Int.toNetworkError(): DataError.Network = when (this) {
    400 -> DataError.Network.NOT_FOUND
    401 -> DataError.Network.UNAUTHORIZED
    403 -> DataError.Network.FORBIDDEN
    404 -> DataError.Network.NOT_FOUND
    408 -> DataError.Network.REQUEST_TIMEOUT
    409 -> DataError.Network.CONFLICT
    413 -> DataError.Network.PAYLOAD_TOO_LARGE
    429 -> DataError.Network.TOO_MANY_REQUESTS
    503 -> DataError.Network.SERVICE_UNAVAILABLE
    in 500..599 -> DataError.Network.SERVER_ERROR
    else -> DataError.Network.UNKNOWN
}
