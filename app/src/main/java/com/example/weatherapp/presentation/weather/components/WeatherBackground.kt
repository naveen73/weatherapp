package com.example.weatherapp.presentation.weather.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import com.example.weatherapp.presentation.theme.WeatherPalette

/**
 * Full-screen weather gradient. The gradient stops are animated with [animateColorAsState] and
 * read inside [drawBehind] (draw phase), so condition changes cross-fade smoothly without
 * recomposing the content drawn on top.
 */
@Composable
fun WeatherBackground(
    palette: WeatherPalette,
    modifier: Modifier = Modifier
) {
    val duration = 900
    val top = animateColorAsState(palette.gradient[0], tween(duration), label = "gradientTop")
    val mid = animateColorAsState(
        palette.gradient.getOrElse(1) { palette.gradient[0] }, tween(duration), label = "gradientMid"
    )
    val bottom = animateColorAsState(
        palette.gradient.getOrElse(2) { palette.gradient.last() }, tween(duration), label = "gradientBottom"
    )

    Box(
        modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    Brush.verticalGradient(
                        listOf(top.value, mid.value, bottom.value)
                    )
                )
            }
    )
}
