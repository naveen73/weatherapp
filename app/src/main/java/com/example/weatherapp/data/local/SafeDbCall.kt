package com.example.weatherapp.data.local

import android.database.sqlite.SQLiteFullException
import com.example.weatherapp.core.domain.util.DataError
import com.example.weatherapp.core.domain.util.Result
import kotlinx.coroutines.CancellationException

/** Wraps a database operation, converting exceptions into typed [DataError.Local]. */
suspend fun <T> safeDbCall(execute: suspend () -> T): Result<T, DataError.Local> {
    return try {
        Result.Success(execute())
    } catch (e: CancellationException) {
        throw e
    } catch (e: SQLiteFullException) {
        Result.Error(DataError.Local.DISK_FULL)
    } catch (e: Exception) {
        Result.Error(DataError.Local.UNKNOWN)
    }
}
