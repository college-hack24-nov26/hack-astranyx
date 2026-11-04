package com.sktech.wastetrack.ui.screens.analytics

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sktech.wastetrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MandiPriceTrendScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPnL: () -> Unit,
    viewModel: MandiPriceTrendViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "APMC Mandi Rates & Trends",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Agmarknet Live Telemetry • Maharashtra",
                            style = MaterialTheme.typography.labelSmall,
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
                    IconButton(onClick = onNavigateToPnL) {
                        Icon(
                            imageVector = Icons.Filled.AccountBalanceWallet,
                            contentDescription = "Farm P&L",
                            tint = HarvestGreen
                        )
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkHarvestGreenContainer),
                    border = BorderStroke(1.dp, HarvestGreenSoft.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${state.selectedCrop} Benchmark Modal Rate",
                                    color = HarvestGreenLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "INR ${String.format("%,.0f", state.selectedModalRate)} / Quintal",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = Color.White
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = HarvestGreen.copy(alpha = 0.3f),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ShowChart,
                                        contentDescription = null,
                                        tint = GoldenWheatLight,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = HarvestGreenSoft.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Industrial Metric Ton (MT)", color = DarkTextSecondary, fontSize = 11.sp)
                                Text("INR ${String.format("%,.0f", state.selectedModalRate * 10)}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            }
                            Column {
                                Text("Stubble Floor Value", color = DarkTextSecondary, fontSize = 11.sp)
                                Text("INR ${String.format("%,.0f", state.selectedStubbleValue)} / MT", fontWeight = FontWeight.Bold, color = GoldenWheatLight, fontSize = 13.sp)
                            }
                            Column {
                                Text("Mandi Coverage", color = DarkTextSecondary, fontSize = 11.sp)
                                Text("${state.filteredRates.size} Hubs", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "30-Day Agmarknet Price Trajectory",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Nashik APMC Zone",
                                fontSize = 11.sp,
                                color = GoldenWheatLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val points = state.historyPoints
                        val minVal = (points.minOfOrNull { it.ratePerQuintal } ?: 2000.0) - 100.0
                        val maxVal = (points.maxOfOrNull { it.ratePerQuintal } ?: 3000.0) + 100.0
                        val range = (maxVal - minVal).coerceAtLeast(1.0)

                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        ) {
                            val w = size.width
                            val h = size.height
                            val spacing = w / (points.size - 1).coerceAtLeast(1)

                            val path = Path()
                            val fillPath = Path()

                            points.forEachIndexed { i, pt ->
                                val x = i * spacing
                                val normY = ((pt.ratePerQuintal - minVal) / range).toFloat()
                                val y = h - (normY * (h - 20f)) - 10f

                                if (i == 0) {
                                    path.moveTo(x, y)
                                    fillPath.moveTo(x, h)
                                    fillPath.lineTo(x, y)
                                } else {
                                    path.lineTo(x, y)
                                    fillPath.lineTo(x, y)
                                }
                            }

                            fillPath.lineTo(w, h)
                            fillPath.close()

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(HarvestGreenLight.copy(alpha = 0.35f), Color.Transparent)
                                )
                            )

                            drawPath(
                                path = path,
                                color = HarvestGreenLight,
                                style = Stroke(width = 5f)
                            )

                            points.forEachIndexed { i, pt ->
                                val x = i * spacing
                                val normY = ((pt.ratePerQuintal - minVal) / range).toFloat()
                                val y = h - (normY * (h - 20f)) - 10f
                                drawCircle(color = HarvestGreenDark, radius = 7f, center = Offset(x, y))
                                drawCircle(color = GoldenWheatLight, radius = 4f, center = Offset(x, y))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            points.forEach { pt ->
                                Text(pt.dateLabel, fontSize = 9.sp, color = DarkTextSecondary)
                            }
                        }
                    }
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.crops) { crop ->
                        FilterChip(
                            selected = state.selectedCrop == crop,
                            onClick = { viewModel.selectCrop(crop) },
                            label = { Text(crop, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HarvestGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    label = { Text("Search Mandi / Yard (e.g. Lasalgaon, Sinnar)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = DarkTextSecondary)
                    }
                )
            }

            items(state.filteredRates) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = item.marketName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Arrivals: ${item.lastArrivalsMt} MT today",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DarkTextSecondary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (item.isRising) HarvestGreen.copy(alpha = 0.2f) else WarningOrange.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (item.isRising) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (item.isRising) HarvestGreenLight else WarningOrange
                                    )
                                    Text(
                                        text = "${if (item.trendPercent >= 0) "+" else ""}${item.trendPercent}%",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (item.isRising) HarvestGreenLight else WarningOrange
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Min Rate", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                                Text("INR ${String.format("%,.0f", item.minRateQuintal)}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                            Column {
                                Text("Modal Rate", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                                Text("INR ${String.format("%,.0f", item.modalRateQuintal)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GoldenWheatLight)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Max Rate", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                                Text("INR ${String.format("%,.0f", item.maxRateQuintal)}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Residual Stubble Floor Value:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DarkTextSecondary
                                )
                                Text(
                                    text = "INR ${String.format("%,.0f", item.stubbleResidueValuePerMt)} / MT",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = HarvestGreenLight
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

