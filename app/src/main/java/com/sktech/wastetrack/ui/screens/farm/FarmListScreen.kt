package com.sktech.wastetrack.ui.screens.farm

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.sktech.wastetrack.domain.model.Farm
import com.sktech.wastetrack.domain.model.SoilType
import com.sktech.wastetrack.domain.model.WaterSource
import com.sktech.wastetrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmListScreen(
    onNavigateBack: () -> Unit,
    viewModel: FarmViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val totalAcres = remember(state.farms) {
        state.farms.sumOf { it.plotAreaAcres }
    }
    val totalResidue = remember(state.farms) {
        state.farms.sumOf { it.projectedResidueMt }
    }

    val filteredFarms = remember(state.farms, state.selectedFilter) {
        when (state.selectedFilter) {
            "Drip" -> state.farms.filter { it.waterSource == WaterSource.DRIP_IRRIGATION }
            "Canal" -> state.farms.filter { it.waterSource == WaterSource.CANAL_IRRIGATION }
            "Borewell" -> state.farms.filter { it.waterSource == WaterSource.BOREWELL }
            "Rain-fed" -> state.farms.filter { it.waterSource == WaterSource.RAIN_FED }
            else -> state.farms
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Farmland Parcel Registry",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "7/12 Land Records & Survey Boundaries",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                containerColor = HarvestGreen,
                contentColor = Color.White,
                icon = { Icon(Icons.Filled.AddLocationAlt, contentDescription = null) },
                text = { Text("Register Parcel", fontWeight = FontWeight.Bold) }
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
                    colors = CardDefaults.cardColors(containerColor = HarvestGreen)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Agricultural Land Holding",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "${String.format("%.1f", totalAcres)} Total Acres",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = Color.White
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Landscape,
                                        contentDescription = null,
                                        tint = GoldenWheatLight,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Registered Plots",
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "${state.farms.size} Parcels",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                            Column {
                                Text(
                                    text = "Projected Biomass",
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "${String.format("%.1f", totalResidue)} MT",
                                    fontWeight = FontWeight.Bold,
                                    color = GoldenWheatLight,
                                    fontSize = 14.sp
                                )
                            }
                            Column {
                                Text(
                                    text = "Region",
                                    color = Color.White.copy(alpha = 0.8f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Nashik District",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val filters = listOf("All", "Drip", "Canal", "Borewell", "Rain-fed")
                    items(filters) { filter ->
                        FilterChip(
                            selected = state.selectedFilter == filter,
                            onClick = { viewModel.setFilter(filter) },
                            label = { Text(filter) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HarvestGreen,
                                selectedLabelColor = Color.White
                            )
                        )
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
                        text = "Land Holdings (${filteredFarms.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            items(filteredFarms, key = { it.id }) { farm ->
                FarmCard(
                    farm = farm,
                    onDelete = { viewModel.deleteFarm(farm.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (state.isAddDialogOpen) {
        AddFarmDialog(
            onDismiss = { viewModel.closeAddDialog() },
            onSave = { name, survey, acres, soil, water, village, crop, residue ->
                viewModel.addFarm(name, survey, acres, soil, water, village, crop, residue)
            }
        )
    }
}

@Composable
private fun FarmCard(
    farm: Farm,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, HarvestGreen.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = farm.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${farm.village}, ${farm.taluka} • ${farm.district}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SkyBlueContainer,
                    border = BorderStroke(1.dp, SkyBlue.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = farm.surveyNumber,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SkyBlueDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = HarvestGreenContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Terrain,
                            contentDescription = null,
                            tint = HarvestGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${farm.plotAreaAcres} ac",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = HarvestGreenDark
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GoldenWheatContainer
                ) {
                    Text(
                        text = farm.soilType.label,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = GoldenWheatDark
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = farm.waterSource.label,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Eco,
                        contentDescription = null,
                        tint = HarvestGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${farm.activeCropName} • Est: ${farm.projectedResidueMt} MT Residue",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFarmDialog(
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        survey: String,
        acres: Double,
        soil: SoilType,
        water: WaterSource,
        village: String,
        crop: String,
        residue: Double
    ) -> Unit
) {
    var plotName by remember { mutableStateOf("") }
    var surveyNo by remember { mutableStateOf("") }
    var acresText by remember { mutableStateOf("") }
    var villageName by remember { mutableStateOf("") }
    var activeCrop by remember { mutableStateOf("") }
    var residueText by remember { mutableStateOf("") }
    var selectedSoil by remember { mutableStateOf(SoilType.BLACK_COTTON) }
    var selectedWater by remember { mutableStateOf(WaterSource.DRIP_IRRIGATION) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Register Farmland Parcel", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = plotName,
                    onValueChange = { plotName = it },
                    label = { Text("Plot Name (e.g. Shivar East)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = surveyNo,
                    onValueChange = { surveyNo = it },
                    label = { Text("7/12 Gat No. / Survey No.") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = acresText,
                        onValueChange = { acresText = it },
                        label = { Text("Area (Acres)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = residueText,
                        onValueChange = { residueText = it },
                        label = { Text("Residue (MT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = villageName,
                    onValueChange = { villageName = it },
                    label = { Text("Village / Taluka") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = activeCrop,
                    onValueChange = { activeCrop = it },
                    label = { Text("Standing Crop (e.g. Cotton, Grapes)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text(
                    text = "Soil Type",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(SoilType.values()) { soil ->
                        FilterChip(
                            selected = selectedSoil == soil,
                            onClick = { selectedSoil = soil },
                            label = { Text(soil.label, fontSize = 11.sp) }
                        )
                    }
                }

                Text(
                    text = "Water Source",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(WaterSource.values()) { water ->
                        FilterChip(
                            selected = selectedWater == water,
                            onClick = { selectedWater = water },
                            label = { Text(water.label, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val acres = acresText.toDoubleOrNull() ?: 1.0
                    val residue = residueText.toDoubleOrNull() ?: (acres * 3.5)
                    onSave(
                        plotName,
                        surveyNo,
                        acres,
                        selectedSoil,
                        selectedWater,
                        villageName,
                        activeCrop,
                        residue
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
            ) {
                Text("Save Parcel")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

