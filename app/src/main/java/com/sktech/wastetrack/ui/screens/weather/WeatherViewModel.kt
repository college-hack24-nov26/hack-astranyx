package com.sktech.wastetrack.ui.screens.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sktech.wastetrack.domain.model.BiomassDryingAdvisory
import com.sktech.wastetrack.domain.model.DailyForecastItem
import com.sktech.wastetrack.domain.model.WeatherData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WeatherDashboardUiState(
    val currentWeather: WeatherData = WeatherData(),
    val advisory: BiomassDryingAdvisory = BiomassDryingAdvisory(),
    val forecastDays: List<DailyForecastItem> = listOf(
        DailyForecastItem("Today", "03 Nov", 29, 18, 0, "Sunny & Warm", "Rapid"),
        DailyForecastItem("Wed", "04 Nov", 30, 19, 5, "Clear Skies", "Optimal"),
        DailyForecastItem("Thu", "05 Nov", 28, 18, 15, "Partly Cloudy", "Moderate"),
        DailyForecastItem("Fri", "06 Nov", 27, 17, 10, "Breezy & Mild", "Moderate"),
        DailyForecastItem("Sat", "07 Nov", 29, 19, 0, "Bright Sun", "Rapid")
    ),
    val selectedCluster: String = "Nashik Rural Hub",
    val isRefreshing: Boolean = false
)

@HiltViewModel
class WeatherViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherDashboardUiState())
    val uiState: StateFlow<WeatherDashboardUiState> = _uiState.asStateFlow()

    fun updateCluster(cluster: String) {
        val (temp, hum, desc) = when (cluster) {
            "Sinnar Agro Hub" -> Triple(29.2, 44, "Dry Breeze")
            "Niphad Bio-Energy" -> Triple(28.0, 52, "Partly Cloudy")
            "Dindori Cluster" -> Triple(27.5, 50, "Mild Sun")
            else -> Triple(28.5, 48, "Sunny & Warm")
        }

        _uiState.update {
            it.copy(
                selectedCluster = cluster,
                currentWeather = it.currentWeather.copy(
                    locationName = "$cluster, Maharashtra",
                    temperatureCelsius = temp,
                    humidityPercent = hum,
                    weatherDescription = desc
                )
            )
        }
    }

    fun refreshWeather() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            kotlinx.coroutines.delay(400)
            _uiState.update {
                it.copy(
                    isRefreshing = false,
                    currentWeather = it.currentWeather.copy(observedAtMillis = System.currentTimeMillis())
                )
            }
        }
    }
}
