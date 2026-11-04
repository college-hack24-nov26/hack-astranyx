package com.sktech.wastetrack.ui.screens.weather

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sktech.wastetrack.domain.model.DailyForecastItem
import com.sktech.wastetrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherDashboardScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToCropSuitability: () -> Unit = {},
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val clusters = listOf("Nashik Rural Hub", "Sinnar Agro Hub", "Niphad Bio-Energy", "Dindori Cluster")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Agri Weather & Drying Radar", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(state.currentWeather.locationName, style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToCropSuitability) {
                        Icon(imageVector = Icons.Filled.Agriculture, contentDescription = "Agro Suitability", tint = HarvestGreen)
                    }
                    IconButton(onClick = { viewModel.refreshWeather() }) {
                        Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Refresh", tint = HarvestGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(clusters) { cluster ->
                        val isSelected = state.selectedCluster == cluster
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateCluster(cluster) },
                            label = { Text(cluster, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HarvestGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = HarvestGreenContainer)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${state.currentWeather.temperatureCelsius.toInt()}°C",
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HarvestGreenDark
                                )
                                Text(
                                    text = state.currentWeather.weatherDescription,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = HarvestGreenDark
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = HarvestGreen,
                                modifier = Modifier.size(60.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(imageVector = Icons.Filled.WbSunny, contentDescription = null, tint = Color.White, modifier = Modifier.size(34.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = HarvestGreen.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            WeatherKpiCol("Humidity", "${state.currentWeather.humidityPercent}%")
                            WeatherKpiCol("Wind", "${state.currentWeather.windSpeedKmh} km/h")
                            WeatherKpiCol("UV Index", "${state.currentWeather.uvIndex}")
                            WeatherKpiCol("Precipitation", "${state.currentWeather.precipitationMm} mm")
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldenWheatContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Filled.LocalFireDepartment, contentDescription = null, tint = Color(0xFFC84B31))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Residue Burning Hazard & Advisory", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = GoldenWheatDark)
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFFEBEE)
                            ) {
                                Text(
                                    text = "Alert: High Risk",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC62828)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Dry conditions and wind velocity increase wildfire spread. Open field burning is strictly prohibited under environmental norms. Sell dry stalks through AgriStock for bio-energy pellets.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF5D4037)
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Drying Index: ${state.advisory.dryingIndex}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GoldenWheatDark)
                            Text("~${state.advisory.moistureLossRatePerDayPercent}% Moisture Loss/Day", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = GoldenWheatDark)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Field Operations Suitability",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SuitabilityRow("Crop Residue Baling", state.advisory.residueBalingFeasibility, Icons.Filled.Recycling, HarvestGreen)
                        SuitabilityRow("Foliar / Pesticide Spray", state.advisory.pesticideSprayWindow, Icons.Filled.BugReport, HarvestGreen)
                        SuitabilityRow("Harvest & Transit", state.advisory.harvestReadinessWindow, Icons.Filled.Agriculture, HarvestGreen)
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToCropSuitability() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = HarvestGreen.copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, HarvestGreen.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(HarvestGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Spa,
                                contentDescription = "Suitability Matrix",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Agro-Climatic Suitability & N-P-K Desk",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "ML crop suitability radar, drying window & soil mineral retention",
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkTextSecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Navigate",
                            tint = HarvestGreen
                        )
                    }
                }
            }

            item {
                Text(
                    text = "5-Day Agricultural Forecast",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(state.forecastDays) { day ->
                ForecastRowCard(day = day)
            }
        }
    }
}

@Composable
private fun WeatherKpiCol(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = HarvestGreenDark)
    }
}

@Composable
private fun SuitabilityRow(title: String, verdict: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = HarvestGreenContainer,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = title, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
        Text(text = verdict, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = HarvestGreen)
    }
}

@Composable
private fun ForecastRowCard(day: DailyForecastItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "${day.dayLabel} (${day.dateText})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = day.conditionDescription, style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.WaterDrop, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text(text = "${day.rainProbabilityPercent}%", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "${day.tempMaxCelsius}° / ${day.tempMinCelsius}°", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "Drying: ${day.dryingSuitability}", style = MaterialTheme.typography.labelSmall, color = HarvestGreen)
            }
        }
    }
}
