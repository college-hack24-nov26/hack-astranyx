package com.sktech.wastetrack.ui.screens.agroplan

import com.sktech.wastetrack.ui.screens.agroplan.components.PlanRevisionHistorySheet
import com.sktech.wastetrack.ui.screens.agroplan.components.WeatherAdaptationBanner

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Agriculture
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.Recycling
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sktech.wastetrack.domain.agro.model.CustomAgroPlan
import com.sktech.wastetrack.domain.agro.model.GrowthStageScheduleStep
import com.sktech.wastetrack.domain.agro.model.ScheduledCareAction
import com.sktech.wastetrack.ui.theme.GoldenWheat
import com.sktech.wastetrack.ui.theme.HarvestGreen
import com.sktech.wastetrack.ui.theme.HarvestGreenContainer
import com.sktech.wastetrack.ui.theme.HarvestGreenDark
import com.sktech.wastetrack.ui.theme.WarningOrange
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomAgroPlanScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToListingCreation: (String) -> Unit = {},
    onNavigateToRecommendations: () -> Unit = {},
    viewModel: CustomAgroPlanViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val plan = state.plan
    val snackbarHostState = remember { SnackbarHostState() }
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }
    BackHandler(onBack = onNavigateBack)

    var revisionNotesText by remember { mutableStateOf("") }

    val cropOptions = listOf("Wheat", "Cotton", "Sugarcane", "Soybean")

    LaunchedEffect(state.feedbackMessage) {
        state.feedbackMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Individualized AgroPlan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${plan?.farmName ?: "Sinnar Agro Hub"} • ${plan?.cropName ?: "Wheat"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    Surface(
                        modifier = Modifier
                            .clickable { viewModel.showRevisionSheet(true) }
                            .padding(end = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = HarvestGreenContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.History,
                                contentDescription = null,
                                tint = HarvestGreenDark,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "v${plan?.planVersion ?: 1}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = HarvestGreenDark
                            )
                        }
                    }

                    IconButton(onClick = onNavigateToRecommendations) {
                        Icon(
                            imageVector = Icons.Filled.Eco,
                            contentDescription = "Crop Recommendations",
                            tint = HarvestGreen
                        )
                    }
                    IconButton(onClick = { viewModel.toggleReviseDialog(true) }) {
                        Icon(
                            Icons.Filled.EditNote,
                            contentDescription = "Customize Plan",
                            tint = HarvestGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (plan == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading personalized AgroPlan...")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(cropOptions, key = { it }) { crop ->
                            val isSelected = state.cropName.equals(crop, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectCrop(crop) },
                                label = { Text(crop, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = HarvestGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                if (!state.isWeatherNoticeDismissed && plan.activeAdaptationAlerts.isNotEmpty()) {
                    items(plan.activeAdaptationAlerts, key = { it.noticeId }) { alert ->
                        WeatherAdaptationBanner(
                            notice = alert,
                            onReviewClick = { viewModel.showRevisionSheet(true) },
                            onDismiss = { viewModel.dismissWeatherNotice() }
                        )
                    }
                }

                item {
                    HeroSuitabilityCard(
                        plan = plan,
                        onRevise = { viewModel.toggleReviseDialog(true) }
                    )
                }

                item {
                    WeatherIrrigationRadarCard(
                        state = state,
                        irrigationGuidance = plan.irrigationGuidance
                    )
                }

                item {
                    PrecisionFertilizerCard(plan = plan)
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Growth Stage Care Schedule",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = HarvestGreenContainer
                        ) {
                            Text(
                                text = "${state.completedActionIds.size} Done",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = HarvestGreenDark,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                items(plan.careSchedule, key = { it.stageOrder }) { stage ->
                    GrowthStageCard(
                        stage = stage,
                        completedIds = state.completedActionIds,
                        onToggleAction = { viewModel.toggleAction(it) }
                    )
                }

                item {
                    EconomicsForecastCard(
                        plan = plan,
                        currencyFormat = currencyFormat
                    )
                }

                item {
                    ResidueValorizationCard(
                        plan = plan,
                        currencyFormat = currencyFormat,
                        onCreateListing = { onNavigateToListingCreation(plan.cropName) }
                    )
                }

                item {
                    DisclaimerCard(disclaimerText = plan.statutoryDisclaimer)
                }
            }
        }
    }

    if (state.isReviseDialogOpen) {
        Dialog(onDismissRequest = { viewModel.toggleReviseDialog(false) }) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Customize AgroPlan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { viewModel.toggleReviseDialog(false) }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        }
                    }

                    Text(
                        text = "Update dosages, irrigation intervals, or seed variety based on on-field soil tests or farm machinery availability.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = revisionNotesText,
                        onValueChange = { revisionNotesText = it },
                        label = { Text("Revision Notes & Customization") },
                        placeholder = { Text("E.g. Adjusted basal DAP dosage from 50kg to 45kg based on latest NPK soil card.") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )

                    Button(
                        onClick = {
                            viewModel.submitRevision(revisionNotesText)
                            revisionNotesText = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                    ) {
                        Text("Save Custom Version", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (state.isRevisionSheetOpen) {
        PlanRevisionHistorySheet(
            revisions = state.revisions,
            currentVersion = state.plan?.planVersion ?: 1,
            onDismiss = { viewModel.showRevisionSheet(false) }
        )
    }

}



@Composable
private fun HeroSuitabilityCard(
    plan: CustomAgroPlan,
    onRevise: () -> Unit
) {
    val summary = plan.suitabilitySummary
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, HarvestGreen.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plan.cropName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = HarvestGreenDark
                    )
                    Text(
                        text = "${plan.variety ?: "Selected Variety"} • ${plan.targetSeason}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = HarvestGreenContainer
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${summary.compositeScore}/100",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = HarvestGreenDark
                        )
                        Text(
                            text = "SUITABILITY",
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = HarvestGreen
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Text(
                text = summary.primaryRecommendationRationale,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RadarScorePill("Weather", "${summary.weatherScore}/20", Modifier.weight(1f))
                RadarScorePill("Climate", "${summary.climateScore}/25", Modifier.weight(1f))
                RadarScorePill("Soil NPK", "${summary.soilScore}/25", Modifier.weight(1f))
                RadarScorePill("Rotation", "${summary.rotationScore}/15", Modifier.weight(1f))
                RadarScorePill("Market", "${summary.marketScore}/15", Modifier.weight(1f))
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Expected Yield", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(summary.expectedYieldRange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Water Budget", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(summary.waterNeed.take(24), fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun RadarScorePill(
    label: String,
    score: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = HarvestGreen.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(score, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = HarvestGreenDark)
        }
    }
}

@Composable
private fun WeatherIrrigationRadarCard(
    state: CustomAgroPlanUiState,
    irrigationGuidance: com.sktech.wastetrack.domain.agro.model.IrrigationPlanGuidance
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (!state.isWaterReleaseSafe) WarningOrange.copy(alpha = 0.5f) else HarvestGreen.copy(alpha = 0.4f))
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (!state.isWaterReleaseSafe) WarningOrange.copy(alpha = 0.15f) else HarvestGreen.copy(alpha = 0.15f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                if (!state.isWaterReleaseSafe) Icons.Filled.WaterDrop else Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = if (!state.isWaterReleaseSafe) WarningOrange else HarvestGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Smart Irrigation Radar",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Rain Probability: ${state.todayRainProbability}% • ${state.todayPrecipitationMm} mm",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (!state.isWaterReleaseSafe) WarningOrange else HarvestGreen
                ) {
                    Text(
                        text = if (!state.isWaterReleaseSafe) "HOLD IRRIGATION" else "SAFE TO IRRIGATE",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (!state.isWaterReleaseSafe) WarningOrange.copy(alpha = 0.1f) else HarvestGreen.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = irrigationGuidance.rainAdjustmentNote ?: "Scheduled: ${irrigationGuidance.frequencyHours} via ${irrigationGuidance.baseMethod}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (!state.isWaterReleaseSafe) Color(0xFF8A3B00) else HarvestGreenDark,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

@Composable
private fun PrecisionFertilizerCard(plan: CustomAgroPlan) {
    val guidance = plan.fertilizerGuidance
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.Science, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(20.dp))
                Text("Precision N-P-K & Micronutrient Prescription", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Soil Test Baseline: ${guidance.soilSummaryText}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FertilizerRow("Basal Dose", guidance.basalApplicationDose)
                FertilizerRow("Vegetative Top Dress", guidance.vegetativeTopDressDose)
                FertilizerRow("Flowering Fertigation", guidance.floweringFertigationDose)
                guidance.micronutrientPrescription?.let { FertilizerRow("Micronutrient Spray", it) }
                guidance.previousCropCarryOverCredit?.let { FertilizerRow("Crop Carryover Credit", it) }
            }
        }
    }
}

@Composable
private fun FertilizerRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.6f))
    }
}

@Composable
private fun GrowthStageCard(
    stage: GrowthStageScheduleStep,
    completedIds: Set<String>,
    onToggleAction: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${stage.stageOrder}. ${stage.stageName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = HarvestGreenDark
                )
                Text(
                    text = stage.windowDescription,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = stage.keyObjectives,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            stage.actions.forEach { action ->
                val isDone = completedIds.contains(action.actionId)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleAction(action.actionId) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDone) HarvestGreenContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isDone,
                            onCheckedChange = { onToggleAction(action.actionId) },
                            colors = CheckboxDefaults.colors(checkedColor = HarvestGreen)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = action.title,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp,
                                textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                            )
                            Text(
                                text = action.instruction,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EconomicsForecastCard(
    plan: CustomAgroPlan,
    currencyFormat: NumberFormat
) {
    val eco = plan.economicsSnapshot ?: return
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, GoldenWheat.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Outlined.MonetizationOn, contentDescription = null, tint = GoldenWheat, modifier = Modifier.size(20.dp))
                Text("Indicative Farm Economics & Net Profit", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Gross Crop Revenue", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(currencyFormat.format(eco.grossCropRevenue), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Column {
                    Text("Residue Buyback", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(currencyFormat.format(eco.grossResidueRevenue), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = HarvestGreen)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Production Cost", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(currencyFormat.format(eco.totalProductionCost), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Projected Net Profit / Acre", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                Text(
                    currencyFormat.format(eco.expectedNetProfit),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = HarvestGreen
                )
            }
        }
    }
}

@Composable
private fun ResidueValorizationCard(
    plan: CustomAgroPlan,
    currencyFormat: NumberFormat,
    onCreateListing: () -> Unit
) {
    val res = plan.residuePlan
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, HarvestGreen.copy(alpha = 0.35f))
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
                    Icon(Icons.Filled.Eco, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(20.dp))
                    Text("Biomass Stubble Valorization", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = HarvestGreenContainer
                ) {
                    Text(
                        text = "ZERO BURNING",
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = HarvestGreenDark,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "${res.residueName}: Estimated ${res.estimatedBiomassTonsPerAcre} MT / Acre. Suitable for ${res.suggestedIndustrialApplication}.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onCreateListing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
            ) {
                Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Publish Stubble on AgriStock", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DisclaimerCard(disclaimerText: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Text(
                text = disclaimerText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )
        }
    }
}

