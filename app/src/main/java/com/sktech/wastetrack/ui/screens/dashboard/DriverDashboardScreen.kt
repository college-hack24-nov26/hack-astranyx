package com.sktech.wastetrack.ui.screens.dashboard

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.sktech.wastetrack.domain.model.TransferStatus
import com.sktech.wastetrack.ui.theme.DarkTextSecondary
import com.sktech.wastetrack.ui.theme.GoldenWheatContainer
import com.sktech.wastetrack.ui.theme.GoldenWheatDark
import com.sktech.wastetrack.ui.theme.GoldenWheatLight
import com.sktech.wastetrack.ui.theme.HarvestGreen
import com.sktech.wastetrack.ui.theme.HarvestGreenContainer
import com.sktech.wastetrack.ui.theme.HarvestGreenDark
import com.sktech.wastetrack.ui.theme.HarvestGreenLight

private fun generateDriverQRBitmap(content: String, size: Int = 512): Bitmap {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    return bitmap
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverDashboardScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToTransfers: () -> Unit = {},
    onNavigateToTracking: (String) -> Unit = {},
    onNavigateToFleet: () -> Unit = {},
    onNavigateToQRScan: () -> Unit = {},
    onNavigateToWeighbridge: () -> Unit = {},
    onNavigateToQRGenerate: (String) -> Unit = {},
    viewModel: DriverDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Transporter Fleet Hub", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("${state.vehicleNumber} • ${state.driverName}", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToQRGenerate("tr_001") }) {
                        Icon(imageVector = Icons.Filled.QrCode, contentDescription = "Gatepass Pass QR", tint = HarvestGreen)
                    }
                    IconButton(onClick = onNavigateToQRScan) {
                        Icon(imageVector = Icons.Filled.QrCodeScanner, contentDescription = "Scan Gatepass", tint = HarvestGreen)
                    }
                    IconButton(onClick = onNavigateToWeighbridge) {
                        Icon(imageVector = Icons.Filled.Scale, contentDescription = "Weighbridge Slips", tint = HarvestGreen)
                    }
                    IconButton(onClick = onNavigateToFleet) {
                        Icon(imageVector = Icons.Filled.LocalShipping, contentDescription = "Fleet Operations", tint = HarvestGreen)
                    }
                    IconButton(onClick = onNavigateToTransfers) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ListAlt, contentDescription = "All Transfers", tint = HarvestGreen)
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = HarvestGreenContainer,
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(imageVector = Icons.Filled.LocalShipping, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(24.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = state.driverName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Text(text = state.vehicleNumber, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = HarvestGreen)
                                    Text(text = state.vehicleType, style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                                }
                            }

                            FilterChip(
                                selected = state.isOnline,
                                onClick = { viewModel.toggleDutyStatus() },
                                label = { Text(if (state.isOnline) "On Duty" else "Off Duty", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = HarvestGreenContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        DriverMetricCol("Active Trips", "${state.activeTrips.size} Loads")
                        VerticalDivider(modifier = Modifier.height(36.dp), color = HarvestGreen.copy(alpha = 0.2f))
                        DriverMetricCol("Total Hauled", "${state.totalTonnageHauledMt.toInt()} MT")
                        VerticalDivider(modifier = Modifier.height(36.dp), color = HarvestGreen.copy(alpha = 0.2f))
                        DriverMetricCol("Performance", "${state.onTimeRatingPercent}% On-Time")
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { onNavigateToQRGenerate("tr_001") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = HarvestGreenDark),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.QrCode, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Pass QR", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                    }
                    Button(
                        onClick = onNavigateToQRScan,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.QrCodeScanner, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Scan QR", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Button(
                        onClick = onNavigateToWeighbridge,
                        modifier = Modifier.weight(1.1f),
                        colors = ButtonDefaults.buttonColors(containerColor = HarvestGreenLight),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Scale, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Scale Slips", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                    }
                    OutlinedButton(
                        onClick = onNavigateToFleet,
                        modifier = Modifier.weight(0.9f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 10.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.LocalShipping, contentDescription = null, modifier = Modifier.size(15.dp), tint = HarvestGreen)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Fleet", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            item {
                Text(
                    text = "Current Assigned Dispatches",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(state.activeTrips, key = { it.id }) { trip ->
                DriverTripCard(
                    trip = trip,
                    onShowQR = { onNavigateToQRGenerate(trip.id) },
                    onAdvanceStatus = { viewModel.advanceTripStatus(trip.id) },
                    onReportIncident = { viewModel.openIncidentDialog(trip) },
                    onTrackRoute = { onNavigateToTracking(trip.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Recent Delivery Ledger",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(state.completedTrips, key = { it.id }) { trip ->
                CompletedTripCard(trip = trip)
            }
        }
    }

    if (state.selectedTripForQR != null) {
        val trip = state.selectedTripForQR!!
        val qrBitmap = remember(trip.qrPayload) {
            generateDriverQRBitmap(trip.qrPayload, 512)
        }

        AlertDialog(
            onDismissRequest = { viewModel.dismissQR() },
            title = {
                Text("Weighbridge Gatepass QR", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Gatepass QR",
                        modifier = Modifier
                            .size(210.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )

                    Text(
                        text = trip.gatePassNumber,
                        fontWeight = FontWeight.Bold,
                        color = HarvestGreen,
                        fontSize = 16.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = HarvestGreenContainer
                    ) {
                        Text(
                            text = "Fingerprint: ${trip.hashFingerprint}",
                            style = MaterialTheme.typography.labelSmall,
                            color = HarvestGreenDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = "${trip.cargoTitle} • ${trip.quantityMt} MT",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Present this digital code to weighbridge scanner at factory gate to automate inbound weighing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val tripId = trip.id
                            viewModel.dismissQR()
                            onNavigateToQRGenerate(tripId)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HarvestGreenDark)
                    ) {
                        Text("Dynamic Pass")
                    }
                    Button(
                        onClick = { viewModel.dismissQR() },
                        colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                    ) {
                        Text("Done")
                    }
                }
            }
        )
    }

    if (state.showIncidentDialog) {
        val incidentOptions = listOf("Traffic / Road Closure", "Mechanical Breakdown", "Weighbridge Queue Delay", "Weather Hazard")
        AlertDialog(
            onDismissRequest = { viewModel.closeIncidentDialog() },
            title = { Text("Report Transit Incident", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Incident Type:", style = MaterialTheme.typography.labelSmall)
                    incidentOptions.forEach { opt ->
                        FilterChip(
                            selected = state.incidentReasonInput == opt,
                            onClick = { viewModel.updateIncidentReason(opt) },
                            label = { Text(opt, fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.submitIncident() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Submit Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeIncidentDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun DriverMetricCol(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HarvestGreenDark)
    }
}

@Composable
private fun DriverTripCard(
    trip: DriverTrip,
    onShowQR: () -> Unit,
    onAdvanceStatus: () -> Unit,
    onReportIncident: () -> Unit,
    onTrackRoute: () -> Unit = {}
) {
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
                Text(text = trip.gatePassNumber, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = HarvestGreen)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (trip.status == TransferStatus.IN_TRANSIT) GoldenWheatContainer else HarvestGreenContainer
                ) {
                    Text(
                        text = trip.status.displayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (trip.status == TransferStatus.IN_TRANSIT) GoldenWheatDark else HarvestGreenDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = trip.cargoTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTrackRoute() }
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Place, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = trip.pickupLocation, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
                    Row(modifier = Modifier.padding(start = 6.dp)) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = DarkTextSecondary, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "${trip.distanceKm} km route", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Factory, contentDescription = null, tint = HarvestGreenDark, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = trip.dropLocation, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onShowQR,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Filled.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Show QR", fontSize = 12.sp)
                }

                Button(
                    onClick = onAdvanceStatus,
                    modifier = Modifier.weight(1.3f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                ) {
                    Text(
                        text = when (trip.status) {
                            TransferStatus.INITIATED -> "Start Transit"
                            TransferStatus.IN_TRANSIT -> "Arrived at Gate"
                            else -> "Verify Entry"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onReportIncident) {
                    Icon(imageVector = Icons.Filled.Warning, contentDescription = "Incident", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun CompletedTripCard(trip: DriverTrip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "${trip.cropName} • ${trip.quantityMt} MT", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "${trip.pickupLocation} → ${trip.dropLocation}", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                Text(text = trip.gatePassNumber, style = MaterialTheme.typography.labelSmall, color = HarvestGreen)
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = HarvestGreenContainer
            ) {
                Text(
                    text = "Delivered",
                    color = HarvestGreenDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
