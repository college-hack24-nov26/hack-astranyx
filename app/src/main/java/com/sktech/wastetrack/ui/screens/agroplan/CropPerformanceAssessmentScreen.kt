package com.sktech.wastetrack.ui.screens.agroplan

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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sktech.wastetrack.domain.agro.model.CropSuitabilitySummary
import com.sktech.wastetrack.domain.agro.model.IndicativeEconomicsSummary
import com.sktech.wastetrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropPerformanceAssessmentScreen(
    farmId: String,
    cropName: String,
    onNavigateBack: () -> Unit,
    onNavigateToAgroPlan: (String) -> Unit,
    onNavigateToMarketTrends: () -> Unit,
    viewModel: CropPerformanceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    BackHandler(onBack = onNavigateBack)

    var showCostDialog by remember { mutableStateOf(false) }
    var inputSeedCost by remember { mutableStateOf("") }
    var inputFertCost by remember { mutableStateOf("") }
    var inputLaborCost by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Crop Performance & Economics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${state.farmName} | ${state.selectedCrop}",
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
                    IconButton(onClick = onNavigateToMarketTrends) {
                        Icon(Icons.Default.TrendingUp, contentDescription = "Market Trends", tint = HarvestGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onNavigateToMarketTrends,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, HarvestGreen)
                    ) {
                        Icon(Icons.Default.ShowChart, contentDescription = null, modifier = Modifier.size(16.dp), tint = HarvestGreen)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Price Trends", color = HarvestGreen)
                    }

                    Button(
                        onClick = { onNavigateToAgroPlan(state.selectedCrop) },
                        colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Text("Generate Plan")
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
        ) {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(state.availableCrops) { cropItem ->
                        val isSelected = state.selectedCrop.contains(cropItem.substringBefore(" "), ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectCrop(cropItem) },
                            label = { Text(cropItem.substringBefore(" (").take(18)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HarvestGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            state.suitabilitySummary?.let { summary ->
                item {
                    SuitabilityOverviewCard(summary = summary)
                }
            }

            state.economicsSummary?.let { econ ->
                item {
                    SideBySideRevenueCard(econ = econ)
                }

                item {
                    ProductionCostsCard(
                        econ = econ,
                        onEditCosts = {
                            inputSeedCost = econ.seedCost.toInt().toString()
                            inputFertCost = econ.fertilizerNutrientCost.toInt().toString()
                            inputLaborCost = econ.laborHarvestCost.toInt().toString()
                            showCostDialog = true
                        }
                    )
                }

                item {
                    ProfitSensitivityCard(econ = econ)
                }
            }

            state.suitabilitySummary?.let { summary ->
                item {
                    AgronomicStrengthsCard(summary = summary)
                }
            }
        }

        if (showCostDialog) {
            AlertDialog(
                onDismissRequest = { showCostDialog = false },
                title = { Text("Customize Input Costs (INR)") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = inputSeedCost,
                            onValueChange = { inputSeedCost = it },
                            label = { Text("Seed / Nursery Cost") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = inputFertCost,
                            onValueChange = { inputFertCost = it },
                            label = { Text("Fertilizer & Nutrition") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = inputLaborCost,
                            onValueChange = { inputLaborCost = it },
                            label = { Text("Labor & Harvesting") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.applyCustomCosts(
                                seedCost = inputSeedCost.toDoubleOrNull(),
                                fertilizerCost = inputFertCost.toDoubleOrNull(),
                                laborCost = inputLaborCost.toDoubleOrNull()
                            )
                            showCostDialog = false
                        }
                    ) {
                        Text("Apply & Recalculate", color = HarvestGreen)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCostDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun SuitabilityOverviewCard(summary: CropSuitabilitySummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Multi-Factor Suitability", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(text = "Algorithmic composite rating", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = HarvestGreen.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${summary.compositeScore}/100",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = HarvestGreenDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            MetricProgressRow(label = "Weather Compatibility", value = "${summary.weatherScore}/20", progress = summary.weatherScore / 20f)
            MetricProgressRow(label = "Seasonal Climate Match", value = "${summary.climateScore}/25", progress = summary.climateScore / 25f)
            MetricProgressRow(label = "Soil Type & pH Fit", value = "${summary.soilScore}/25", progress = summary.soilScore / 25f)
            MetricProgressRow(label = "Crop Rotation Benefit", value = "${summary.rotationScore}/15", progress = summary.rotationScore / 15f)
            MetricProgressRow(label = "Mandi Market Momentum", value = "${summary.marketScore}/15", progress = summary.marketScore / 15f)
        }
    }
}

@Composable
private fun MetricProgressRow(label: String, value: String, progress: Float) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = HarvestGreen,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun SideBySideRevenueCard(econ: IndicativeEconomicsSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Dual-Stream Revenue Comparison",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Market crop harvest vs stubble biomass monetization (${econ.acreage} Acres)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = HarvestGreen.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, HarvestGreen.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Primary Crop Yield", fontSize = 11.sp, color = DarkTextSecondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "INR ${econ.grossCropRevenue.toInt()}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HarvestGreenDark)
                        Text(text = "${econ.expectedYieldQuintals} Qtl @ INR ${econ.forecastModalRatePerQuintal.toInt()}/Qtl", fontSize = 10.sp, color = DarkTextSecondary)
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    color = GoldenWheatLight.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, GoldenWheatLight.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Biomass Residue", fontSize = 11.sp, color = DarkTextSecondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "INR ${econ.grossResidueRevenue.toInt()}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = GoldenWheatDark)
                        Text(text = "${econ.residueBiomassTons} MT @ INR ${econ.residueValuePerTon.toInt()}/MT", fontSize = 10.sp, color = DarkTextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Total Gross Farm Income", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(text = "INR ${econ.totalGrossRevenue.toInt()}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HarvestGreenDark)
                }
            }
        }
    }
}

@Composable
private fun ProductionCostsCard(
    econ: IndicativeEconomicsSummary,
    onEditCosts: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Estimated Production Expenditure", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(text = "Input breakdown per total acreage", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onEditCosts) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit Costs", tint = HarvestGreen, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            CostItemRow(label = "Seed & Nursery", amount = econ.seedCost)
            CostItemRow(label = "Fertilizer & Plant Nutrition", amount = econ.fertilizerNutrientCost)
            CostItemRow(label = "Crop Protection & Sprays", amount = econ.cropProtectionCost)
            CostItemRow(label = "Irrigation & Electricity", amount = econ.irrigationElectricityCost)
            CostItemRow(label = "Labor & Manual Harvesting", amount = econ.laborHarvestCost)
            CostItemRow(label = "Logistics & Transport", amount = econ.logisticsTransportCost)

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Total Input Expenditure", fontWeight = FontWeight.Bold)
                Text(text = "INR ${econ.totalProductionCost.toInt()}", fontWeight = FontWeight.Bold, color = AlertRed)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = econ.costSourceNote, style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
        }
    }
}

