package com.example.weatherapp.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.example.weatherapp.domain.model.WeatherConditionType

/**
 * A weather-condition-driven visual palette. Drives the animated background gradient and the
 * per-city Material [ColorScheme], so the whole screen re-themes based on the current weather.
 */
data class WeatherPalette(
    val gradient: List<Color>,
    val accent: Color,
    val useLightContent: Boolean
) {
    val content: Color get() = if (useLightContent) Color.White else Color(0xFF1A2530)
    val contentMuted: Color get() = content.copy(alpha = 0.7f)

    /** Builds a Material [ColorScheme] tinted for this weather condition. */
    fun toColorScheme(): ColorScheme {
        val surface = gradient.last()
        return if (useLightContent) {
            darkColorScheme(
                primary = accent,
                onPrimary = Color(0xFF10151F),
                background = gradient.first(),
                onBackground = content,
                surface = surface.copy(alpha = 0.35f),
                onSurface = content,
                surfaceVariant = surface.copy(alpha = 0.25f),
                onSurfaceVariant = contentMuted
            )
        } else {
            lightColorScheme(
                primary = accent,
                onPrimary = Color.White,
                background = gradient.first(),
                onBackground = content,
                surface = Color.White.copy(alpha = 0.45f),
                onSurface = content,
                surfaceVariant = Color.White.copy(alpha = 0.30f),
                onSurfaceVariant = contentMuted
            )
        }
    }

    companion object {
        /** Selects the palette for a coarse condition [type] and day/night. */
        fun forCondition(type: WeatherConditionType, isDay: Boolean): WeatherPalette = when (type) {
            WeatherConditionType.CLEAR ->
                if (isDay) light(listOf(Color(0xFF2E86DE), Color(0xFF54A0FF), Color(0xFF7FC1FF)), Color(0xFFFFD54F))
                else light(listOf(Color(0xFF0B1633), Color(0xFF14224A), Color(0xFF243B6B)), Color(0xFFFFCA28))

            WeatherConditionType.PARTLY_CLOUDY ->
                if (isDay) light(listOf(Color(0xFF4A7FC0), Color(0xFF6FA0D0), Color(0xFF9BB8D3)), Color(0xFFFFD54F))
                else light(listOf(Color(0xFF16233E), Color(0xFF243352), Color(0xFF3A4A66)), Color(0xFFB0BEC5))

            WeatherConditionType.CLOUDY ->
                if (isDay) light(listOf(Color(0xFF5C7A8C), Color(0xFF78909C), Color(0xFF9EB2BD)), Color(0xFFCFD8DC))
                else light(listOf(Color(0xFF1C2732), Color(0xFF2C3A47), Color(0xFF3E4E5C)), Color(0xFF90A4AE))

            WeatherConditionType.FOG ->
                if (isDay) dark(listOf(Color(0xFF9EAEB8), Color(0xFFB8C4CC), Color(0xFFD3DCE1)), Color(0xFF607D8B))
                else light(listOf(Color(0xFF2B343B), Color(0xFF3C474F), Color(0xFF515D66)), Color(0xFF90A4AE))

            WeatherConditionType.DRIZZLE, WeatherConditionType.RAIN ->
                if (isDay) light(listOf(Color(0xFF37474F), Color(0xFF4B636E), Color(0xFF62808D)), Color(0xFF4FC3F7))
                else light(listOf(Color(0xFF14202B), Color(0xFF20303C), Color(0xFF30454F)), Color(0xFF4FC3F7))

            WeatherConditionType.SLEET ->
                if (isDay) light(listOf(Color(0xFF4A6572), Color(0xFF63818E), Color(0xFF89A6B2)), Color(0xFF80DEEA))
                else light(listOf(Color(0xFF18262E), Color(0xFF263A44), Color(0xFF37525E)), Color(0xFF80DEEA))

            WeatherConditionType.SNOW ->
                dark(listOf(Color(0xFF8FA8BC), Color(0xFFB6C7D6), Color(0xFFE1EAF1)), Color(0xFF4A6572))

            WeatherConditionType.THUNDER ->
                light(listOf(Color(0xFF1A1F2B), Color(0xFF2A2F45), Color(0xFF3D3A5C)), Color(0xFFB388FF))
        }

        private fun light(gradient: List<Color>, accent: Color) =
            WeatherPalette(gradient = gradient, accent = accent, useLightContent = true)

        private fun dark(gradient: List<Color>, accent: Color) =
            WeatherPalette(gradient = gradient, accent = accent, useLightContent = false)

        /** Neutral fallback used before any weather has loaded. */
        val Neutral = light(
            gradient = listOf(SkyBlue, SkyBlueLight, Color(0xFFA8D2FF)),
            accent = Amber
        )
    }
}
