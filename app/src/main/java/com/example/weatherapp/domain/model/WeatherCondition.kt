package com.example.weatherapp.domain.model

/**
 * Coarse grouping of WeatherAPI condition codes. Drives both the icon shown and the
 * dynamic theme/gradient. Kept in the domain layer because it is pure, framework-free logic.
 */
enum class WeatherConditionType {
    CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    FOG,
    DRIZZLE,
    RAIN,
    SLEET,
    SNOW,
    THUNDER;

    companion object {
        /**
         * Maps a WeatherAPI condition [code] (see https://www.weatherapi.com/docs/weather_conditions.json)
         * to a coarse [WeatherConditionType].
         */
        fun fromCode(code: Int): WeatherConditionType = when (code) {
            1000 -> CLEAR
            1003 -> PARTLY_CLOUDY
            1006, 1009 -> CLOUDY
            1030, 1135, 1147 -> FOG
            1063, 1150, 1153, 1180, 1183, 1240 -> DRIZZLE
            1186, 1189, 1192, 1195, 1243, 1246 -> RAIN
            1069, 1072, 1168, 1171, 1198, 1201, 1204, 1207,
            1249, 1252, 1237, 1261, 1264 -> SLEET
            1066, 1114, 1117, 1210, 1213, 1216, 1219,
            1222, 1225, 1255, 1258 -> SNOW
            1087, 1273, 1276, 1279, 1282 -> THUNDER
            else -> CLOUDY
        }
    }
}

/**
 * A resolved weather condition: the raw API [code] + human [text], whether it is currently
 * daytime, and the derived [type] used for theming and icon selection.
 */
data class WeatherCondition(
    val code: Int,
    val text: String,
    val isDay: Boolean,
    val type: WeatherConditionType = WeatherConditionType.fromCode(code)
)
