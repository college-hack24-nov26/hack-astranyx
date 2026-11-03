package com.sktech.wastetrack.ui.screens.transfer

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.sktech.wastetrack.domain.model.TransferStatus
import com.sktech.wastetrack.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun generateTrackingQRBitmap(content: String, size: Int = 512): Bitmap {
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
fun ShipmentTrackingScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: ShipmentTrackingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Live Route Telemetry", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("${state.gatePassNumber} ? ${state.vehicleNumber}", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleSatelliteView() }) {
                        Icon(
                            imageVector = if (state.isSatelliteView) Icons.Filled.Layers else Icons.Filled.Map,
                            contentDescription = "Map Style",
                            tint = HarvestGreen
                        )
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
                TelemetryMapCard(
                    state = state,
                    onCallDriver = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${state.driverPhone}")
                        }
                        context.startActivity(intent)
                    }
                )
            }

            item {
                TripMetricsRow(state = state)
            }

            item {
                DriverConsigneeCard(state = state, onCallDriver = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${state.driverPhone}")
                    }
                    context.startActivity(intent)
                })
            }

            item {
                DualWeighbridgeCard(
                    slip = state.weighbridgeSlip,
                    onOpenEntryDialog = { viewModel.openWeighbridgeDialog() }
                )
            }

            item {
                TransitMilestoneTimeline(
                    waypoints = state.waypoints,
                    currentStatus = state.status
                )
            }

            item {
                ActionControlBar(
                    status = state.status,
                    onShowQR = { viewModel.openQrDialog() },
                    onAdvanceStatus = { viewModel.advanceTransitMilestone() },
                    onReportIncident = { viewModel.openIncidentDialog() }
                )
            }
        }
    }

    if (state.showQrDialog) {
        val qrBitmap = remember(state.qrPayload) { generateTrackingQRBitmap(state.qrPayload) }
        AlertDialog(
            onDismissRequest = { viewModel.dismissQrDialog() },
            title = {
                Text(
                    text = "Waybill Digital Handshake",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "GatePass QR",
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = state.gatePassNumber,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = HarvestGreen
                    )
                    Text(
                        text = "Hash: ${state.hashFingerprint}",
                        style = MaterialTheme.typography.labelSmall,
                        color = DarkTextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissQrDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                ) {
                    Text("Close Pass")
                }
            }
        )
    }

    if (state.showWeighbridgeDialog) {
        var grossInput by remember { mutableStateOf(state.weighbridgeSlip?.grossWeightMt?.toString() ?: "62.4") }
        var tareInput by remember { mutableStateOf(state.weighbridgeSlip?.tareWeightMt?.toString() ?: "17.6") }

        AlertDialog(
            onDismissRequest = { viewModel.dismissWeighbridgeDialog() },
            title = {
                Text("Destination Weighbridge Entry", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Enter Gross (laden) and Tare (empty) weights verified at plant weighbridge terminal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary
                    )
                    OutlinedTextField(
                        value = grossInput,
                        onValueChange = { grossInput = it },
                        label = { Text("Gross Weight (MT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tareInput,
                        onValueChange = { tareInput = it },
                        label = { Text("Tare Weight (MT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    val g = grossInput.toDoubleOrNull() ?: 0.0
                    val t = tareInput.toDoubleOrNull() ?: 0.0
                    val net = (g - t).coerceAtLeast(0.0)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = HarvestGreenLight.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Calculated Net Weight:", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            Text(String.format(Locale.US, "%.2f MT", net), fontWeight = FontWeight.Bold, color = HarvestGreenDark, fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val g = grossInput.toDoubleOrNull() ?: 62.4
                        val t = tareInput.toDoubleOrNull() ?: 17.6
                        viewModel.submitWeighbridgeEntry(g, t)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                ) {
                    Text("Certify Weighment")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissWeighbridgeDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (state.showIncidentDialog) {
        val incidentOptions = listOf("Vehicle Breakdown / Puncture", "Severe Weather / Route Blockage", "Tollway Highway Delay", "Biomass Quality Discrepancy")
        AlertDialog(
            onDismissRequest = { viewModel.dismissIncidentDialog() },
            title = {
                Text("Report Transit Incident", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select reason to alert central agro-fleet dispatch:", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                    incidentOptions.forEach { opt ->
                        FilterChip(
                            selected = state.incidentReason == opt,
                            onClick = { viewModel.setIncidentReason(opt) },
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
                    Text("Broadcast Alert")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissIncidentDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TelemetryMapCard(
    state: ShipmentTrackingUiState,
    onCallDriver: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .background(if (state.isSatelliteView) Color(0xFF1E2620) else Color(0xFF2C3E50))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    val startPoint = Offset(w * 0.15f, h * 0.75f)
                    val midPoint = Offset(w * 0.50f, h * 0.45f)
                    val endPoint = Offset(w * 0.85f, h * 0.25f)

                    drawLine(
                        color = Color.White.copy(alpha = 0.25f),
                        start = startPoint,
                        end = endPoint,
                        strokeWidth = 6f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                    )

                    drawLine(
                        color = Color(0xFF2ECC71),
                        start = startPoint,
                        end = midPoint,
                        strokeWidth = 8f
                    )

                    drawCircle(color = Color(0xFF27AE60), radius = 12f, center = startPoint)
                    drawCircle(color = Color.White, radius = 6f, center = startPoint)

                    drawCircle(color = Color(0xFF2ECC71).copy(alpha = 0.35f), radius = 24f, center = midPoint)
                    drawCircle(color = Color(0xFF2ECC71), radius = 14f, center = midPoint)
                    drawCircle(color = Color.White, radius = 7f, center = midPoint)

                    drawCircle(color = Color(0xFFE74C3C), radius = 12f, center = endPoint)
                    drawCircle(color = Color.White, radius = 6f, center = endPoint)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF2ECC71), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "GPS FIXED ? ${state.speedKmph} KM/H",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = "ETA: ${state.etaMinutes} MINS",
                            color = Color(0xFFF1C40F),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Filled.NearMe, contentDescription = null, tint = Color(0xFF2ECC71), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${state.distanceRemainingKm} km to destination plant",
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Current Transit Corridors", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("${state.sourceLocation}  ?  ${state.destinationPlant}", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                    }
                }

                LinearProgressIndicator(
                    progress = { state.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = HarvestGreen,
                    trackColor = HarvestGreenLight.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
private fun TripMetricsRow(state: ShipmentTrackingUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricTile(
            title = "Remaining",
            value = "${state.distanceRemainingKm} km",
            icon = Icons.Filled.Navigation,
            modifier = Modifier.weight(1f)
        )
        MetricTile(
            title = "Est. Arrival",
            value = "${state.etaMinutes} min",
            icon = Icons.Filled.Timer,
            modifier = Modifier.weight(1f)
        )
        MetricTile(
            title = "Cargo Qty",
            value = "${state.quantityMt} MT",
            icon = Icons.Filled.Scale,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricTile(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(18.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DriverConsigneeCard(
    state: ShipmentTrackingUiState,
    onCallDriver: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Filled.LocalShipping, contentDescription = null, tint = HarvestGreenDark)
                    }
                }
                Column {
                    Text(state.driverName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("${state.vehicleNumber} ? ${state.vehicleType}", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                }
            }

            FilledIconButton(
                onClick = onCallDriver,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = HarvestGreen)
            ) {
                Icon(imageVector = Icons.Filled.Phone, contentDescription = "Call", tint = Color.White)
            }
        }
    }
}

@Composable
private fun DualWeighbridgeCard(
    slip: WeighbridgeSlip?,
    onOpenEntryDialog: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Filled.Scale, contentDescription = null, tint = HarvestGreenDark)
                    Text("Dual Weighbridge Verification", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Surface(
                    color = if (slip?.isVerified == true) HarvestGreenLight.copy(alpha = 0.3f) else Color(0xFFFDEED9),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (slip?.isVerified == true) "CERTIFIED" else "PENDING",
                        color = if (slip?.isVerified == true) HarvestGreenDark else Color(0xFFD35400),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            if (slip != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Gross (Laden)", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                        Text("${slip.grossWeightMt} MT", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Column {
                        Text("Tare (Empty)", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                        Text("${slip.tareWeightMt} MT", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Column {
                        Text("Net Delivered", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                        Text("${slip.netWeightMt} MT", fontWeight = FontWeight.Bold, color = HarvestGreenDark, fontSize = 14.sp)
                    }
                    Column {
                        Text("Variance", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                        Text(String.format(Locale.US, "%.1f%%", slip.variancePercent), fontWeight = FontWeight.Bold, color = if (slip.variancePercent < 1.0) HarvestGreen else Color(0xFFE74C3C), fontSize = 14.sp)
                    }
                }
            }

            OutlinedButton(
                onClick = onOpenEntryDialog,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Filled.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Recalibrate Weighbridge Entry", fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun TransitMilestoneTimeline(
    waypoints: List<WaypointPoint>,
    currentStatus: TransferStatus
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Waybill Checkpoint Milestones", fontWeight = FontWeight.Bold, fontSize = 15.sp)

            waypoints.forEachIndexed { index, wp ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = when {
                                wp.isCompleted -> HarvestGreen
                                wp.isCurrent -> Color(0xFFF39C12)
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (wp.isCompleted) {
                                    Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                } else {
                                    Text("${index + 1}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        if (index < waypoints.size - 1) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(28.dp)
                                    .background(if (wp.isCompleted) HarvestGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = wp.title,
                            fontWeight = if (wp.isCurrent) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp,
                            color = if (wp.isCurrent) HarvestGreenDark else MaterialTheme.colorScheme.onSurface
                        )
                        Text(text = wp.subtitle, style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionControlBar(
    status: TransferStatus,
    onShowQR: () -> Unit,
    onAdvanceStatus: () -> Unit,
    onReportIncident: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onShowQR,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(imageVector = Icons.Filled.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("QR Pass", fontSize = 12.sp)
        }

        Button(
            onClick = onAdvanceStatus,
            modifier = Modifier.weight(1.3f),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
        ) {
            Text(
                text = when (status) {
                    TransferStatus.INITIATED -> "Dispatch Transit"
                    TransferStatus.IN_TRANSIT -> "Plant Gate In"
                    TransferStatus.DELIVERED -> "Verify Scales"
                    TransferStatus.VERIFIED -> "Waybill Verified"
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
