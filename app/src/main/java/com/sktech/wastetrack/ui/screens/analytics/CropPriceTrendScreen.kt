package com.sktech.wastetrack.ui.screens.analytics

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sktech.wastetrack.domain.model.MandiArrivalRadarItem
import com.sktech.wastetrack.domain.model.MandiComparisonItem
import com.sktech.wastetrack.domain.model.MonthlyPricePoint
import com.sktech.wastetrack.domain.model.PriceTrend
import com.sktech.wastetrack.ui.theme.AgriDeepForest
import com.sktech.wastetrack.ui.theme.AgriEarthyGold
import com.sktech.wastetrack.ui.theme.AgriPrimaryGreen
import com.sktech.wastetrack.ui.theme.DarkCard
import com.sktech.wastetrack.ui.theme.DarkHarvestGreenContainer
import com.sktech.wastetrack.ui.theme.DarkTextSecondary
import com.sktech.wastetrack.ui.theme.GoldenWheatLight
import com.sktech.wastetrack.ui.theme.HarvestGreen
import com.sktech.wastetrack.ui.theme.HarvestGreenDark
import com.sktech.wastetrack.ui.theme.HarvestGreenLight
import com.sktech.wastetrack.ui.theme.HarvestGreenSoft
import com.sktech.wastetrack.ui.theme.WarningOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropPriceTrendScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPnL: () -> Unit = {},
    viewModel: CropPriceTrendViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "APMC Mandi Price Trends",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Agmarknet Live Telemetry & Historical Modal Radar",
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
                            contentDescription = "Farm PnL",
                            tint = HarvestGreen
                        )
                    }
                    IconButton(
                        onClick = { viewModel.refresh() },
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
                    shape = RoundedCornerShape(12.dp),
                    color = HarvestGreen.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, HarvestGreen.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(HarvestGreen)
                            )
                            Text(
                                text = "LIVE APMC MANDI TELEMETRY",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = HarvestGreenDark
                            )
                        }
                        Text(
                            text = state.selectedState + " (" + state.activeMarketsCount + " Yards)",
                            style = MaterialTheme.typography.labelSmall,
                            color = HarvestGreenDark.copy(alpha = 0.85f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Agricultural State / Region",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.availableStates) { stateName ->
                            FilterChip(
                                selected = state.selectedState == stateName,
                                onClick = { viewModel.selectState(stateName) },
                                label = { Text(stateName, fontWeight = if (state.selectedState == stateName) FontWeight.Bold else FontWeight.Normal) },
                                leadingIcon = {
                                    if (state.selectedState == stateName) {
                                        Icon(
                                            imageVector = Icons.Filled.LocationOn,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = HarvestGreenDark
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldenWheatLight.copy(alpha = 0.3f),
                                    selectedLabelColor = HarvestGreenDark
                                )
                            )
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Crop Commodity Benchmark",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(state.availableCrops) { crop ->
                            FilterChip(
                                selected = state.selectedCrop == crop,
                                onClick = { viewModel.selectCrop(crop) },
                                label = { Text(crop, fontWeight = FontWeight.SemiBold) },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = HarvestGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            item {
                val price = state.modalRate ?: 2450.0
                val minRate = state.minRate ?: (price * 0.93)
                val peakRate = state.peakRate ?: (price * 1.07)
                val change = state.currentPrice?.changePercent ?: 3.8

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkHarvestGreenContainer),
                    border = BorderStroke(1.dp, HarvestGreenSoft.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = state.selectedCrop + " Benchmark Modal Rate",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = state.selectedState + " Zone Benchmark APMC Yard",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (change >= 0) HarvestGreen else Color(0xFFEF4444)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (change >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "%+.1f%%".format(change),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = "WEIGHTED MODAL SPOT RATE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "INR %,.0f".format(price),
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "/ Quintal",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldenWheatLight
                                )
                                Text(
                                    text = "INR %,.0f / MT".format(price * 10.0),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Min Yard Rate", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                                Text("INR %,.0f".format(minRate), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Modal Rate", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                                Text("INR %,.0f".format(price), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = GoldenWheatLight)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Peak Yard Rate", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                                Text("INR %,.0f".format(peakRate), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, if (state.isBullish) HarvestGreen.copy(alpha = 0.6f) else WarningOrange.copy(alpha = 0.6f))
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
                                Icon(
                                    imageVector = Icons.Filled.Lightbulb,
                                    contentDescription = null,
                                    tint = if (state.isBullish) HarvestGreen else WarningOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Moving Average & Volatility Indicators",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (state.isBullish) HarvestGreen.copy(alpha = 0.15f) else WarningOrange.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (state.isBullish) "BULLISH SIGNAL" else "NEUTRAL / HOLD",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.isBullish) HarvestGreenDark else WarningOrange
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("7-Day EMA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("INR %,.0f".format(state.ema7Day), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = HarvestGreenDark)
                            }
                            Column {
                                Text("30-Day EMA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("INR %,.0f".format(state.ema30Day), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column {
                                Text("Volatility Index", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("%.1f%%".format(state.volatilityPercent), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (state.volatilityPercent > 3.0) WarningOrange else HarvestGreenDark)
                            }
                            Column {
                                Text("Govt MSP Floor", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("INR %,.0f".format(state.mspBenchmarkPrice), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = AgriEarthyGold)
                            }
                        }

                        if (!state.breakoutSignal.isNullOrBlank()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = state.breakoutSignal ?: "",
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
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
                            Column {
                                Text(
                                    text = "12-Month APMC Spot vs MSP Benchmark Curve",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Interactive Bezier trajectory with MSP support floor",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = HarvestGreen.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "12M RADAR",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = HarvestGreenDark
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp, 3.dp)
                                        .background(HarvestGreen)
                                )
                                Text("APMC Spot Modal", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp, 3.dp)
                                        .background(AgriEarthyGold)
                                )
                                Text("MSP Floor Support", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        val points = state.monthlyHistoryPoints
                        if (points.isNotEmpty()) {
                            val minVal = (points.minOfOrNull { minOf(it.spotRate, it.mspRate) } ?: 2000.0) * 0.92
                            val maxVal = (points.maxOfOrNull { maxOf(it.spotRate, it.mspRate) } ?: 3000.0) * 1.06
                            val range = (maxVal - minVal).coerceAtLeast(1.0)
                            val msp = state.mspBenchmarkPrice

                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                            ) {
                                val w = size.width
                                val h = size.height
                                val spacing = w / (points.size - 1).coerceAtLeast(1)

                                val mspY = h - (((msp - minVal) / range).toFloat() * (h - 26f)) - 13f
                                drawLine(
                                    color = AgriEarthyGold,
                                    start = Offset(0f, mspY),
                                    end = Offset(w, mspY),
                                    strokeWidth = 3f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                                )

                                val spotPath = Path()
                                val fillPath = Path()

                                val screenCoords = points.mapIndexed { i, pt ->
                                    val x = i * spacing
                                    val normY = ((pt.spotRate - minVal) / range).toFloat()
                                    val y = h - (normY * (h - 26f)) - 13f
                                    Offset(x, y)
                                }

                                if (screenCoords.isNotEmpty()) {
                                    spotPath.moveTo(screenCoords.first().x, screenCoords.first().y)
                                    fillPath.moveTo(screenCoords.first().x, h)
                                    fillPath.lineTo(screenCoords.first().x, screenCoords.first().y)

                                    for (i in 0 until screenCoords.size - 1) {
                                        val p0 = screenCoords[i]
                                        val p1 = screenCoords[i + 1]
                                        val controlX = (p0.x + p1.x) / 2f
                                        spotPath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                                        fillPath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                                    }

                                    fillPath.lineTo(screenCoords.last().x, h)
                                    fillPath.close()

                                    drawPath(
                                        path = fillPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(HarvestGreen.copy(alpha = 0.35f), Color.Transparent)
                                        )
                                    )

                                    drawPath(
                                        path = spotPath,
                                        color = HarvestGreen,
                                        style = Stroke(width = 4.5f, cap = StrokeCap.Round)
                                    )

                                    screenCoords.forEach { pt ->
                                        drawCircle(color = HarvestGreenDark, radius = 5f, center = pt)
                                        drawCircle(color = Color.White, radius = 3f, center = pt)
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                points.filterIndexed { index, _ -> index % 2 == 0 || index == points.size - 1 }.forEach { pt ->
                                    Text(pt.monthLabel, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "7-Day Daily APMC Price Fluctuation",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        val maxPrice = state.priceHistory.maxOfOrNull { it.pricePerUnit } ?: 2500.0
                        val minPrice = state.priceHistory.minOfOrNull { it.pricePerUnit } ?: 2000.0
                        val baseline = (minPrice * 0.88).coerceAtLeast(0.0)
                        val effectiveMax = (maxPrice * 1.04).coerceAtLeast(baseline + 1.0)
                        val range = effectiveMax - baseline
                        val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
                        val latestPoint = state.priceHistory.lastOrNull()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            state.priceHistory.forEach { point ->
                                val heightFraction = if (range <= 0.0) 0.6f else ((point.pricePerUnit - baseline) / range).coerceIn(0.22, 1.0).toFloat()
                                val isLatest = point == latestPoint

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "INR %,.0f".format(point.pricePerUnit),
                                        fontSize = 9.sp,
                                        fontWeight = if (isLatest) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (isLatest) HarvestGreenDark else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(22.dp)
                                                .fillMaxHeight(heightFraction)
                                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                                                .background(
                                                    if (isLatest) {
                                                        Brush.verticalGradient(
                                                            listOf(HarvestGreen, HarvestGreen.copy(alpha = 0.7f))
                                                        )
                                                    } else {
                                                        Brush.verticalGradient(
                                                            listOf(HarvestGreenDark.copy(alpha = 0.45f), HarvestGreenDark.copy(alpha = 0.22f))
                                                        )
                                                    }
                                                )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = dateFormat.format(Date(point.observedAt)),
                                        fontSize = 8.5.sp,
                                        fontWeight = if (isLatest) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isLatest) HarvestGreenDark else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Mandi Arrival Intake Radar",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            items(state.arrivalRadarItems) { radar ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = radar.yardName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = radar.location,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "%,.0f Qtl Today".format(radar.dailyArrivalQuintals),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = HarvestGreenDark
                                )
                                Text(
                                    text = "INR %,.0f / Qtl Modal".format(radar.modalRateQuintal),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Yard Capacity Utilization: " + radar.capacityUtilizationPercent + "%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            LinearProgressIndicator(
                                progress = { radar.capacityUtilizationPercent / 100f },
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (radar.capacityUtilizationPercent > 75) WarningOrange else HarvestGreen,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }

                        Text(
                            text = radar.trendNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Regional APMC Spot Rate Comparisons",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            items(state.marketComparison) { item ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.mandiName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = item.district + ", " + item.state + " (" + item.distanceKm + " km away)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "INR %,.0f".format(item.pricePerUnit),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = HarvestGreenDark
                                )
                                Text(
                                    text = "/ " + item.unit + " (" + item.trend.displayName + ")",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!item.arrivalDate.isNullOrBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = GoldenWheatLight.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = item.arrivalDate ?: "",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        color = HarvestGreenDark,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            if (!item.variety.isNullOrBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = item.variety ?: "",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (item.minPrice != null && item.maxPrice != null) {
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "Range: %,.0f - %,.0f".format(item.minPrice, item.maxPrice),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = HarvestGreen.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, HarvestGreen.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lightbulb,
                            contentDescription = null,
                            tint = HarvestGreenDark
                        )
                        Text(
                            text = "Mandi Advisory: Ensure harvested crop moisture meets APMC standards (< 12% for grains, < 14% for stalks). Combine farm stubble with primary crop transit to optimize backhaul freight costs.",
                            style = MaterialTheme.typography.bodySmall,
                            color = HarvestGreenDark
                        )
                    }
                }
            }
        }
    }
}
