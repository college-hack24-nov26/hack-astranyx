package com.sktech.wastetrack.ui.screens.payment

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.sktech.wastetrack.domain.payment.model.PaymentMethod
import com.sktech.wastetrack.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentCheckoutDialog(
    lotTitle: String = "Shredded Cotton Stalks (45 MT)",
    cropName: String = "Cotton",
    farmerName: String = "Rameshwar Patil",
    farmerPhone: String = "+91 98221 44512",
    buyerName: String = "Igatpuri Bio-Energy Pvt Ltd",
    buyerGstin: String = "27AABCI4819M1Z8",
    weightMt: Double = 45.0,
    ratePerMt: Double = 2800.0,
    onConfirmPayment: (
        method: PaymentMethod,
        transactionRef: String,
        freight: Double,
        handling: Double,
        grandTotal: Double
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var selectedMethod by remember { mutableStateOf(PaymentMethod.UPI_QR) }
    var freightInput by remember { mutableStateOf("6500") }
    var handlingInput by remember { mutableStateOf("1000") }
    var isProcessing by remember { mutableStateOf(false) }

    val baseMaterialCost = weightMt * ratePerMt
    val gstAmount = baseMaterialCost * 0.05
    val tcsAmount = baseMaterialCost * 0.01

    val freight = freightInput.toDoubleOrNull() ?: 0.0
    val transportGst = freight * 0.05
    val handling = handlingInput.toDoubleOrNull() ?: 0.0
    val grandTotal = baseMaterialCost + gstAmount + tcsAmount + freight + transportGst + handling

    val escrowVpa = "agristock.escrow@upi"
    val txnRef = remember { "UTR-AGRI-" + System.currentTimeMillis().toString().takeLast(8) }

    val upiIntentUrl = "upi:" + "/" + "/pay?pa=$escrowVpa&pn=AgriStock%20Escrow&am=%.2f&cu=INR&tn=Biomass%20Consignment".format(Locale.US, grandTotal)

    val qrBitmap by produceState<Bitmap?>(initialValue = null, grandTotal, selectedMethod) {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(upiIntentUrl, BarcodeFormat.QR_CODE, 512, 512)
        val bmp = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
        for (x in 0 until 512) {
            for (y in 0 until 512) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        value = bmp
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Biomass Checkout & Escrow Lock",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Direct Industrial B2B Settlement Protocol",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = adaptiveHarvestContainer(),
                        border = BorderStroke(1.dp, HarvestGreen.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = lotTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Surface(
                                    color = HarvestGreen,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = cropName.uppercase(),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Producer: $farmerName  |  Buyer: $buyerName",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Text(
                                text = "Weight: %.1f MT  @  INR %,.0f / MT".format(weightMt, ratePerMt),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = HarvestGreenDark
                            )
                        }
                    }

                    Text(
                        text = "Select Payment & Settlement Rail",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PaymentMethod.entries.forEach { method ->
                            val isSelected = selectedMethod == method
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { selectedMethod = method },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) adaptiveTealContainer() else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Teal else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedMethod = method }
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = method.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isSelected) Teal else MaterialTheme.colorScheme.surfaceVariant
                                            ) {
                                                Text(
                                                    text = method.badge,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 9.sp,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Text(
                                            text = method.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (selectedMethod == PaymentMethod.UPI_QR) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Scan with any UPI Banking App",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "BHIM, PhonePe, Google Pay, Paytm, Cred",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                val bmp = qrBitmap
                                if (bmp != null) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(3.dp, HarvestGreen),
                                        modifier = Modifier.size(190.dp)
                                    ) {
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = "Dynamic UPI QR Code",
                                            modifier = Modifier.fillMaxSize().padding(8.dp)
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier.size(190.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = HarvestGreen)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "VPA: $escrowVpa",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = { clipboardManager.setText(AnnotatedString(escrowVpa)) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy VPA", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = "Adjust Logistics & Handling Tariffs",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = freightInput,
                            onValueChange = { freightInput = it },
                            label = { Text("Freight Tariff (INR)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = handlingInput,
                            onValueChange = { handlingInput = it },
                            label = { Text("Handling (INR)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Statutory B2B Invoice Breakdown",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            HorizontalDivider()

                            BreakdownRow("Raw Biomass Material Cost", "INR %,.2f".format(baseMaterialCost))
                            BreakdownRow("5% GST on Crop Residue (HSN 1213)", "INR %,.2f".format(gstAmount))
                            BreakdownRow("1% TCS u/s 206C(1)", "INR %,.2f".format(tcsAmount))
                            BreakdownRow("Dedicated Freight Logistics", "INR %,.2f".format(freight))
                            BreakdownRow("5% GTA Freight GST", "INR %,.2f".format(transportGst))
                            BreakdownRow("Loading & Weighbridge Handling", "INR %,.2f".format(handling))

                            HorizontalDivider()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Net Payable into Escrow",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = HarvestGreenDark
                                )
                                Text(
                                    text = "INR %,.2f".format(grandTotal),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = HarvestGreenDark
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = adaptiveSkyBlueContainer(),
                        border = BorderStroke(1.dp, SkyBlue.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Filled.Lock,
                                contentDescription = null,
                                tint = SkyBlueDark,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "Multi-Signature Escrow Vault Protocol",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SkyBlueDark
                                )
                                Text(
                                    text = "Funds are locked in escrow and released directly to the farmer bank account upon digital weighbridge tare-gross confirmation and gatepass generation.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            isProcessing = true
                            onConfirmPayment(selectedMethod, txnRef, freight, handling, grandTotal)
                            onDismiss()
                        },
                        modifier = Modifier.weight(2f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Filled.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Lock Escrow (INR %,.0f)".format(grandTotal),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BreakdownRow(label: String, value: String) {
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
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
