package com.sktech.wastetrack.ui.screens.transfer

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sktech.wastetrack.domain.model.WeighbridgeSlip
import com.sktech.wastetrack.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeighbridgeSlipScreen(
    onNavigateBack: () -> Unit,
    onNavigateToScanner: () -> Unit = {},
    viewModel: WeighbridgeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val totalPayableKg = remember(state.slips) {
        state.slips.sumOf { it.netPayableBiomassKg }
    }
    val avgMoisture = remember(state.slips) {
        if (state.slips.isNotEmpty()) state.slips.map { it.moisturePercent }.average() else 11.0
    }

    val filteredSlips = remember(state.slips, state.selectedFilter) {
        when (state.selectedFilter) {
            "Acceptable" -> state.slips.filter { it.isMoistureAcceptable }
            "Penalized" -> state.slips.filter { !it.isMoistureAcceptable }
            else -> state.slips
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Weighbridge Scale Slips",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Dual Gross-Tare Verification & Moisture Audit",
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
                    IconButton(onClick = onNavigateToScanner) {
                        Icon(
                            imageVector = Icons.Filled.DocumentScanner,
                            contentDescription = "Scan Physical Slip",
                            tint = HarvestGreen
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
                onClick = { viewModel.openCreateDialog() },
                containerColor = HarvestGreen,
                contentColor = Color.White,
                icon = { Icon(Icons.Filled.Scale, contentDescription = null) },
                text = { Text("Log Scale Ticket", fontWeight = FontWeight.Bold) }
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
                    colors = CardDefaults.cardColors(containerColor = HarvestGreenDark),
                    border = BorderStroke(1.dp, HarvestGreenSoft.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Verified Net Biomass",
                                    color = HarvestGreenLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${String.format("%,.1f", totalPayableKg / 1000.0)} MT",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
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
                                        imageVector = Icons.Filled.Scale,
                                        contentDescription = null,
                                        tint = GoldenWheatLight,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = HarvestGreenSoft.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Scale Tickets", color = DarkTextSecondary, style = MaterialTheme.typography.bodySmall)
                                Text("${state.slips.size} Slips", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            }
                            Column {
                                Text("Avg Moisture", color = DarkTextSecondary, style = MaterialTheme.typography.bodySmall)
                                Text("${String.format("%.1f", avgMoisture)}%", fontWeight = FontWeight.Bold, color = GoldenWheatLight, fontSize = 14.sp)
                            }
                            Column {
                                Text("Moisture Threshold", color = DarkTextSecondary, style = MaterialTheme.typography.bodySmall)
                                Text("≤ 12.0% (Mandi Std)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val filters = listOf("All", "Acceptable", "Penalized")
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
                Text(
                    text = "Certified Scale Records (${filteredSlips.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(filteredSlips, key = { it.id }) { slip ->
                SlipCard(
                    slip = slip,
                    onViewTicket = { viewModel.selectSlipForPreview(slip) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (state.isCreateDialogOpen) {
        CreateSlipDialog(
            onDismiss = { viewModel.closeCreateDialog() },
            onSave = { gatePass, vehicle, driver, commodity, station, operator, gross, tare, moisture ->
                viewModel.recordScaleTicket(gatePass, vehicle, driver, commodity, station, operator, gross, tare, moisture)
            }
        )
    }

    state.selectedSlipForPreview?.let { previewSlip ->
        SlipPreviewDialog(
            slip = previewSlip,
            onDismiss = { viewModel.selectSlipForPreview(null) },
            onShare = {
                Toast.makeText(context, "Sharing Scale Ticket ${previewSlip.slipNumber}...", Toast.LENGTH_SHORT).show()
                viewModel.selectSlipForPreview(null)
            }
        )
    }
}

@Composable
private fun SlipCard(
    slip: WeighbridgeSlip,
    onViewTicket: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

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
                Column {
                    Text(
                        text = slip.slipNumber,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = slip.scaleStationName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (slip.isMoistureAcceptable) HarvestGreenContainer else DarkWarningContainer
                ) {
                    Text(
                        text = if (slip.isMoistureAcceptable) "MOISTURE OK" else "EXCESS MOISTURE",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (slip.isMoistureAcceptable) HarvestGreenDark else WarningOrangeLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Vehicle: ${slip.vehicleNumber} (${slip.driverName}) • ${slip.commodityName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    WeightColumn("Gross (GVW)", "${String.format("%,.0f", slip.grossWeightKg)} kg")
                    WeightColumn("Tare (Empty)", "${String.format("%,.0f", slip.tareWeightKg)} kg")
                    WeightColumn("Raw Net", "${String.format("%,.0f", slip.rawNetWeightKg)} kg")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Moisture: ${slip.moisturePercent}% ${if (slip.moistureDeductionKg > 0) "(-${String.format("%,.0f", slip.moistureDeductionKg)} kg penalty)" else "(Within 12% limit)"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (slip.isMoistureAcceptable) HarvestGreenDark else WarningOrange
                    )
                    Text(
                        text = "Net Dry Biomass: ${String.format("%,.1f", slip.netPayableBiomassKg / 1000.0)} MT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = HarvestGreenDark
                    )
                }

                Button(
                    onClick = onViewTicket,
                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Scale Ticket", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Gatepass: ${slip.gatePassNumber} • Logged: ${dateFormat.format(Date(slip.outboundTimeMillis))}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun WeightColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = DarkTextSecondary)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateSlipDialog(
    onDismiss: () -> Unit,
    onSave: (
        gatePass: String,
        vehicle: String,
        driver: String,
        commodity: String,
        station: String,
        operator: String,
        gross: Double,
        tare: Double,
        moisture: Double
    ) -> Unit
) {
    var gatePass by remember { mutableStateOf("GP-MH-2026-9142") }
    var vehicleNumber by remember { mutableStateOf("MH-15-EG-4921") }
    var driverName by remember { mutableStateOf("Sachin Shinde") }
    var commodityName by remember { mutableStateOf("Cotton Stalk Bales") }
    var stationName by remember { mutableStateOf("Sinnar MIDC Scale Terminal #2") }
    var operatorId by remember { mutableStateOf("OP-SN-41") }
    var grossText by remember { mutableStateOf("62400") }
    var tareText by remember { mutableStateOf("17600") }
    var moistureText by remember { mutableStateOf("10.5") }

    val gross = grossText.toDoubleOrNull() ?: 0.0
    val tare = tareText.toDoubleOrNull() ?: 0.0
    val moisture = moistureText.toDoubleOrNull() ?: 10.0
    val rawNet = (gross - tare).coerceAtLeast(0.0)
    val excess = (moisture - 12.0).coerceAtLeast(0.0)
    val deduction = if (excess > 0) (rawNet * (excess / 100.0)) else 0.0
    val netPayable = (rawNet - deduction).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Log Certified Scale Ticket", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = gatePass,
                    onValueChange = { gatePass = it },
                    label = { Text("Gatepass Reference") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = vehicleNumber,
                        onValueChange = { vehicleNumber = it },
                        label = { Text("Truck Reg No.") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = driverName,
                        onValueChange = { driverName = it },
                        label = { Text("Driver Name") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = commodityName,
                    onValueChange = { commodityName = it },
                    label = { Text("Commodity Residue") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = stationName,
                    onValueChange = { stationName = it },
                    label = { Text("Weighbridge Scale Station") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = grossText,
                        onValueChange = { grossText = it },
                        label = { Text("Gross GVW (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tareText,
                        onValueChange = { tareText = it },
                        label = { Text("Tare Empty (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = moistureText,
                    onValueChange = { moistureText = it },
                    label = { Text("Moisture Meter Reading (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HarvestGreenContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Raw Net Weight: ${String.format("%,.0f", rawNet)} kg", fontSize = 12.sp, color = HarvestGreenDark)
                        Text("Moisture Penalty: -${String.format("%,.0f", deduction)} kg (Threshold: 12%)", fontSize = 12.sp, color = if (excess > 0) WarningOrange else HarvestGreenDark)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Net Dry Payable Biomass: ${String.format("%,.1f", netPayable / 1000.0)} MT", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = HarvestGreenDark)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(gatePass, vehicleNumber, driverName, commodityName, stationName, operatorId, gross, tare, moisture)
                },
                colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
            ) {
                Text("Save Scale Record")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SlipPreviewDialog(
    slip: WeighbridgeSlip,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Industrial Weighbridge Scale Ticket", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(slip.slipNumber, style = MaterialTheme.typography.labelSmall, color = SkyBlueDark)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SkyBlueContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Weigh Station: ${slip.scaleStationName}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SkyBlueDark)
                        Text("Scale Operator: ${slip.operatorId} • Gatepass: ${slip.gatePassNumber}", fontSize = 11.sp, color = SkyBlueDark)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                PreviewSlipRow("Transport Vehicle", slip.vehicleNumber)
                PreviewSlipRow("Carrier Driver", slip.driverName)
                PreviewSlipRow("Commodity", slip.commodityName)

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                PreviewSlipRow("Gross Weight (Loaded)", "${String.format("%,.0f", slip.grossWeightKg)} kg")
                PreviewSlipRow("Tare Weight (Empty)", "${String.format("%,.0f", slip.tareWeightKg)} kg")
                PreviewSlipRow("Raw Net Weight", "${String.format("%,.0f", slip.rawNetWeightKg)} kg")
                PreviewSlipRow("Moisture Reading", "${slip.moisturePercent}%")
                PreviewSlipRow("Moisture Deduction", if (slip.moistureDeductionKg > 0) "-${String.format("%,.0f", slip.moistureDeductionKg)} kg" else "0 kg (Within Limit)")

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Net Dry Payable Biomass:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("${String.format("%,.1f", slip.netPayableBiomassKg / 1000.0)} MT (${String.format("%,.0f", slip.netPayableBiomassKg)} kg)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = HarvestGreenDark)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Digital Security Seal: ${slip.digitalSealHash}", fontSize = 10.sp, color = DarkTextSecondary, fontFamily = FontFamily.Monospace)
                Text("Certified Outbound: ${dateFormat.format(Date(slip.outboundTimeMillis))}", fontSize = 10.sp, color = DarkTextSecondary)
            }
        },
        confirmButton = {
            Button(
                onClick = onShare,
                colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
            ) {
                Icon(imageVector = Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share Ticket")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun PreviewSlipRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}
