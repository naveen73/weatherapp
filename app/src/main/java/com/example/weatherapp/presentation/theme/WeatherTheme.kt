package com.example.weatherapp.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BaseLight = lightColorScheme(
    primary = SkyBlue,
    secondary = SkyBlueLight,
    tertiary = Amber,
    background = LightBackground
)

private val BaseDark = darkColorScheme(
    primary = SkyBlueLight,
    secondary = SkyBlue,
    tertiary = Amber,
    background = DarkBackground
)

/**
 * Base app theme used at the root (around navigation). Individual weather pages further tint
 * their own [MaterialTheme] from a [WeatherPalette] based on the current condition.
 */
@Composable
fun WeatherAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) BaseDark else BaseLight,
        typography = WeatherTypography,
        content = content
    )
}

/**
 * Applies a weather-condition-driven [palette] as the current [MaterialTheme] color scheme,
 * so Material components (cards, text, chips) adapt to the weather for the active city.
 */
@Composable
fun WeatherConditionTheme(
    palette: WeatherPalette,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = palette.toColorScheme(),
        typography = WeatherTypography,
        content = content
    )
}

/** Slightly translucent card surface that reads well on top of the weather gradient. */
val WeatherPalette.cardColor: Color
    get() = if (useLightContent) Color.White.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.55f)
