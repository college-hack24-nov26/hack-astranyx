package com.sktech.wastetrack.ui.screens.crop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import com.sktech.wastetrack.domain.model.CropActivity
import com.sktech.wastetrack.domain.model.CropCycle
import com.sktech.wastetrack.domain.model.GrowthStage
import com.sktech.wastetrack.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropCycleDetailScreen(
    cycleId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToScanner: () -> Unit = {},
    onNavigateToMarket: () -> Unit = {},
    viewModel: CropCycleViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val crop = remember(state.crops, cycleId) {
        state.crops.find { it.id == cycleId } ?: state.crops.firstOrNull()
    }
    val activities = remember(state.activities, cycleId) {
        state.activities[cycleId] ?: emptyList()
    }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    if (crop == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Crop record not found")
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("${crop.cropName} Lifecycle", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.openLogSheet() }) {
                        Icon(imageVector = Icons.Filled.AddCircleOutline, contentDescription = "Log Activity", tint = HarvestGreen)
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "${crop.cropName} (${crop.variety})", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                Text(text = "${crop.village} • ${crop.category}", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = HarvestGreenContainer
                            ) {
                                Text(
                                    text = "${crop.healthScorePercent}% Health",
                                    color = HarvestGreenDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Plot Area", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                                Text(text = "${crop.plotAreaAcres} Acres", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Column {
                                Text(text = "Sown On", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                                Text(text = dateFormat.format(Date(crop.sowingDateMillis)), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Column {
                                Text(text = "Target Harvest", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                                Text(text = dateFormat.format(Date(crop.expectedHarvestMillis)), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Current Growth Stage", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                text = crop.growthStage.displayName,
                                fontWeight = FontWeight.Bold,
                                color = HarvestGreen,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { crop.growthStage.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = HarvestGreen,
                            trackColor = HarvestGreenContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.advanceGrowthStage(crop.id) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Advance To Next Stage", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldenWheatContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Filled.Recycling, contentDescription = null, tint = GoldenWheatDark)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Residue & Biomass Output", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = GoldenWheatDark)
                            }
                            Text(
                                text = "~${crop.projectedResidueMt} MT",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = GoldenWheatDark
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Estimated harvest residue volume eligible for industrial sale or biocarbon processing.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6D4C00)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onNavigateToScanner,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Camera Scan", fontSize = 12.sp)
                            }
                            Button(
                                onClick = onNavigateToMarket,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                            ) {
                                Text("View Market", fontSize = 12.sp)
                            }
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
                    Text("Field Activity Timeline", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    TextButton(onClick = { viewModel.openLogSheet() }) {
                        Text("+ Log Event", color = HarvestGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(activities) { act ->
                ActivityTimelineItem(activity = act)
            }
        }
    }

    if (state.showLogSheet) {
        val activityTypes = listOf("Irrigation", "Fertilizer", "Pest Spray", "Weeding", "Soil Test")
        AlertDialog(
            onDismissRequest = { viewModel.closeLogSheet() },
            title = { Text("Log Field Activity", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Activity Type:", style = MaterialTheme.typography.labelSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        activityTypes.take(3).forEach { t ->
                            FilterChip(
                                selected = state.activityTypeInput == t,
                                onClick = { viewModel.updateActivityType(t) },
                                label = { Text(t, fontSize = 11.sp) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = state.activityDosageInput,
                        onValueChange = { viewModel.updateActivityDosage(it) },
                        label = { Text("Dosage / Quantity (e.g. 5 kg/acre)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.activityNotesInput,
                        onValueChange = { viewModel.updateActivityNotes(it) },
                        label = { Text("Notes / Observations") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.submitActivity(crop.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                ) {
                    Text("Save Activity")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeLogSheet() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ActivityTimelineItem(activity: CropActivity) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = HarvestGreenContainer,
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (activity.activityType) {
                            "Irrigation" -> Icons.Filled.WaterDrop
                            "Fertilizer" -> Icons.Filled.Science
                            "Pest Spray" -> Icons.Filled.BugReport
                            else -> Icons.Filled.Eco
                        },
                        contentDescription = null,
                        tint = HarvestGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = activity.activityType, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = timeFormat.format(Date(activity.timestampMillis)), style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = activity.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Application: ${activity.dosageAmount}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = HarvestGreen)
            }
        }
    }
}
