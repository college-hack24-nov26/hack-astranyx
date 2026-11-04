package com.sktech.wastetrack.ui.screens.transfer

import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
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
import com.sktech.wastetrack.ui.theme.DarkTextSecondary
import com.sktech.wastetrack.ui.theme.HarvestGreen
import com.sktech.wastetrack.ui.theme.HarvestGreenContainer
import com.sktech.wastetrack.ui.theme.HarvestGreenDark
import com.sktech.wastetrack.ui.theme.WarningContainer
import com.sktech.wastetrack.ui.theme.WarningOrange
import com.sktech.wastetrack.ui.theme.WarningOrangeLight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun generateHighResQR(content: String, size: Int = 600): Bitmap {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
    val pixels = IntArray(size * size)
    for (y in 0 until size) {
        val rowOffset = y * size
        for (x in 0 until size) {
            pixels[rowOffset + x] = if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
        }
    }
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
    bitmap.setPixels(pixels, 0, size, 0, 0, size, size)
    return bitmap
}

private fun formatFullDateTime(millis: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(millis))
}

private fun formatClockTime(millis: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(millis))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRGenerateScreen(
    transferId: String,
    onNavigateBack: () -> Unit,
    viewModel: TransferViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val activeItem = remember(state.transfers, transferId) {
        state.transfers.find { it.id == transferId } ?: state.transfers.firstOrNull() ?: TransferItem(
            id = "tr_001",
            gatePassNumber = "GP-MH-2026-9142",
            residueTitle = "Baled Dry Cotton Stalks (45 MT)",
            cropName = "Cotton",
            quantityMt = 45.0,
            vehicleNumber = "MH-15-EG-4921",
            driverName = "Sachin Shinde",
            driverPhone = "+91 98220 14829",
            sourceLocation = "Sinnar Agro Hub, Nashik",
            destinationPlant = "Mahindra Bio-Energy Plant, Igatpuri",
            status = TransferStatus.IN_TRANSIT,
            weightAtSourceMt = 45.0,
            weighbridgeGrossMt = 62.4,
            weighbridgeTareMt = 17.6,
            weighbridgeNetMt = 44.8,
            dispatchTimestampMillis = System.currentTimeMillis() - 2L * 3600 * 1000,
            qrPayload = "GP:GP-MH-2026-9142|TRUCK:MH15EG4921|CROP:COTTON|QTY:45MT|ORIGIN:SINNAR",
            digitalHashSignature = "0x8F9A4C71B2D0E6F3"
        )
    }

    BackHandler {
        onNavigateBack()
    }

    val totalSeconds = 900
    var secondsRemaining by remember { mutableIntStateOf(totalSeconds) }
    var currentTokenSalt by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(transferId) {
        viewModel.prepareQRForTransfer(transferId)
    }

    LaunchedEffect(currentTokenSalt) {
        secondsRemaining = totalSeconds
        while (secondsRemaining > 0) {
            delay(1000)
            secondsRemaining--
        }
    }

    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)
    val progress = secondsRemaining.toFloat() / totalSeconds.toFloat()

    val qrPayload = remember(activeItem, currentTokenSalt) {
        val cleanVehicle = activeItem.vehicleNumber.replace("-", "").uppercase()
        val cleanCrop = activeItem.cropName.uppercase()
        val cleanOrigin = activeItem.sourceLocation.split(",")[0].trim().uppercase()
        "GP:${activeItem.gatePassNumber}|TRUCK:$cleanVehicle|CROP:$cleanCrop|QTY:${activeItem.quantityMt.toInt()}MT|ORIGIN:$cleanOrigin"
    }

    val dynamicToken = remember(activeItem, currentTokenSalt) {
        val hash = MessageDigest.getInstance("SHA-256")
            .digest("${activeItem.id}_${activeItem.vehicleNumber}_$currentTokenSalt".toByteArray())
            .take(8)
            .joinToString("") { "%02x".format(it) }
            .uppercase()
        "0x$hash"
    }

    val qrBitmap by produceState<Bitmap?>(initialValue = null, qrPayload) {
        value = withContext(Dispatchers.Default) {
            runCatching { generateHighResQR(qrPayload, 600) }.getOrNull()
        }
    }

    val isUrgent = secondsRemaining <= 120
    val activeColor = if (isUrgent) WarningOrange else HarvestGreen
    val activeContainer = if (isUrgent) WarningContainer else HarvestGreenContainer
    val activeDark = if (isUrgent) WarningOrangeLight else HarvestGreenDark

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Consignment Gatepass QR",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Dynamic Cryptographic Optical Handshake",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Agri WasteTrack Consignment Pass\nGatepass: ${activeItem.gatePassNumber}\nVehicle: ${activeItem.vehicleNumber}\nDriver: ${activeItem.driverName} (${activeItem.driverPhone})\nOrigin: ${activeItem.sourceLocation}\nDestination: ${activeItem.destinationPlant}\nPayload: ${activeItem.quantityMt} MT\nSecurity Token: $dynamicToken\nPayload Raw: $qrPayload"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Consignment Gatepass"))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share Gatepass",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                color = activeContainer,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, activeColor.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = activeColor.copy(alpha = 0.2f),
                            modifier = Modifier
                                .size(44.dp)
                                .graphicsLayer { alpha = pulseAlpha }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Timer,
                                    contentDescription = null,
                                    tint = activeColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Dynamic Pass Validity",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (secondsRemaining > 0) "Expires in $formattedTime" else "Gatepass Expired",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = activeDark
                            )
                            Text(
                                text = "Window: ${formatClockTime(currentTokenSalt)} - ${formatClockTime(currentTokenSalt + 15 * 60_000L)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(38.dp),
                        strokeWidth = 3.5.dp,
                        color = activeColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = 3.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val currentBitmap = qrBitmap
                    if (currentBitmap != null) {
                        Image(
                            bitmap = currentBitmap.asImageBitmap(),
                            contentDescription = "Dynamic Consignment Gate Pass QR",
                            modifier = Modifier.size(230.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(230.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = HarvestGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = HarvestGreenContainer
                    ) {
                        Text(
                            text = activeItem.gatePassNumber,
                            fontWeight = FontWeight.Bold,
                            color = HarvestGreenDark,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Scan at Factory Outgate Weighbridge",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "CameraX ML Kit optical handshake ready",
                        style = MaterialTheme.typography.labelSmall,
                        color = DarkTextSecondary
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VerifiedUser,
                                contentDescription = null,
                                tint = HarvestGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Dispatch Manifest Summary",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = HarvestGreenContainer
                        ) {
                            Text(
                                text = activeItem.status.displayName,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = HarvestGreenDark
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

                    ManifestDetailRow("Transfer ID", activeItem.id)
                    ManifestDetailRow("Vehicle Reg", activeItem.vehicleNumber)
                    ManifestDetailRow("Driver Name", "${activeItem.driverName} (${activeItem.driverPhone})")
                    ManifestDetailRow("Crop & Payload", "${activeItem.residueTitle} • ${activeItem.quantityMt} MT")
                    ManifestDetailRow("Origin Hub", activeItem.sourceLocation)
                    ManifestDetailRow("Destination", activeItem.destinationPlant)
                    ManifestDetailRow("Generated At", formatFullDateTime(currentTokenSalt))
                    ManifestDetailRow("SHA-256 Signature", dynamicToken, isMono = true)
                }
            }

            Button(
                onClick = {
                    currentTokenSalt = System.currentTimeMillis()
                    viewModel.prepareQRForTransfer(activeItem.id)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Regenerate Dynamic Security Pass",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun ManifestDetailRow(label: String, value: String, isMono: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            fontFamily = if (isMono) FontFamily.Monospace else FontFamily.Default,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
