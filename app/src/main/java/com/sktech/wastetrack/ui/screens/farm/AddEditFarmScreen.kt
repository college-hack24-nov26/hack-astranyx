package com.sktech.wastetrack.ui.screens.farm

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.sktech.wastetrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFarmScreen(
    onNavigateBack: () -> Unit,
    onSaveSuccess: (String) -> Unit = {},
    viewModel: AddEditFarmViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    val soilTypes = listOf(
        "Black Cotton (Regur)",
        "Alluvial Loam",
        "Red & Yellow",
        "Clayey Loam",
        "Sandy Loam",
        "Laterite"
    )

    val waterSources = listOf(
        "Drip Irrigation",
        "Canal Irrigation",
        "Well / Borewell",
        "Rain-fed Only"
    )

    val quickAcres = listOf("1.0", "2.5", "4.0", "8.0", "15.0")
    val crops = listOf("Cotton", "Sugarcane", "Soybean", "Wheat", "Paddy")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.farmId == null) "Register Cadastral Parcel" else "Edit Land Parcel",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "1. Maharashtra 7/12 Land Record Cadastre",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = HarvestGreenDark
                    )

                    OutlinedTextField(
                        value = state.parcelName,
                        onValueChange = viewModel::onParcelNameChange,
                        label = { Text("Parcel / Plot Title") },
                        placeholder = { Text("e.g. Patil Bio-Parcel C") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = state.extractNumber712,
                            onValueChange = viewModel::onExtractNumberChange,
                            label = { Text("7/12 Extract No.") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = state.gutNumber,
                            onValueChange = viewModel::onGutNumberChange,
                            label = { Text("Gut / Survey No.") },
                            placeholder = { Text("Gut 112/A") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
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
                        text = "2. Land Acreage & Biomass Projection",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = HarvestGreenDark
                    )

                    Text("Quick Acreage Select", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickAcres.forEach { acre ->
                            val isSelected = state.plotAreaAcresText == acre
                            SuggestionChip(
                                onClick = { viewModel.onAreaChange(acre) },
                                label = { Text("$acre Ac", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (isSelected) HarvestGreen else HarvestGreen.copy(alpha = 0.08f),
                                    labelColor = if (isSelected) Color.White else HarvestGreenDark
                                ),
                                border = BorderStroke(1.dp, HarvestGreen.copy(alpha = 0.4f))
                            )
                        }
                    }

                    OutlinedTextField(
                        value = state.plotAreaAcresText,
                        onValueChange = viewModel::onAreaChange,
                        label = { Text("Total Plot Area (Acres)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Text("Active Crop Cultivated", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(crops) { crop ->
                            val isSelected = state.activeCropName.equals(crop, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.onActiveCropChange(crop) },
                                label = { Text(crop, fontSize = 11.sp) }
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GoldenWheatContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Est. Harvest Biomass Residue", style = MaterialTheme.typography.bodySmall, color = GoldenWheatDark)
                            Text("${state.projectedResidueMtText} MT", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = GoldenWheatDark)
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
                        text = "3. Soil & Irrigation Infrastructure",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = HarvestGreenDark
                    )

                    Text("Soil Classification Type", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(soilTypes) { soil ->
                            FilterChip(
                                selected = state.selectedSoilType == soil,
                                onClick = { viewModel.onSoilTypeChange(soil) },
                                label = { Text(soil, fontSize = 11.sp) }
                            )
                        }
                    }

                    Text("Water Source / Irrigation", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(waterSources) { water ->
                            FilterChip(
                                selected = state.selectedWaterSource == water,
                                onClick = { viewModel.onWaterSourceChange(water) },
                                label = { Text(water, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, HarvestGreen.copy(alpha = 0.4f))
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
                        Column {
                            Text("Laboratory Soil Health Card (N-P-K)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Attach chemical test profile", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = state.hasSoilTest,
                            onCheckedChange = viewModel::toggleSoilTest
                        )
                    }

                    if (state.hasSoilTest) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = state.soilPhText,
                                onValueChange = viewModel::onSoilPhChange,
                                label = { Text("pH Level") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = state.nitrogenText,
                                onValueChange = viewModel::onNitrogenChange,
                                label = { Text("Nitrogen (N)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = state.phosphorusText,
                                onValueChange = viewModel::onPhosphorusChange,
                                label = { Text("Phosphorus (P)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = state.potassiumText,
                                onValueChange = viewModel::onPotassiumChange,
                                label = { Text("Potassium (K)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.saveParcel(onSuccess = { parcelId ->
                        onSaveSuccess(parcelId)
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
                    Icon(Icons.Filled.Save, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Register Cadastral Parcel", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

