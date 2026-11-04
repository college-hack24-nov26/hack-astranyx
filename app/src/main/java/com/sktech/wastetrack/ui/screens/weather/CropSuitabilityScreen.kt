package com.sktech.wastetrack.ui.screens.weather

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sktech.wastetrack.domain.weather.model.CropSuitabilityEvaluation
import com.sktech.wastetrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropSuitabilityScreen(
    onNavigateBack: () -> Unit,
    onNavigateToValorization: (String) -> Unit = {},
    onNavigateToAddCropCycle: () -> Unit = {},
    viewModel: CropSuitabilityViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(onBack = onNavigateBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Agro-Climatic Suitability Matrix",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Machine Learning Soil N-P-K & Phenology Desk",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = adaptiveHarvestContainer(),
                    border = BorderStroke(1.dp, HarvestGreen.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.Grass, contentDescription = null, tint = HarvestGreenDark, modifier = Modifier.size(20.dp))
                                Text(
                                    text = "Agro-Climatic Baseline Telemetry",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = HarvestGreenDark
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = HarvestGreen
                            ) {
                                Text(
                                    text = "LIVE SENSORS",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        val hubs = listOf(
                            "Sinnar Agro Hub",
                            "Niphad Bio-Energy Corridor",
                            "Igatpuri Agro-Climatic Belt",
                            "Dindori Horticultural Zone"
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(hubs) { hub ->
                                val isSelected = state.selectedHub == hub
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) HarvestGreen else MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, if (isSelected) HarvestGreen else MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.clickable { viewModel.selectHub(hub) }
                                ) {
                                    Text(
                                        text = hub,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = HarvestGreen.copy(alpha = 0.2f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Soil Classification", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                                Text(state.baseline.soilType, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Soil pH", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                                Text("%.1f".format(state.baseline.soilPh), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = HarvestGreenDark)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Organic Carbon", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                                Text("%.2f%%".format(state.baseline.organicCarbonPercent), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, HarvestGreen.copy(alpha = 0.25f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.WbSunny, contentDescription = null, tint = GoldenWheat, modifier = Modifier.size(18.dp))
                                Column {
                                    Text(
                                        text = "Dew Radar & Optimal Baling Window",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = HarvestGreenDark
                                    )
                                    Text(
                                        text = state.baseline.optimalBalingWindow,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isNpkMode = state.showNpkRetentionMode
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (!isNpkMode) HarvestGreen else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, if (!isNpkMode) HarvestGreen else MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { if (isNpkMode) viewModel.toggleNpkRetentionMode() }
                    ) {
                        Text(
                            text = "Commercial Valorization",
                            modifier = Modifier.padding(vertical = 10.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (!isNpkMode) Color.White else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isNpkMode) Teal else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, if (isNpkMode) Teal else MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { if (!isNpkMode) viewModel.toggleNpkRetentionMode() }
                    ) {
                        Text(
                            text = "Soil N-P-K Retention",
                            modifier = Modifier.padding(vertical = 10.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isNpkMode) Color.White else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item {
                val seasons = listOf("ALL" to "All Seasons", "Kharif" to "Kharif Crops", "Rabi" to "Rabi Crops", "Annual" to "Annual Cash Crops")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(seasons) { (key, label) ->
                        val isSelected = state.selectedSeason == key
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectSeason(key) },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HarvestGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            items(state.filteredEvaluations, key = { it.cropName }) { evaluation ->
                CropSuitabilityCard(
                    evaluation = evaluation,
                    showNpkMode = state.showNpkRetentionMode,
                    onNavigateToValorization = { onNavigateToValorization(evaluation.cropName) },
                    onNavigateToAddCropCycle = onNavigateToAddCropCycle
                )
            }
        }
    }
}

@Composable
private fun CropSuitabilityCard(
    evaluation: CropSuitabilityEvaluation,
    showNpkMode: Boolean,
    onNavigateToValorization: () -> Unit,
    onNavigateToAddCropCycle: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = evaluation.cropName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${evaluation.categoryName}  |  Season: ${evaluation.season}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = adaptiveHarvestContainer(),
                    border = BorderStroke(1.dp, HarvestGreen.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Filled.Verified, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(16.dp))
                        Text(
                            text = "${evaluation.suitabilityScore}% SUITABLE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = HarvestGreenDark,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            if (!showNpkMode) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Residue Volume", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                            Text("%.1f MT / Acre".format(evaluation.estimatedResidueMtPerAcre), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Est. Residue Revenue", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                            Text("₹%,.0f / Acre".format(evaluation.estimatedResidueRevenueInr), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = HarvestGreenDark)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Drying Sunlight", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                            Text(evaluation.dryingWindowHours, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, fontSize = 10.sp)
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = adaptiveTealContainer(),
                    border = BorderStroke(1.dp, Teal.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Soil Mineral Recovery via Mulching / Biochar Pyrolysis:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TealDark
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Nitrogen (N)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                                Text("%.1f kg / Acre".format(evaluation.soilNitrogenRetentionKg), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = HarvestGreenDark)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Phosphorus (P)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                                Text("%.1f kg / Acre".format(evaluation.soilPhosphorusRetentionKg), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = WarningOrange)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Potassium (K)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                                Text("%.1f kg / Acre".format(evaluation.soilPotassiumRetentionKg), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SkyBlueDark)
                            }
                        }
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Psychology, contentDescription = null, tint = HarvestGreenDark, modifier = Modifier.size(16.dp))
                    Text(
                        text = evaluation.rationale,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onNavigateToValorization,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Valorization Desk", fontSize = 11.sp)
                }

                Button(
                    onClick = onNavigateToAddCropCycle,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Plant Crop Cycle", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

