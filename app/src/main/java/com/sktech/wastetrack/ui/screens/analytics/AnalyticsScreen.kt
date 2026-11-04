package com.sktech.wastetrack.ui.screens.analytics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sktech.wastetrack.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToPriceTrends: () -> Unit = {},
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Enterprise Analytics", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Carbon Offsets & Biomass Revenue", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.openCertificateDialog() }) {
                        Icon(imageVector = Icons.Filled.WorkspacePremium, contentDescription = "Certificate", tint = HarvestGreen)
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
                CarbonOffsetHeroCard(
                    co2Avoided = state.co2AvoidedMt,
                    treesEquivalent = state.treesEquivalent,
                    cleanEnergyKwh = state.cleanEnergyGeneratedKwh,
                    onOpenCertificate = { viewModel.openCertificateDialog() }
                )
            }

            item {
                MonthlyRevenueChartCard(
                    monthlyData = state.monthlyData,
                    totalEarned = state.totalRevenueEarned
                )
            }

            item {
                CategoryBreakdownCard(shares = state.categoryShares)
            }

            item {
                MandiPriceBenchmarkCard(benchmarks = state.mandiBenchmarks, onClick = onNavigateToPriceTrends)
            }
        }
    }

    if (state.showCertificateDialog) {
        val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }
        AlertDialog(
            onDismissRequest = { viewModel.dismissCertificateDialog() },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Filled.Verified, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clean Air Impact Certificate", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = HarvestGreenLight.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("AstraNyx Agro-Ecology Verified", style = MaterialTheme.typography.labelSmall, color = HarvestGreenDark, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${state.co2AvoidedMt} MT CO?e",
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                color = HarvestGreenDark
                            )
                            Text("Averted Stubble Burning Emissions", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                            HorizontalDivider(color = HarvestGreen.copy(alpha = 0.3f), thickness = 1.dp)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Issue Date:", style = MaterialTheme.typography.labelSmall)
                                Text(dateFormat.format(Date()), fontWeight = FontWeight.Medium, fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Certificate Hash:", style = MaterialTheme.typography.labelSmall)
                                Text(state.certificateHash, style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissCertificateDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                ) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
private fun CarbonOffsetHeroCard(
    co2Avoided: Double,
    treesEquivalent: Int,
    cleanEnergyKwh: Double,
    onOpenCertificate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkHarvestGreenContainer),
        border = BorderStroke(1.dp, HarvestGreenSoft.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = HarvestGreenSoft.copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Filled.Eco, contentDescription = null, tint = HarvestGreenSoft, modifier = Modifier.size(20.dp))
                        }
                    }
                    Column {
                        Text("Stubble Burning Offset", fontWeight = FontWeight.Bold, color = HarvestGreenSoft, fontSize = 15.sp)
                        Text("Carbon Neutrality Metric", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = HarvestGreenSoft.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "AUDIT VERIFIED",
                        color = HarvestGreenSoft,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Column {
                Text("CO? Equivalent Emissions Averted", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                Text(
                    text = "$co2Avoided MT",
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = Color.White
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Trees Equivalent", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                        Text("$treesEquivalent Trees", fontWeight = FontWeight.Bold, color = GoldenWheatLight, fontSize = 14.sp)
                    }
                }

                Surface(
                    color = Color.Black.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Green Power", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                        Text(String.format(Locale.US, "%,.0f kWh", cleanEnergyKwh), fontWeight = FontWeight.Bold, color = SkyBlueLight, fontSize = 14.sp)
                    }
                }
            }

            Button(
                onClick = onOpenCertificate,
                colors = ButtonDefaults.buttonColors(containerColor = HarvestGreenSoft, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Filled.WorkspacePremium, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("View Carbon Offset Certificate", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun MonthlyRevenueChartCard(
    monthlyData: List<com.sktech.wastetrack.ui.screens.analytics.MonthlyRevenue>,
    totalEarned: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Biomass Waste Monetization", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("6-Month Cumulative Earnings", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                }
                Text(
                    text = String.format(Locale.US, "?%,.0f", totalEarned),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = HarvestGreenDark
                )
            }

            val maxRevenue = remember(monthlyData) { monthlyData.maxOfOrNull { it.revenueAmount } ?: 1.0 }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(vertical = 8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val barCount = monthlyData.size
                    val spacing = w / barCount
                    val barWidth = spacing * 0.45f

                    for (i in 0 until barCount) {
                        val item = monthlyData[i]
                        val fraction = (item.revenueAmount / maxRevenue).toFloat()
                        val barHeight = h * fraction * 0.85f

                        val left = i * spacing + (spacing - barWidth) / 2f
                        val top = h - barHeight

                        drawRoundRect(
                            color = Color(0xFF2E7D32),
                            topLeft = Offset(left, top),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                monthlyData.forEach { item ->
                    Text(
                        text = item.monthName,
                        style = MaterialTheme.typography.labelSmall,
                        color = DarkTextSecondary,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryBreakdownCard(shares: Map<String, Float>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Residue Volume Utilization", fontWeight = FontWeight.Bold, fontSize = 15.sp)

            shares.forEach { (cat, frac) ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(cat, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                        Text(String.format(Locale.US, "%.0f%%", frac * 100f), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = HarvestGreenDark)
                    }
                    LinearProgressIndicator(
                        progress = { frac },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = HarvestGreen,
                        trackColor = HarvestGreenLight.copy(alpha = 0.25f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MandiPriceBenchmarkCard(
    benchmarks: List<com.sktech.wastetrack.ui.screens.analytics.MandiPriceBenchmark>,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Regional Mandi Spot Rates", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("APMC Benchmark Price / MT", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                }
                Icon(imageVector = Icons.Filled.Storefront, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(20.dp))
            }

            benchmarks.forEach { bm ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(bm.cropName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(bm.mandiLocation, style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(String.format(Locale.US, "?%,.0f / MT", bm.ratePerMt), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (bm.isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = if (bm.isPositive) HarvestGreen else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = String.format(Locale.US, "%.1f%%", bm.changePercent),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (bm.isPositive) HarvestGreen else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
