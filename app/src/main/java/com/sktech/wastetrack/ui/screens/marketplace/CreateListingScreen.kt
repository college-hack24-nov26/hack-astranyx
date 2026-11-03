package com.sktech.wastetrack.ui.screens.marketplace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sktech.wastetrack.ui.theme.*
import com.sktech.wastetrack.util.LocationHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListingScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToMarket: () -> Unit = {},
    viewModel: CreateListingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isDetectingGps by remember { mutableStateOf(false) }

    val cropTypes = listOf("Cotton", "Sugarcane", "Soybean", "Wheat", "Paddy")
    val moistureGrades = listOf("Grade A (<14%)", "Grade B (15-20%)", "Standard Moisture")
    val storageTypes = listOf("Baled & Dry Warehouse", "Fieldside Tarpaulin Cover", "Loose in Open Yard")

    val quantityMt = state.quantityMtText.toDoubleOrNull() ?: 0.0
    val pricePerTon = state.askingPricePerTonText.toDoubleOrNull() ?: 0.0
    val estimatedTotalPayout = quantityMt * pricePerTon

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("List Crop Residue", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Publish harvest biomass for industrial buyers", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Crop Residue Type", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(cropTypes) { crop ->
                            val isSelected = state.selectedCrop == crop
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateCrop(crop) },
                                label = { Text(crop, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = HarvestGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = state.residueTitle,
                        onValueChange = { viewModel.updateResidueTitle(it) },
                        label = { Text("Residue Lot Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Available Biomass Volume", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.quantityMtText,
                        onValueChange = { viewModel.updateQuantity(it) },
                        label = { Text("Quantity in Metric Tons (MT)") },
                        leadingIcon = { Icon(Icons.Filled.Scale, contentDescription = null, tint = HarvestGreen) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("10", "25", "50", "100").forEach { quickQty ->
                            OutlinedButton(
                                onClick = { viewModel.updateQuantity(quickQty) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                Text("$quickQty MT", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Moisture Quality Grade", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(moistureGrades) { grade ->
                            val isSelected = state.moistureGrade == grade
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateMoistureGrade(grade) },
                                label = { Text(grade, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }

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
                        Text("Pricing & Valuation", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = GoldenWheatContainer
                        ) {
                            Text(
                                text = "Mandi Benchmark: ₹${state.benchmarkPricePerTon.toInt()}/MT",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = GoldenWheatDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.askingPricePerTonText,
                        onValueChange = { viewModel.updateAskingPrice(it) },
                        label = { Text("Asking Price (₹ / Metric Ton)") },
                        leadingIcon = { Icon(Icons.Filled.AttachMoney, contentDescription = null, tint = HarvestGreen) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = HarvestGreenContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Estimated Lot Value", style = MaterialTheme.typography.labelSmall, color = HarvestGreenDark)
                                Text(
                                    text = "₹${String.format("%,.0f", estimatedTotalPayout)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = HarvestGreenDark
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Energy Yield", style = MaterialTheme.typography.labelSmall, color = HarvestGreenDark)
                                Text(
                                    text = "${state.calorificValueMjKg} MJ/kg",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = HarvestGreenDark
                                )
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Logistics & Storage Details", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.villageAddress,
                        onValueChange = { viewModel.updateVillageAddress(it) },
                        label = { Text("Pickup Location / Farm Village") },
                        leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null, tint = HarvestGreen) },
                        trailingIcon = {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    isDetectingGps = true
                                    val loc = LocationHelper.getCurrentLocation(context)
                                    if (loc != null) {
                                        val formatted = LocationHelper.getFormattedLocationName(context, loc.latitude, loc.longitude)
                                        if (formatted != null) {
                                            viewModel.updateVillageAddress(formatted)
                                        }
                                    }
                                    isDetectingGps = false
                                }
                            }) {
                                Icon(Icons.Filled.MyLocation, contentDescription = "Auto GPS", tint = HarvestGreen)
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Storage Type:", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(storageTypes) { st ->
                            val isSelected = state.storageCondition == st
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateStorageCondition(st) },
                                label = { Text(st, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.additionalNotes,
                        onValueChange = { viewModel.updateNotes(it) },
                        label = { Text("Logistics Notes for Truck Dispatch") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Button(
                onClick = { viewModel.publishListing() },
                enabled = !state.isPublishing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
            ) {
                if (state.isPublishing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(imageVector = Icons.Filled.Publish, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Publish Lot to Marketplace", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (state.isPublishedSuccess) {
        AlertDialog(
            onDismissRequest = { viewModel.resetPublishState() },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = HarvestGreenContainer,
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(32.dp))
                    }
                }
            },
            title = {
                Text("Residue Lot Published!", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Your lot of ${state.quantityMtText} MT ${state.selectedCrop} residue is now visible to industrial buyers, biomass plants, and recyclers on the marketplace.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Asking Rate: ₹${state.askingPricePerTonText} / Ton • Lot ID: ${state.createdListingId}",
                        fontWeight = FontWeight.SemiBold,
                        color = HarvestGreenDark,
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetPublishState()
                        onNavigateToMarket()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                ) {
                    Text("View in Marketplace", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.resetPublishState()
                    onNavigateBack()
                }) {
                    Text("Done")
                }
            }
        )
    }
}
