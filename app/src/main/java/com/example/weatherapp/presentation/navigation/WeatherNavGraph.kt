package com.example.weatherapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.weatherapp.presentation.cities.CitiesRoot
import com.example.weatherapp.presentation.weather.WeatherRoot

/** App navigation host: the weather home screen and the manage-cities screen. */
@Composable
fun WeatherNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = WeatherRoute) {
        composable<WeatherRoute> {
            WeatherRoot(
                onNavigateToCities = { navController.navigate(CitiesRoute) }
            )
        }
        composable<CitiesRoute> {
            CitiesRoot(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
