package com.example.weatherapp.presentation.weather.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.presentation.weather.DailyUi
import com.example.weatherapp.presentation.weather.HourlyUi

/** A titled, translucent card container used for the hourly and daily blocks. */
@Composable
fun WeatherSection(
    title: String,
    surface: Color,
    content: Color,
    modifier: Modifier = Modifier,
    body: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = content.copy(alpha = 0.85f),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(surface)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            body()
        }
    }
}

/** A simple horizontally-scrollable strip of upcoming hours: time + temperature only. */
@Composable
fun HourlyRow(
    hours: List<HourlyUi>,
    content: Color,
    contentMuted: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        hours.forEach { hour ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(end = 22.dp)
            ) {
                Text(hour.timeLabel, color = contentMuted, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                Text(hour.temperature, color = content, fontSize = 17.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

/** The multi-day list: day label + high / low temperatures only. */
@Composable
fun DailyForecastList(
    days: List<DailyUi>,
    content: Color,
    contentMuted: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        days.forEach { day ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = day.dayLabel,
                    color = content,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = day.low,
                    color = contentMuted,
                    fontSize = 16.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(48.dp)
                )
                Text(
                    text = day.high,
                    color = content,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(48.dp)
                )
            }
        }
    }
}
