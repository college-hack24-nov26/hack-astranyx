package com.sktech.wastetrack.ui.screens.crop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sktech.wastetrack.domain.model.GrowthStage
import com.sktech.wastetrack.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCropCycleScreen(
    onNavigateBack: () -> Unit,
    onSaveSuccess: () -> Unit = {},
    viewModel: AddCropCycleViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    val crops = listOf("Cotton", "Sugarcane", "Soybean", "Wheat", "Rice", "Tomato", "Onion", "Maize")
    val durationPresets = listOf(
        90 to "90d (3 Mo)",
        120 to "120d (4 Mo)",
        150 to "150d (5 Mo)",
        180 to "180d (6 Mo)",
        365 to "Annual (1 Yr)"
    )

    val growthStages = listOf(
        GrowthStage.PLANNING,
        GrowthStage.SOWING,
        GrowthStage.GERMINATION,
        GrowthStage.VEGETATIVE,
        GrowthStage.FLOWERING,
        GrowthStage.FRUITING,
        GrowthStage.HARVEST_READY
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Register Crop Planting", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Cadastral Field Allocation & Growth Tracking", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            state.errorMessage?.let { err ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Text(text = err, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "1. Target Land Parcel (7/12 Cadastre)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = HarvestGreenDark
                    )

                    state.availableFarms.forEach { farm ->
                        val isSelected = state.selectedFarmId == farm.id
                        Surface(
                            onClick = { viewModel.onFarmSelected(farm.id) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) HarvestGreenContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) HarvestGreen else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.onFarmSelected(farm.id) },
                                        colors = RadioButtonDefaults.colors(selectedColor = HarvestGreen)
                                    )
                                    Column {
                                        Text(farm.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("${farm.surveyNumber} • ${farm.village}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Text("${farm.plotAreaAcres} Acres", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = HarvestGreenDark)
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "2. Crop Specie & Seed Variety",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = HarvestGreenDark
                    )

                    Text("Popular Maharashtra Crops", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(crops) { crop ->
                            val isSelected = state.cropName.equals(crop, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.onCropNameChange(crop) },
                                label = { Text(crop, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = state.variety,
                        onValueChange = viewModel::onVarietyChange,
                        label = { Text("Certified Seed Variety") },
                        placeholder = { Text("e.g. Bt-II Gold Hybrid") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = state.plotAreaAcresText,
                            onValueChange = viewModel::onAreaChange,
                            label = { Text("Cultivated Area (Acres)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = GoldenWheatContainer,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("Residue Forecast", style = MaterialTheme.typography.labelSmall, color = GoldenWheatDark)
                                Text("${state.projectedResidueMtText} MT Biomass", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GoldenWheatDark)
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "3. Sowing Timeline & Phenology Stages",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = HarvestGreenDark
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Sowing Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(dateFormat.format(Date(state.sowingDateMillis)), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Expected Harvest", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(dateFormat.format(Date(state.expectedHarvestDateMillis)), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = HarvestGreenDark)
                        }
                    }

                    Text("Crop Duration Presets", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(durationPresets) { (days, label) ->
                            val isSelected = state.selectedDurationDays == days
                            SuggestionChip(
                                onClick = { viewModel.onDurationPresetChange(days) },
                                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (isSelected) HarvestGreen else HarvestGreen.copy(alpha = 0.08f),
                                    labelColor = if (isSelected) Color.White else HarvestGreenDark
                                ),
                                border = BorderStroke(1.dp, HarvestGreen.copy(alpha = 0.4f))
                            )
                        }
                    }

                    Text("Initial Phenology Growth Stage", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(growthStages) { stage ->
                            val isSelected = state.initialGrowthStage == stage
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.onGrowthStageChange(stage) },
                                label = { Text(stage.displayName, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = state.notes,
                        onValueChange = viewModel::onNotesChange,
                        label = { Text("Agronomic Notes & Treatment Plan") },
                        placeholder = { Text("e.g. Drip fertigation active, zero stubble burning mandate") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Button(
                onClick = {
                    viewModel.saveCropCycle(onSuccess = {
                        onSaveSuccess()
                        onNavigateBack()
                    })
                },
                enabled = !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.Spa, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Cultivation Cycle", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

