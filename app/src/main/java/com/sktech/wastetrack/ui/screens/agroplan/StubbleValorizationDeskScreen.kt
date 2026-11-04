package com.sktech.wastetrack.ui.screens.agroplan

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sktech.wastetrack.domain.agro.model.ValorizationChannel
import com.sktech.wastetrack.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StubbleValorizationDeskScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAgroPlan: (String, String) -> Unit = { _, _ -> },
    onNavigateToCreateListing: () -> Unit = {},
    viewModel: StubbleValorizationViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val cropOptions = listOf("Cotton", "Sugarcane", "Soybean")
    val balingCostOptions = listOf(500.0, 600.0, 750.0)
    val freightOptions = listOf(15.0, 25.0, 45.0, 65.0)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Stubble Valorization Desk", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text(
                            text = "${state.summary?.cropName ?: "Agro Residue"} • Multi-Channel ROI",
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
                actions = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = HarvestGreenContainer,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Filled.Eco, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(14.dp))
                            Text(
                                text = "Zero Stubble Burning",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = HarvestGreenDark
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        val summary = state.summary
        if (summary == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = HarvestGreen)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(cropOptions) { crop ->
                            val isSelected = state.cropName.contains(crop, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectCrop(crop) },
                                label = { Text(crop, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
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
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = GoldenWheatContainer
                                    ) {
                                        Text(
                                            text = summary.farmName,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldenWheatDark
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "${summary.cropName} Crop Biomass",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(HarvestGreen.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Recycling, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(24.dp))
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Biomass Payload", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "${summary.estimatedTonnage} MT Stubble",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = HarvestGreenDark
                                    )
                                    Text(
                                        text = "From ${summary.totalAcreage} Acres",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Thermal Fuel Grade", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "${summary.baseCalorificMjKg} MJ/kg",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldenWheatDark
                                    )
                                    Text(
                                        text = "<${summary.moisturePercent}% Moisture",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = HarvestGreen
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Total Offtake Value", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "₹${String.format(Locale.US, "%,.0f", summary.totalGrossPotentialInr)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = AgriDeepForest
                                    )
                                    Text(
                                        text = "Combined Monetization",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Valorization Channels & Industrial Offtake Pathways",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(summary.channels, key = { it.id }) { channel ->
                    val isSelected = state.selectedChannelId == channel.id
                    ValorizationChannelCard(
                        channel = channel,
                        isSelected = isSelected,
                        onClick = { viewModel.selectChannel(channel.id) }
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, GoldenWheatDark.copy(alpha = 0.4f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                                Text(
                                    text = "Logistics & Net Margin Simulator",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = HarvestGreenDark
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = GoldenWheatContainer
                                ) {
                                    Text(
                                        text = "Live Sensitivity Model",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldenWheatDark,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Adjust baling machinery tariff and freight corridor radius to project net farm-gate returns.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text("Baling & Shredding Cost (₹/Ton)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                balingCostOptions.forEach { cost ->
                                    val isSelected = state.customBalingCostPerTon == cost
                                    SuggestionChip(
                                        onClick = { viewModel.updateBalingCost(cost) },
                                        label = { Text("₹${cost.toInt()} / MT", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = if (isSelected) HarvestGreen else HarvestGreen.copy(alpha = 0.08f),
                                            labelColor = if (isSelected) Color.White else HarvestGreenDark
                                        ),
                                        border = BorderStroke(1.dp, HarvestGreen.copy(alpha = 0.3f))
                                    )
                                }
                            }

                            Text("Logistics Freight Distance (km)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                freightOptions.forEach { km ->
                                    val isSelected = state.customFreightKm == km
                                    SuggestionChip(
                                        onClick = { viewModel.updateFreightKm(km) },
                                        label = { Text("${km.toInt()} km", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = if (isSelected) SkyBlueDark else SkyBlueContainer,
                                            labelColor = if (isSelected) Color.White else SkyBlueDark
                                        ),
                                        border = BorderStroke(1.dp, SkyBlueDark.copy(alpha = 0.3f))
                                    )
                                }
                            }

                            val activeChannel = summary.channels.find { it.id == state.selectedChannelId } ?: summary.channels.first()
                            val simulatedRevenue = viewModel.calculateSimulatedNetRevenue(activeChannel.netRevenuePerAcreInr)

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Simulated Net Profit / Acre", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "${activeChannel.name.take(18)}...",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = HarvestGreenDark,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = "₹${String.format(Locale.US, "%,.0f", simulatedRevenue)} / Ac",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = HarvestGreenDark
                                )
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, HarvestGreen.copy(alpha = 0.35f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.Verified, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(20.dp))
                                Text("Environmental & ESG Carbon Abatement", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Text(
                                text = "Zero crop residue burning on ${summary.totalAcreage} acres avoids ${(summary.totalAcreage * 4.2).toInt()} Metric Tons of CO₂ equivalent emissions. Fully verified under MPCB clean air mandates.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateToCreateListing,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, HarvestGreen)
                        ) {
                            Icon(Icons.Filled.Storefront, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("List Lot", color = HarvestGreenDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Button(
                            onClick = { onNavigateToAgroPlan(state.farmId, state.cropName) },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                        ) {
                            Icon(Icons.Filled.BarChart, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AgroPlan Matrix", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun ValorizationChannelCard(
    channel: ValorizationChannel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) HarvestGreenContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (isSelected) HarvestGreen else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(channel.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    if (channel.isRecommendedBest) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = HarvestGreen
                        ) {
                            Text(
                                text = "Best Offtake",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GoldenWheatContainer
                ) {
                    Text(
                        text = channel.categoryBadge,
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldenWheatDark,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Net Revenue / Acre", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "₹${String.format(Locale.US, "%,.0f", channel.netRevenuePerAcreInr)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = HarvestGreenDark
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Net ROI", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${channel.netRoiPercent.toInt()}%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = HarvestGreen
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Payback Period", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${channel.paybackDays} Days",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Buyers: ${channel.targetBuyers.take(28)}...",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "CO₂: -${channel.carbonAbatementTonsCo2} MT/Ac",
                    style = MaterialTheme.typography.labelSmall,
                    color = HarvestGreenDark,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

