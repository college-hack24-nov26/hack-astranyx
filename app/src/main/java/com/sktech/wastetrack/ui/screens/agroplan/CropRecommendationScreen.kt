package com.sktech.wastetrack.ui.screens.agroplan

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sktech.wastetrack.domain.agro.model.CropSuitabilitySummary
import com.sktech.wastetrack.ui.screens.agroplan.components.CropComparisonBottomSheet
import com.sktech.wastetrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropRecommendationScreen(
    farmId: String,
    onNavigateBack: () -> Unit,
    onNavigateToCropAssessment: (String) -> Unit,
    onNavigateToDirectSearch: () -> Unit = {},
    viewModel: CropRecommendationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    BackHandler(onBack = onNavigateBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Next-Season Crop Recommendations",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${state.farmName} | Agro-Climatic Shortlist",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }, enabled = !state.isRefreshing) {
                        if (state.isRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = HarvestGreen, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = HarvestGreen)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            AnimatedVisibility(visible = state.selectedCropsForComparison.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.openComparisonSheet() },
                    icon = { Icon(Icons.Filled.CompareArrows, contentDescription = null) },
                    text = { Text("Compare (${state.selectedCropsForComparison.size})") },
                    containerColor = HarvestGreen,
                    contentColor = Color.White
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = HarvestGreen.copy(alpha = 0.10f)),
                        border = BorderStroke(1.dp, HarvestGreen.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(HarvestGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.AutoGraph, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Rotation & Nutrient Recovery Radar",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Evaluates preceding crop depletion, soil N-P-K carryover, and APMC market momentum.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DarkTextSecondary
                                )
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ranked Crop Candidates",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = state.dataFreshnessNotice,
                            style = MaterialTheme.typography.labelSmall,
                            color = HarvestGreenDark,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                itemsIndexed(state.recommendations) { index, crop ->
                    val isSelected = state.selectedCropsForComparison.contains(crop.cropName)
                    RecommendationCard(
                        rank = index + 1,
                        crop = crop,
                        isSelected = isSelected,
                        onToggleCompare = { viewModel.toggleCropComparison(crop.cropName) },
                        onAssess = { onNavigateToCropAssessment(crop.cropName) }
                    )
                }
            }

            if (state.isComparisonSheetOpen) {
                CropComparisonBottomSheet(
                    comparisonList = state.comparisonList,
                    onDismiss = { viewModel.closeComparisonSheet() },
                    onSelectCropForPlan = { cropName ->
                        viewModel.closeComparisonSheet()
                        onNavigateToCropAssessment(cropName)
                    }
                )
            }
        }
    }
}

@Composable
private fun RecommendationCard(
    rank: Int,
    crop: CropSuitabilitySummary,
    isSelected: Boolean,
    onToggleCompare: () -> Unit,
    onAssess: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (isSelected) HarvestGreen else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = when (rank) {
                            1 -> GoldenWheatLight.copy(alpha = 0.25f)
                            2 -> HarvestGreen.copy(alpha = 0.20f)
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "#$rank",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = when (rank) {
                                    1 -> GoldenWheatDark
                                    2 -> HarvestGreenDark
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = crop.categoryName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HarvestGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${crop.compositeScore}% Match",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = HarvestGreenDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = crop.cropName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = crop.primaryRecommendationRationale,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FactorChip(label = "Weather", score = crop.weatherScore, max = 20)
                FactorChip(label = "Climate", score = crop.climateScore, max = 25)
                FactorChip(label = "Soil", score = crop.soilScore, max = 25)
                FactorChip(label = "Rotation", score = crop.rotationScore, max = 15)
                FactorChip(label = "Mandi", score = crop.marketScore, max = 15)
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onToggleCompare,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isSelected) HarvestGreen else MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(1.dp, if (isSelected) HarvestGreen else MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Filled.Check else Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isSelected) "Selected" else "Compare", fontSize = 12.sp)
                }

                Button(
                    onClick = onAssess,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                ) {
                    Text(text = "Assess Economics", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun RowScope.FactorChip(label: String, score: Int, max: Int) {
    Surface(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "$score/$max", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HarvestGreenDark)
        }
    }
}
