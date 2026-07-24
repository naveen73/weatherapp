package com.example.weatherapp.presentation.weather.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.presentation.weather.CityWeatherUi

/** The hero block: city, big temperature, condition text, and today's high / low. */
@Composable
fun CurrentWeatherCard(
    weather: CityWeatherUi,
    content: Color,
    contentMuted: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = weather.cityName,
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold,
            color = content,
            textAlign = TextAlign.Center
        )
        if (weather.region.isNotBlank()) {
            Text(text = weather.region, fontSize = 14.sp, color = contentMuted)
        }

        Text(
            text = weather.temperature,
            fontSize = 96.sp,
            fontWeight = FontWeight.Thin,
            color = content,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(text = weather.conditionText, fontSize = 18.sp, color = content)
        if (weather.highLow.isNotBlank()) {
            Text(
                text = weather.highLow,
                fontSize = 16.sp,
                color = contentMuted,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
