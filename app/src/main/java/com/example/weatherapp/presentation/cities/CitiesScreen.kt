package com.example.weatherapp.presentation.cities

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weatherapp.R
import com.example.weatherapp.core.presentation.ObserveAsEvents
import com.example.weatherapp.domain.model.CitySearchResult
import com.example.weatherapp.presentation.theme.WeatherAppTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun CitiesRoot(
    onNavigateBack: () -> Unit,
    viewModel: CitiesViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            CitiesEvent.NavigateBack -> onNavigateBack()
            is CitiesEvent.ShowMessage -> scope.launch {
                snackbarHostState.showSnackbar(event.message.asString(context))
            }
        }
    }

    CitiesScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitiesScreen(
    state: CitiesState,
    snackbarHostState: SnackbarHostState,
    onAction: (CitiesAction) -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.cities_title)) },
                navigationIcon = {
                    IconButton(onClick = { onAction(CitiesAction.OnBack) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { onAction(CitiesAction.OnQueryChange(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.isSearching) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    }
                },
                placeholder = { Text(stringResource(R.string.cities_search_hint)) }
            )

            if (state.results.isNotEmpty()) {
                SearchResults(
                    results = state.results,
                    onResultClick = { onAction(CitiesAction.OnResultClick(it)) }
                )
                HorizontalDivider()
            }

            Text(
                text = stringResource(R.string.cities_saved_header),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            if (state.savedCities.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.cities_saved_empty),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items = state.savedCities, key = { it.id }) { city ->
                        SavedCityRow(
                            city = city,
                            onRemove = { onAction(CitiesAction.OnRemoveCity(city.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResults(
    results: List<CitySearchResult>,
    onResultClick: (CitySearchResult) -> Unit
) {
    Column {
        results.forEach { result ->
            ListItem(
                headlineContent = { Text(result.name) },
                supportingContent = { Text(result.label) },
                leadingContent = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                trailingContent = {
                    IconButton(onClick = { onResultClick(result) }) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = stringResource(R.string.cd_add_city)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SavedCityRow(
    city: SavedCityUi,
    onRemove: () -> Unit
) {
    ListItem(
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (city.isCurrentLocation) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = stringResource(R.string.cd_current_location),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(city.name)
            }
        },
        supportingContent = { if (city.subtitle.isNotBlank()) Text(city.subtitle) },
        trailingContent = {
            // The auto-detected current-location city cannot be removed.
            if (!city.isCurrentLocation) {
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = stringResource(R.string.cd_remove_city)
                    )
                }
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview
@Composable
private fun CitiesScreenPreview() {
    WeatherAppTheme {
        CitiesScreen(
            state = CitiesState(
                query = "Lon",
                results = listOf(
                    CitySearchResult("london-uk", "London", "England", "United Kingdom", 51.5, -0.1)
                ),
                savedCities = listOf(
                    SavedCityUi("auto", "My Location", "London, United Kingdom", isCurrentLocation = true),
                    SavedCityUi("tokyo", "Tokyo", "Japan", isCurrentLocation = false)
                )
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {}
        )
    }
}
