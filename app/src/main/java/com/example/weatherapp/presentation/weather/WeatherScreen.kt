package com.example.weatherapp.presentation.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weatherapp.R
import com.example.weatherapp.domain.model.WeatherConditionType
import com.example.weatherapp.presentation.theme.WeatherConditionTheme
import com.example.weatherapp.presentation.theme.WeatherPalette
import com.example.weatherapp.presentation.theme.cardColor
import com.example.weatherapp.presentation.weather.components.CurrentWeatherCard
import com.example.weatherapp.presentation.weather.components.DailyForecastList
import com.example.weatherapp.presentation.weather.components.HourlyRow
import com.example.weatherapp.presentation.weather.components.WeatherBackground
import com.example.weatherapp.presentation.weather.components.WeatherSection
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun WeatherRoot(
    onNavigateToCities: () -> Unit,
    viewModel: WeatherViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    com.example.weatherapp.core.presentation.ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            WeatherEvent.NavigateToCities -> onNavigateToCities()
            is WeatherEvent.ShowMessage -> scope.launch {
                snackbarHostState.showSnackbar(event.message.asString(context))
            }
        }
    }

    WeatherScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction
    )
}

@Composable
fun WeatherScreen(
    state: WeatherState,
    snackbarHostState: SnackbarHostState,
    onAction: (WeatherAction) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { state.pages.size })
    val activePage = state.pages.getOrNull(pagerState.currentPage)
    val palette = activePage
        ?.let { WeatherPalette.forCondition(it.conditionType, it.isDay) }
        ?: WeatherPalette.Neutral

    // Keep the ViewModel's notion of the current page in sync (used to target refresh).
    LaunchedEffect(pagerState.currentPage) {
        onAction(WeatherAction.OnPageChanged(pagerState.currentPage))
    }

    WeatherConditionTheme(palette) {
        Box(modifier = Modifier.fillMaxSize()) {
            WeatherBackground(palette)

            Scaffold(
                containerColor = Color.Transparent,
                contentColor = palette.content,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    WeatherTopBar(
                        pageCount = state.pages.size,
                        currentPage = pagerState.currentPage,
                        content = palette.content,
                        onManageCities = { onAction(WeatherAction.OnManageCitiesClick) }
                    )
                }
            ) { innerPadding ->
                when {
                    state.isInitialLoading && state.pages.isEmpty() ->
                        LoadingState(palette.accent, Modifier.padding(innerPadding))

                    state.hasNoCities ->
                        EmptyState(
                            content = palette.content,
                            onAddCity = { onAction(WeatherAction.OnManageCitiesClick) },
                            modifier = Modifier.padding(innerPadding)
                        )

                    else -> PullToRefreshBox(
                        isRefreshing = state.isRefreshing,
                        onRefresh = { onAction(WeatherAction.OnRefresh) },
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        HorizontalPager(state = pagerState) { pageIndex ->
                            val page = state.pages[pageIndex]
                            WeatherPage(
                                weather = page,
                                palette = WeatherPalette.forCondition(page.conditionType, page.isDay),
                                onAction = onAction
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherTopBar(
    pageCount: Int,
    currentPage: Int,
    content: Color,
    onManageCities: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pageCount) { index ->
                val active = index == currentPage
                Box(
                    modifier = Modifier
                        .padding(3.dp)
                        .size(if (active) 9.dp else 7.dp)
                        .clip(CircleShape)
                        .background(content.copy(alpha = if (active) 1f else 0.4f))
                )
            }
        }
        TextButton(
            onClick = onManageCities,
            colors = ButtonDefaults.textButtonColors(contentColor = content)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(text = stringResource(R.string.action_add_city), fontSize = 14.sp)
        }
    }
}

@Composable
private fun WeatherPage(
    weather: CityWeatherUi,
    palette: WeatherPalette,
    onAction: (WeatherAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        CurrentWeatherCard(
            weather = weather,
            content = palette.content,
            contentMuted = palette.contentMuted
        )

        Spacer(Modifier.height(24.dp))
        if (weather.hourly.isNotEmpty()) {
            WeatherSection(
                title = stringResource(R.string.section_hourly),
                surface = palette.cardColor,
                content = palette.content
            ) {
                HourlyRow(
                    hours = weather.hourly,
                    content = palette.content,
                    contentMuted = palette.contentMuted
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        if (weather.daily.isNotEmpty()) {
            WeatherSection(
                title = stringResource(R.string.section_daily),
                surface = palette.cardColor,
                content = palette.content
            ) {
                DailyForecastList(
                    days = weather.daily,
                    content = palette.content,
                    contentMuted = palette.contentMuted
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(
            text = weather.lastUpdated,
            color = palette.contentMuted,
            fontSize = 12.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun LoadingState(accent: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = accent)
    }
}

@Composable
private fun EmptyState(
    content: Color,
    onAddCity: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.empty_no_cities_title),
            color = content,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.empty_no_cities_body),
            color = content.copy(alpha = 0.75f),
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(onClick = onAddCity) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(text = stringResource(R.string.action_add_city))
        }
    }
}

// ---- Previews ----

private fun sampleWeather() = CityWeatherUi(
    cityId = "london",
    cityName = "London",
    region = "England",
    conditionType = WeatherConditionType.RAIN,
    isDay = true,
    temperature = "14°",
    conditionText = "Light rain",
    highLow = "H:16°   L:9°",
    lastUpdated = "Updated 15:20",
    hourly = List(12) { i ->
        HourlyUi(timeLabel = "%02d:00".format((14 + i) % 24), temperature = "${13 + i % 4}°")
    },
    daily = List(5) { i ->
        DailyUi(
            dayLabel = if (i == 0) "Today" else listOf("Mon", "Tue", "Wed", "Thu")[i - 1],
            high = "${16 + i}°",
            low = "${8 + i}°"
        )
    }
)

@Preview
@Composable
private fun WeatherScreenPreview() {
    WeatherScreen(
        state = WeatherState(isInitialLoading = false, pages = listOf(sampleWeather())),
        snackbarHostState = remember { SnackbarHostState() },
        onAction = {}
    )
}

@Preview
@Composable
private fun WeatherScreenEmptyPreview() {
    WeatherScreen(
        state = WeatherState(isInitialLoading = false, pages = emptyList()),
        snackbarHostState = remember { SnackbarHostState() },
        onAction = {}
    )
}
