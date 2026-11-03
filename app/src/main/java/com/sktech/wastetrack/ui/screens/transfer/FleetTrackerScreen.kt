package com.sktech.wastetrack.ui.screens.transfer

import com.sktech.wastetrack.ui.components.IndustrialFleetMap

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sktech.wastetrack.domain.model.TransferStatus
import com.sktech.wastetrack.domain.model.VehicleType
import com.sktech.wastetrack.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetTrackerScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToTracking: (String) -> Unit = {},
    viewModel: FleetTrackerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val filterTabs = listOf("All", "In Transit", "Delivered", "Available at Hub")

    val filteredCarriers = remember(state.carriers, state.selectedFilter) {
        when (state.selectedFilter) {
            "In Transit" -> state.carriers.filter { it.status == TransferStatus.IN_TRANSIT || it.status == TransferStatus.INITIATED }
            "Delivered" -> state.carriers.filter { it.status == TransferStatus.DELIVERED || it.status == TransferStatus.VERIFIED }
            "Available at Hub" -> state.carriers.filter { it.isAvailable }
            else -> state.carriers
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Transporter Fleet Operations", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("${state.carriers.size} Registered Carriers ? Central Dispatch", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.openDispatchModal() }) {
                        Icon(imageVector = Icons.Filled.AddRoad, contentDescription = "Dispatch Truck", tint = HarvestGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openDispatchModal() },
                containerColor = HarvestGreen,
                contentColor = Color.White,
                icon = { Icon(Icons.Filled.LocalShipping, contentDescription = null) },
                text = { Text("Dispatch Truck", fontWeight = FontWeight.Bold) }
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
                FleetOperationsRadarCard(
                    activeCount = state.activeCorridorCount,
                    totalTonnage = state.totalTonnageInTransit
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filterTabs) { tab ->
                        val isSelected = state.selectedFilter == tab
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectFilter(tab) },
                            label = { Text(tab, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HarvestGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            items(filteredCarriers, key = { it.id }) { carrier ->
                CarrierFleetCard(
                    carrier = carrier,
                    onCallDriver = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${carrier.driverPhone}")
                        }
                        context.startActivity(intent)
                    },
                    onTrackTelemetry = { onNavigateToTracking(carrier.id) }
                )
            }
        }
    }

    if (state.showDispatchModal) {
        var vehicleNo by remember { mutableStateOf("") }
        var driverName by remember { mutableStateOf("") }
        var driverPhone by remember { mutableStateOf("") }
        var selectedType by remember { mutableStateOf(VehicleType.BIG_TRUCK) }
        var destination by remember { mutableStateOf("") }
        var cargo by remember { mutableStateOf("") }
        var tonnageText by remember { mutableStateOf("25.0") }

        AlertDialog(
            onDismissRequest = { viewModel.dismissDispatchModal() },
            title = {
                Text("Dispatch Fleet Carrier", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = vehicleNo,
                        onValueChange = { vehicleNo = it },
                        label = { Text("Vehicle Registration (e.g. MH-15-EG-4921)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = driverName,
                        onValueChange = { driverName = it },
                        label = { Text("Driver Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = driverPhone,
                        onValueChange = { driverPhone = it },
                        label = { Text("Driver Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = destination,
                        onValueChange = { destination = it },
                        label = { Text("Destination Bio-Energy Facility") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cargo,
                            onValueChange = { cargo = it },
                            label = { Text("Cargo (e.g. Cotton Stalks)") },
                            modifier = Modifier.weight(1.5f)
                        )
                        OutlinedTextField(
                            value = tonnageText,
                            onValueChange = { tonnageText = it },
                            label = { Text("Tonnage (MT)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val t = tonnageText.toDoubleOrNull() ?: 25.0
                        viewModel.confirmTruckDispatch(
                            vehicleNo = vehicleNo,
                            driverName = driverName,
                            driverPhone = driverPhone,
                            vehicleType = selectedType,
                            destination = destination,
                            cargo = cargo,
                            tonnage = t
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                ) {
                    Text("Issue Outbound GatePass")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDispatchModal() }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (state.showDispatchedToast) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissToast() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null, tint = HarvestGreen)
                    Text("Carrier Dispatched", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text("Carrier dispatch logged in the central transit manifest. GPS tracking & e-way bill telemetry active.")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissToast() },
                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                ) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
private fun FleetOperationsRadarCard(
    activeCount: Int,
    totalTonnage: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkHarvestGreenContainer),
        border = BorderStroke(1.dp, HarvestGreenSoft.copy(alpha = 0.3f))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            ) {
                IndustrialFleetMap(
                    vehicleNumber = "MH-15-EG-4921",
                    driverName = "Suresh Khairnar",
                    driverPhone = "+91 98224 81920",
                    originName = "Sinnar Agro Hub",
                    destinationName = "Igatpuri MIDC Plant",
                    originLat = 19.8500,
                    originLon = 73.9900,
                    destLat = 19.6936,
                    destLon = 73.5558,
                    speedKmh = 52,
                    progressFraction = 0.58f,
                    modifier = Modifier.fillMaxSize(),
                    isInteractive = true,
                    showDriverCard = false
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Active Corridors", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    Text("$activeCount Routes Live", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Payload in Transit", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    Text("${String.format(Locale.US, "%,.0f", totalTonnage)} MT Biomass", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = GoldenWheatLight)
                }
            }
        }
    }
}

@Composable
private fun CarrierFleetCard(
    carrier: FleetCarrier,
    onCallDriver: () -> Unit,
    onTrackTelemetry: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = HarvestGreenLight.copy(alpha = 0.3f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Filled.LocalShipping, contentDescription = null, tint = HarvestGreenDark, modifier = Modifier.size(20.dp))
                        }
                    }
                    Column {
                        Text(carrier.vehicleNumber, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(carrier.vehicleType.categoryName, style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    }
                }

                Surface(
                    color = when (carrier.status) {
                        TransferStatus.IN_TRANSIT -> GoldenWheatContainer
                        TransferStatus.DELIVERED -> HarvestGreenContainer
                        TransferStatus.VERIFIED -> Color(0xFFE8F8F5)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (carrier.isAvailable) "Available at Hub" else carrier.status.displayName,
                        color = when (carrier.status) {
                            TransferStatus.IN_TRANSIT -> GoldenWheatDark
                            TransferStatus.DELIVERED -> HarvestGreenDark
                            TransferStatus.VERIFIED -> Color(0xFF117A65)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Route, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(carrier.currentCorridor, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Factory, contentDescription = null, tint = DarkTextSecondary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(carrier.destinationPlant, style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Filled.Person, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(16.dp))
                    Text(carrier.driverName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "Fuel ${carrier.fuelLevelPercent}%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkTextSecondary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    IconButton(onClick = onCallDriver, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Filled.Phone, contentDescription = "Call", tint = HarvestGreen, modifier = Modifier.size(18.dp))
                    }
                }
            }

            if (!carrier.isAvailable) {
                Button(
                    onClick = onTrackTelemetry,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                ) {
                    Icon(imageVector = Icons.Filled.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Track Live Route & Telemetry", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
