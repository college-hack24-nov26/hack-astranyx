package com.sktech.wastetrack.ui.screens.weather

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sktech.wastetrack.domain.agro.CropSuitabilityItem
import com.sktech.wastetrack.domain.agro.DynamicCropAdvisoryEngine
import com.sktech.wastetrack.domain.model.Farm
import com.sktech.wastetrack.domain.model.SoilProfile
import com.sktech.wastetrack.domain.model.SoilType
import com.sktech.wastetrack.domain.model.WaterSource
import com.sktech.wastetrack.ui.theme.AgriDeepForest
import com.sktech.wastetrack.ui.theme.GoldenWheatLight
import com.sktech.wastetrack.ui.theme.HarvestGreen
import com.sktech.wastetrack.ui.theme.HarvestGreenDark
import com.sktech.wastetrack.ui.theme.WarningOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropSuitabilityScreen(
    farmId: String = "default_farm",
    onNavigateBack: () -> Unit,
    onSelectCropForCycle: (String) -> Unit = {},
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    BackHandler {
        onNavigateBack()
    }

    val defaultFarm = remember(farmId) {
        Farm(
            id = farmId,
            name = "Sinnar Agro Parcel 01",
            surveyNumber = "SN-248/1A",
            plotAreaAcres = 4.5,
            soilType = SoilType.BLACK_COTTON,
            waterSource = WaterSource.DRIP_IRRIGATION,
            village = "Sinnar",
            taluka = "Sinnar",
            district = "Nashik",
            latitude = 19.8512,
            longitude = 73.9214
        )
    }

    val defaultSoil = remember {
        SoilProfile(
            ph = 7.4,
            nitrogen = 220.0,
            phosphorus = 18.0,
            potassium = 150.0,
            organicCarbon = 0.65
        )
    }

    val suitableCrops = remember(state.currentWeather) {
        DynamicCropAdvisoryEngine.evaluateCropSuitability(
            farm = defaultFarm,
            soilProfile = defaultSoil,
            weather = state.currentWeather
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Crop Suitability Matrix",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Agro-Climatic Suitability & Nutrient Retention Desk",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshWeather() },
                        enabled = !state.isRefreshing
                    ) {
                        if (state.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = HarvestGreen,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Refresh",
                                tint = HarvestGreen
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = HarvestGreen.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, HarvestGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Grass,
                                contentDescription = null,
                                tint = HarvestGreenDark
                            )
                            Text(
                                text = "Soil & Climate Agro-Met Baseline",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = HarvestGreenDark
                            )
                        }
                        Text(
                            text = "Sinnar Agro Hub (Lat: 19.85N, Long: 73.92E). Medium Black Vertisol (pH 7.4). Organic Carbon 0.65%. Localized temperature: %.1f C, humidity: %d%%."
                                .format(state.currentWeather.temperatureCelsius, state.currentWeather.humidityPercent),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(suitableCrops, key = { it.cropName }) { crop ->
                SuitabilityCard(
                    crop = crop,
                    onStartCycle = { onSelectCropForCycle(crop.cropName) }
                )
            }
        }
    }
}

@Composable
private fun SuitabilityCard(
    crop: CropSuitabilityItem,
    onStartCycle: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = crop.cropName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = crop.categoryName + " (" + crop.season + " Season)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (crop.suitabilityScore >= 90) HarvestGreen else GoldenWheatLight
                ) {
                    Text(
                        text = "${crop.suitabilityScore}% Match",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (crop.suitabilityScore >= 90) Color.White else HarvestGreenDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = crop.rationale,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Yield Potential", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(crop.expectedYield, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Residue Value", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(crop.residueValue, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = HarvestGreenDark)
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = HarvestGreen.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, HarvestGreen.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Science,
                            contentDescription = null,
                            tint = HarvestGreenDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Soil N-P-K Retention & Biochar Balance",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = HarvestGreenDark
                        )
                    }
                    Text(
                        text = "Nitrogen (N): ${crop.nitrogenRetentionKg} kg/Acre | Phosphorus (P): ${crop.phosphorusRetentionKg} kg/Acre | Potassium (K): ${crop.potassiumRetentionKg} kg/Acre",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Biochar carbon abatement: ${crop.biocharCarbonRetainedMt} MT CO2e avoided from open stubble burn.",
                        style = MaterialTheme.typography.labelSmall,
                        color = HarvestGreenDark
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = GoldenWheatLight.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, GoldenWheatLight.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.WbSunny,
                        contentDescription = null,
                        tint = AgriDeepForest,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Solar Drying Window: " + crop.dryingWindowHours + " (Target < 14% baling moisture)",
                        style = MaterialTheme.typography.labelSmall,
                        color = AgriDeepForest,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            crop.caveat?.let { cav ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = WarningOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = cav,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onStartCycle,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HarvestGreenDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Start Crop Cycle with this Variety", fontWeight = FontWeight.Bold)
            }
        }
    }
}