@Composable
private fun CostItemRow(label: String, amount: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = "INR ${amount.toInt()}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ProfitSensitivityCard(econ: IndicativeEconomicsSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Net Profit Sensitivity Model", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(text = "3-tier sensitivity reflecting mandi price fluctuations", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SensitivityCol(
                    modifier = Modifier.weight(1f),
                    tier = "Conservative",
                    net = econ.conservativeNetProfit,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
                SensitivityCol(
                    modifier = Modifier.weight(1.2f),
                    tier = "Expected",
                    net = econ.expectedNetProfit,
                    containerColor = HarvestGreen.copy(alpha = 0.18f),
                    isHighlight = true
                )
                SensitivityCol(
                    modifier = Modifier.weight(1f),
                    tier = "Optimistic",
                    net = econ.optimisticNetProfit,
                    containerColor = GoldenWheatLight.copy(alpha = 0.20f)
                )
            }
        }
    }
}

@Composable
private fun SensitivityCol(
    modifier: Modifier = Modifier,
    tier: String,
    net: Double,
    containerColor: Color,
    isHighlight: Boolean = false
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = if (isHighlight) BorderStroke(1.5.dp, HarvestGreen) else null
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = tier, fontSize = 11.sp, fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium, color = DarkTextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "INR ${net.toInt()}",
                fontSize = if (isHighlight) 15.sp else 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (net >= 0) HarvestGreenDark else AlertRed
            )
        }
    }
}

@Composable
private fun AgronomicStrengthsCard(summary: CropSuitabilitySummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Agronomic Strengths & Cautions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(10.dp))

            summary.agronomicAdvantages.forEach { adv ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = adv, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            summary.riskCaveats.forEach { cav ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = GoldenWheatDark, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = cav, style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                }
            }
        }
    }
}
