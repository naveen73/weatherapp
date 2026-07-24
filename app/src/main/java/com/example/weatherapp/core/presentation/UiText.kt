package com.example.weatherapp.core.presentation

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

/**
 * Wraps a string that either is, or could be, a string resource — so error/UI messages can
 * be localised and resolved lazily against a [Context].
 */
sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    class StringResource(
        @param:StringRes val id: Int,
        val args: Array<Any> = emptyArray()
    ) : UiText

    @Composable
    fun asString(): String = when (this) {
        is DynamicString -> value
        is StringResource -> stringResource(id, *args)
    }

    fun asString(context: Context): String = when (this) {
        is DynamicString -> value
        is StringResource -> context.getString(id, *args)
    }
}
