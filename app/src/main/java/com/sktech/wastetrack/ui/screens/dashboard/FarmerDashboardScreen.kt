package com.sktech.wastetrack.ui.screens.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sktech.wastetrack.ui.theme.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import com.sktech.wastetrack.R
import com.sktech.wastetrack.ui.components.tutorial.FarmerWelcomeDialog
import com.sktech.wastetrack.ui.components.tutorial.TutorialCoachMarkOverlay
import com.sktech.wastetrack.ui.components.tutorial.TutorialEventBus
import com.sktech.wastetrack.ui.components.tutorial.TutorialTargetKey
import com.sktech.wastetrack.ui.components.tutorial.rememberTutorialState
import com.sktech.wastetrack.ui.components.tutorial.tutorialTarget


@Composable
fun FarmerDashboardScreen(
    onNavigateToCrops: () -> Unit = {},
    onNavigateToMarket: () -> Unit = {},
    onNavigateToWeather: () -> Unit = {},
    onNavigateToTransfers: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {},
    onNavigateToFarms: () -> Unit = {},
    onNavigateToAgent: () -> Unit = {},
    onNavigateToESGDashboard: () -> Unit = {},
    onNavigateToCustomAgroPlan: (String) -> Unit = {},
    onNavigateToDiseaseDetection: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    viewModel: FarmerDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val farmerName = state.user?.name ?: "Kisan Producer"

    val tutorialState = rememberTutorialState(
        onComplete = { viewModel.setTutorialCompleted(true) }
    )

    var hasShownWelcomeThisSession by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.isTutorialCompleted) {
        if (!state.isTutorialCompleted && !hasShownWelcomeThisSession) {
            hasShownWelcomeThisSession = true
            tutorialState.showWelcome()
        }
    }

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        TutorialEventBus.events.collect { cmd ->
            when (cmd.lowercase()) {
                "start" -> tutorialState.startTour()
                "next" -> tutorialState.nextStep()
                "prev" -> tutorialState.prevStep()
                "finish", "skip", "dismiss" -> tutorialState.finishTour()
                "welcome" -> tutorialState.showWelcome()
                "weather", "step0" -> tutorialState.goToStep(0)
                "scan", "step1" -> tutorialState.goToStep(1)
                "crops", "step2" -> tutorialState.goToStep(2)
                "mandi", "step3" -> tutorialState.goToStep(3)
                "agent", "step4" -> tutorialState.goToStep(4)
            }
        }
    }

    LaunchedEffect(tutorialState.currentStepIndex, tutorialState.isOverlayVisible) {
        if (tutorialState.isOverlayVisible) {
            when (tutorialState.currentStep?.key) {
                TutorialTargetKey.WEATHER -> listState.animateScrollToItem(1)
                TutorialTargetKey.SCAN -> listState.animateScrollToItem(2)
                TutorialTargetKey.CROPS -> listState.animateScrollToItem(4)
                TutorialTargetKey.MANDI -> listState.animateScrollToItem(5)
                TutorialTargetKey.AGENT -> listState.animateScrollToItem(2)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = HarvestGreen
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Namaste, $farmerName",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Kisan Producer Hub • Nashik",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    viewModel.restartTutorial()
                                    tutorialState.showWelcome()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.HelpOutline,
                                    contentDescription = stringResource(R.string.cd_tutorial_replay),
                                    tint = GoldenWheatLight,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            IconButton(onClick = onNavigateToNotifications) {
                                Icon(
                                    imageVector = Icons.Filled.Notifications,
                                    contentDescription = "Agro Subsidies & Alerts",
                                    tint = GoldenWheatLight,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = GoldenWheatLight,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .tutorialTarget(TutorialTargetKey.WEATHER, tutorialState),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = GoldenWheatContainer
                ),
                border = BorderStroke(1.dp, GoldenWheat.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = GoldenWheat.copy(alpha = 0.2f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.WbSunny,
                                contentDescription = "Weather",
                                tint = GoldenWheatDark
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${state.currentTemperature}°C • ${state.weatherDescription}",
                            fontWeight = FontWeight.Bold,
                            color = DarkTextPrimary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = state.advisoryHeadline,
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkTextSecondary
                        )
                    }
                    TextButton(onClick = onNavigateToWeather) {
                        Text("Details", fontWeight = FontWeight.SemiBold, color = HarvestGreen)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .tutorialTarget(TutorialTargetKey.AGENT, tutorialState)
                    .clickable { onNavigateToAgent() },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = HarvestGreenContainer
                ),
                border = BorderStroke(1.dp, HarvestGreen.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = HarvestGreenDark,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Psychology,
                                    contentDescription = "Kisan AI Copilot",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Ask Kisan AI Assistant",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = HarvestGreenDark
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = HarvestGreenDark
                                ) {
                                    Text(
                                        text = "AI COPILOT",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Mandi price rates, stubble guidelines & voice queries",
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkTextSecondary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = HarvestGreenDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .tutorialTarget(TutorialTargetKey.SCAN, tutorialState)
                    .clickable { onNavigateToCustomAgroPlan("default_farm") },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DarkHarvestGreenContainer
                ),
                border = BorderStroke(1.dp, GoldenWheatLight.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = GoldenWheatLight.copy(alpha = 0.2f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Eco,
                                    contentDescription = "AgroPlan",
                                    tint = GoldenWheatLight,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Personalized Custom AgroPlan",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = GoldenWheatLight
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = HarvestGreen
                                ) {
                                    Text(
                                        text = "NPK RADAR",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Precision fertilizer dosages, rain radar & residue valorization",
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkTextSecondary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Go to AgroPlan",
                        tint = GoldenWheatLight,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        item {
            Text(
                text = "Farm & Residue Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Active Crops",
                    value = "${state.activeCropCount}",
                    subtitle = "Growing cycles",
                    icon = Icons.Filled.Agriculture,
                    containerColor = HarvestGreenContainer,
                    iconTint = HarvestGreen,
                    modifier = Modifier
                        .weight(1f)
                        .tutorialTarget(TutorialTargetKey.CROPS, tutorialState)
                )
                MetricCard(
                    title = "Total Land",
                    value = "${state.totalPlotAcres} ac",
                    subtitle = "Cultivated plots",
                    icon = Icons.Filled.Terrain,
                    containerColor = SkyBlueContainer,
                    iconTint = SkyBlue,
                    modifier = Modifier.weight(1f).clickable { onNavigateToFarms() }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Available Residue",
                    value = "${String.format("%,.0f", state.totalResidueKg)} kg",
                    subtitle = "Ready for buyers",
                    icon = Icons.Filled.Recycling,
                    containerColor = GoldenWheatContainer,
                    iconTint = GoldenWheatDark,
                    modifier = Modifier
                        .weight(1f)
                        .tutorialTarget(TutorialTargetKey.MANDI, tutorialState)
                )
                MetricCard(
                    title = "Dealer Bids",
                    value = "${state.pendingInquiriesCount}",
                    subtitle = "Open bids to accept",
                    icon = Icons.Filled.LocalOffer,
                    containerColor = HarvestGreenContainer,
                    iconTint = HarvestGreenLight,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionChip(
                    label = "Disease Radar",
                    icon = Icons.Filled.Biotech,
                    onClick = onNavigateToDiseaseDetection,
                    modifier = Modifier.weight(1f)
                )
                ActionChip(
                    label = "Log Crop",
                    icon = Icons.Filled.Add,
                    onClick = onNavigateToCrops,
                    modifier = Modifier.weight(1f)
                )
                ActionChip(
                    label = "Sell Residue",
                    icon = Icons.Filled.Storefront,
                    onClick = onNavigateToMarket,
                    modifier = Modifier.weight(1f)
                )
                ActionChip(
                    label = "Dispatches",
                    icon = Icons.Filled.LocalShipping,
                    onClick = onNavigateToTransfers,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToESGDashboard() },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, HarvestGreen.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = HarvestGreenLight.copy(alpha = 0.3f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(imageVector = Icons.Filled.Eco, contentDescription = null, tint = HarvestGreenDark, modifier = Modifier.size(22.dp))
                            }
                        }
                        Column {
                            Text("ESG Carbon Abatement & Stubble Credits", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Scope 1/2/3 avoided CO₂e ledger • MPCB verified", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                        }
                    }
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(18.dp))
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
                    text = "Recent Residue Batches",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = onNavigateToMarket) {
                    Text("View All", color = HarvestGreen, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        items(state.recentResidues, key = { it.id }) { item ->
            ResidueBatchCard(item)
        }
    }

    TutorialCoachMarkOverlay(
        tutorialState = tutorialState,
        speechManager = viewModel.speechManager,
        listState = listState
    )

    if (tutorialState.isWelcomeDialogVisible) {
        FarmerWelcomeDialog(
            farmerName = farmerName,
            onStartTour = { tutorialState.startTour() },
            onDismiss = {
                viewModel.setTutorialCompleted(true)
                tutorialState.skipTour()
            },
            speechManager = viewModel.speechManager
        )
    }
}
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun ActionChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun ResidueBatchCard(item: CropResidueItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = HarvestGreenContainer,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Eco,
                        contentDescription = null,
                        tint = HarvestGreen
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.cropName} • ${item.residueType}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${String.format("%,.0f", item.quantityKg)} kg • Est. ₹${String.format("%,.0f", item.estimatedValueInr)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = HarvestGreen.copy(alpha = 0.1f)
            ) {
                Text(
                    text = item.status,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = HarvestGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
