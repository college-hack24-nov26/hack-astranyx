package com.sktech.wastetrack.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.sktech.wastetrack.R
import com.sktech.wastetrack.ui.theme.EmeraldPrimary
import com.sktech.wastetrack.ui.theme.adaptiveEmeraldContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class GatePassData(
    val id: String = "GP-" + System.currentTimeMillis().toString().takeLast(6),
    val scrapEntryId: String = "BIOMASS-NIPHAD-01",
    val fromFactoryId: String = "FARM_GATE_NASHIK",
    val weightAtSource: Float = 1450.0f,
    val vehicleNumber: String = "MH-15-EG-4921",
    val contentHash: String = "SHA256-AGRI-CUSTODY",
    val initiatedAt: Long = System.currentTimeMillis()
)

private fun generateGatePassQRBitmap(content: String, size: Int = 512): Bitmap {
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

@Composable
fun GatePassQRDialog(
    gatePass: GatePassData? = null,
    activeTransferId: String = "GP-ACTIVE-01",
    onDismiss: () -> Unit = {},
    onOpenFullScreen: () -> Unit = {}
) {
    val qrPayload = remember(gatePass, activeTransferId) {
        Gson().toJson(
            mapOf(
                "type" to "CHAIN_OF_CUSTODY_GATEPASS",
                "transferId" to (gatePass?.id ?: activeTransferId),
                "scrapEntryId" to (gatePass?.scrapEntryId ?: "BIOMASS"),
                "fromFactoryId" to (gatePass?.fromFactoryId ?: "FARM_GATE"),
                "weightAtSource" to (gatePass?.weightAtSource ?: 1000f),
                "vehicleNumber" to (gatePass?.vehicleNumber ?: "CARRIER_FLEET"),
                "contentHash" to (gatePass?.contentHash ?: activeTransferId),
                "timestamp" to (gatePass?.initiatedAt ?: System.currentTimeMillis())
            )
        )
    }

    val qrBitmap by produceState<Bitmap?>(initialValue = null, qrPayload) {
        value = withContext(Dispatchers.Default) {
            runCatching { generateGatePassQRBitmap(qrPayload, 480) }.getOrNull()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = adaptiveEmeraldContainer(),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.QrCode2, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
                    }
                }
                Column {
                    Text(
                        stringResource(R.string.gate_pass_qr_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.gate_pass_ref_format, (gatePass?.id ?: activeTransferId).take(10).uppercase()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
                    shadowElevation = 2.dp,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Box(
                        modifier = Modifier.size(220.dp).padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val bitmap = qrBitmap
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = stringResource(R.string.gate_pass_qr_cd),
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            CircularProgressIndicator(color = EmeraldPrimary, modifier = Modifier.size(36.dp))
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = adaptiveEmeraldContainer(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.gate_pass_label_truck_vehicle), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                gatePass?.vehicleNumber.orEmpty().ifBlank { stringResource(R.string.gate_pass_assigned_truck_fallback) },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.gate_pass_label_driver_status), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(stringResource(R.string.gate_pass_status_en_route_farmgate), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.gate_pass_label_pass_validity), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(stringResource(R.string.gate_pass_val_active_handshake), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.gate_pass_security_notice),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onOpenFullScreen()
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Icon(Icons.Filled.Fullscreen, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.gate_pass_btn_open_fullscreen), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}
