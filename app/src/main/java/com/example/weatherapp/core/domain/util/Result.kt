package com.example.weatherapp.core.domain.util

/**
 * Generic typed result used across every layer (data, domain, presentation, validation).
 * Success carries data [D]; failure carries a typed [Error] [E]. Expected failures are
 * always represented as [Result.Error] — never thrown.
 */
sealed interface Result<out D, out E : Error> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E : com.example.weatherapp.core.domain.util.Error>(val error: E) :
        Result<Nothing, E>
}

/** A [Result] that carries no meaningful success payload. */
typealias EmptyResult<E> = Result<Unit, E>

inline fun <T, E : Error, R> Result<T, E>.map(map: (T) -> R): Result<R, E> {
    return when (this) {
        is Result.Error -> Result.Error(error)
        is Result.Success -> Result.Success(map(data))
    }
}

inline fun <T, E : Error> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> {
    return when (this) {
        is Result.Error -> this
        is Result.Success -> {
            action(data)
            this
        }
    }
}

inline fun <T, E : Error> Result<T, E>.onFailure(action: (E) -> Unit): Result<T, E> {
    return when (this) {
        is Result.Error -> {
            action(error)
            this
        }
        is Result.Success -> this
    }
}

fun <T, E : Error> Result<T, E>.asEmptyResult(): EmptyResult<E> = map { }

/** Returns the success data or `null` when this is an [Result.Error]. */
fun <T, E : Error> Result<T, E>.getOrNull(): T? = (this as? Result.Success)?.data
