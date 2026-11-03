package com.sktech.wastetrack.ui.screens.transfer

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.sktech.wastetrack.domain.model.TransferItem
import com.sktech.wastetrack.domain.model.TransferStatus
import com.sktech.wastetrack.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun generateQRBitmap(content: String, size: Int = 512): Bitmap {
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
fun TransferScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: TransferViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val filterTabs = listOf("All", "In Transit", "Verified", "Dispatched")

    val filteredTransfers = remember(state.transfers, state.selectedFilter) {
        when (state.selectedFilter) {
            "In Transit" -> state.transfers.filter { it.status == TransferStatus.IN_TRANSIT }
            "Verified" -> state.transfers.filter { it.status == TransferStatus.VERIFIED }
            "Dispatched" -> state.transfers.filter { it.status == TransferStatus.INITIATED || it.status == TransferStatus.DELIVERED }
            else -> state.transfers
        }
    }

    val totalDispatchedMt = state.transfers.sumOf { it.quantityMt }
    val activeTransitCount = state.transfers.count { it.status == TransferStatus.IN_TRANSIT }
    val verifiedCount = state.transfers.count { it.status == TransferStatus.VERIFIED }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Chain-of-Custody & Gatepass", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Digital logistics ledger & weighbridge verification", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
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
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = HarvestGreenContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    TransferMetricCol("In Transit", "$activeTransitCount Trucks")
                    VerticalDivider(modifier = Modifier.height(36.dp), color = HarvestGreen.copy(alpha = 0.2f))
                    TransferMetricCol("Dispatched", String.format("%.0f MT", totalDispatchedMt))
                    VerticalDivider(modifier = Modifier.height(36.dp), color = HarvestGreen.copy(alpha = 0.2f))
                    TransferMetricCol("Verified", "$verifiedCount Receipts")
                }
            }

            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterTabs) { tab ->
                    val isSelected = state.selectedFilter == tab
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setFilter(tab) },
                        label = { Text(tab, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HarvestGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredTransfers) { item ->
                    TransferCard(
                        item = item,
                        onShowQR = { viewModel.showGatepassQR(item) },
                        onConfirmReceipt = { viewModel.confirmWeighbridgeReceipt(item.id) }
                    )
                }
            }
        }
    }

    if (state.selectedTransferForQR != null) {
        val transfer = state.selectedTransferForQR!!
        val qrBitmap = remember(transfer.qrPayload) {
            generateQRBitmap(transfer.qrPayload, 512)
        }

        AlertDialog(
            onDismissRequest = { viewModel.dismissQR() },
            title = {
                Text("Digital Gatepass Handshake", fontWeight = FontWeight.Bold, fontSize = 17.sp)
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
                            .size(200.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )

                    Text(
                        text = transfer.gatePassNumber,
                        fontWeight = FontWeight.Bold,
                        color = HarvestGreen,
                        fontSize = 15.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = HarvestGreenContainer
                    ) {
                        Text(
                            text = "SHA-256: ${transfer.digitalHashSignature}",
                            style = MaterialTheme.typography.labelSmall,
                            color = HarvestGreenDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = "Vehicle: ${transfer.vehicleNumber} • Driver: ${transfer.driverName}",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Present this QR at factory weighbridge scanner for automated gross tare slip generation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissQR() },
                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                ) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun TransferMetricCol(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HarvestGreenDark)
    }
}

@Composable
private fun TransferCard(
    item: TransferItem,
    onShowQR: () -> Unit,
    onConfirmReceipt: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()) }

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
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Filled.LocalShipping, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = item.gatePassNumber, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(text = timeFormat.format(Date(item.dispatchTimestampMillis)), style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (item.status) {
                        TransferStatus.VERIFIED -> HarvestGreenContainer
                        TransferStatus.IN_TRANSIT -> GoldenWheatContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = item.status.displayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when (item.status) {
                            TransferStatus.VERIFIED -> HarvestGreenDark
                            TransferStatus.IN_TRANSIT -> GoldenWheatDark
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = item.residueTitle, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Place, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = item.sourceLocation, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
                    Row(modifier = Modifier.padding(start = 6.dp)) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = DarkTextSecondary, modifier = Modifier.size(12.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Factory, contentDescription = null, tint = HarvestGreenDark, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = item.destinationPlant, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Truck Registration", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    Text(text = item.vehicleNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Column {
                    Text(text = "Driver Contact", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    Text(text = item.driverName, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Source Net", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    Text(text = "${item.weightAtSourceMt} MT", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = HarvestGreen)
                }
            }

            if (item.weighbridgeNetMt != null) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Weighbridge Net: ${item.weighbridgeNetMt} MT (Gross: ${item.weighbridgeGrossMt} / Tare: ${item.weighbridgeTareMt})",
                        style = MaterialTheme.typography.labelSmall,
                        color = HarvestGreenDark,
                        fontWeight = FontWeight.SemiBold
                    )
                    Surface(shape = CircleShape, color = HarvestGreen) {
                        Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp).padding(2.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Show QR", fontSize = 12.sp)
                }

                if (item.status != TransferStatus.VERIFIED) {
                    Button(
                        onClick = onConfirmReceipt,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                    ) {
                        Text("Verify Receipt", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
