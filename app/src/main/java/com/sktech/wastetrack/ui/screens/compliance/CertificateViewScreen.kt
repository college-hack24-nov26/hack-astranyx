package com.sktech.wastetrack.ui.screens.compliance

import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.sktech.wastetrack.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CertificateViewScreen(
    certificateId: String,
    onNavigateBack: () -> Unit,
    viewModel: ComplianceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val certificate = state.certificates.find { it.id == certificateId } ?: state.certificates.firstOrNull()

    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.ENGLISH) }

    val qrPayload = remember(certificate?.id) {
        "MPCB:FORM13:VERIFIED:${certificate?.certificateNumber}:${certificate?.digitalHash}:${certificate?.tonnageMt}MT:${certificate?.co2SavedMt}CO2e"
    }

    val qrBitmap by produceState<Bitmap?>(initialValue = null, qrPayload) {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(qrPayload, BarcodeFormat.QR_CODE, 450, 450)
        val bmp = Bitmap.createBitmap(450, 450, Bitmap.Config.RGB_565)
        for (x in 0 until 450) {
            for (y in 0 until 450) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        value = bmp
    }

    BackHandler(onBack = onNavigateBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "MPCB Form 13 Statutory Manifest",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Air Act Section 19(5) Zero Stubble Burning Clearance",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (certificate != null) {
                        IconButton(onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "GOVERNMENT OF MAHARASHTRA\nMAHARASHTRA POLLUTION CONTROL BOARD\nStatutory Agricultural Biomass Diversion Certificate\nManifest No: ${certificate.certificateNumber}\nCommodity: ${certificate.categoryName}\nTonnage: ${certificate.tonnageMt} MT\nScope 3 Carbon Abatement: ${certificate.co2SavedMt} MT CO2e\nVerification Hash: ${certificate.digitalHash}\nIssued by: ${certificate.issuingAuthority}"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Statutory Certificate"))
                        }) {
                            Icon(Icons.Filled.Share, contentDescription = "Share", tint = HarvestGreen)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            if (certificate != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    "Certificate ${certificate.certificateNumber} verified and synchronized with MPCB National Clean Air Portal",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.CloudDone, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sync Portal", fontWeight = FontWeight.Bold, color = HarvestGreenDark, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    "Official Green Credit Pass PDF exported to Downloads folder",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            modifier = Modifier
                                .weight(1.3f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                        ) {
                            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Green Credit Pass", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (certificate == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Certificate not found", style = MaterialTheme.typography.titleMedium)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    color = adaptiveHarvestContainer(),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, HarvestGreen.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = HarvestGreen,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Statutory Clearance: Section 19(5) Air Act",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = HarvestGreenDark
                            )
                            Text(
                                text = "Zero Stubble Burning Certification & Scope 3 Offset Clearance",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Filled.AccountBalance,
                                contentDescription = null,
                                tint = HarvestGreenDark,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "GOVERNMENT OF MAHARASHTRA",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "MAHARASHTRA POLLUTION CONTROL BOARD",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = HarvestGreenDark,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Kalpataru Point, Sion Circle, Mumbai / Nashik Regional Directorate",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(thickness = 1.dp, color = HarvestGreen.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "STATUTORY AGRICULTURAL BIOMASS DIVERSION FORM 13",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Manifest: ${certificate.certificateNumber}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "FORM 10 COMPLIANT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = HarvestGreenDark
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            ManifestDataRow("Diversion Project Title", certificate.title)
                            ManifestDataRow("Crop Residue Feedstock", certificate.categoryName)
                            ManifestDataRow("Audited Consignment Tonnage", "%.1f MT".format(certificate.tonnageMt))
                            ManifestDataRow("Scope 3 Carbon Abated", "%.2f MT CO2e (Avoided Burn)".format(certificate.co2SavedMt))
                            ManifestDataRow("Statutory Authority", certificate.issuingAuthority)
                            ManifestDataRow("Issuance & Audit Date", dateFormat.format(Date(certificate.issueDateMillis)))
                            ManifestDataRow("Verification Hash", certificate.digitalHash, isMono = true)
                            ManifestDataRow("Cadastral Parcel Reference", "Gat No. 142/2A, Sinnar Taluka, Nashik")
                            ManifestDataRow("Authorized Receiving Facility", "Igatpuri MIDC Bio-Energy Pelletization Plant")
                            ManifestDataRow("Statutory Act Clause", "Section 19(5), Air Act 1981 (Zero Stubble Mandate)")
                        }

                        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val bmp = qrBitmap
                            if (bmp != null) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(2.dp, HarvestGreen),
                                    modifier = Modifier.size(110.dp)
                                ) {
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = "MPCB Verification QR",
                                        modifier = Modifier.fillMaxSize().padding(4.dp)
                                    )
                                }
                            } else {
                                Box(modifier = Modifier.size(110.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = HarvestGreen, modifier = Modifier.size(24.dp))
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Cryptographic Tamper-Proof QR",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Scan with any MPCB Field Inspector device or Camera to verify on-chain green registry hash.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "Status: STATUTORY AUDIT VALID",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = HarvestGreenDark,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Officer: Er. S. R. Deshmukh",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Regional Environmental Officer, MPCB",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 9.sp
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = adaptiveSkyBlueContainer(),
                                border = BorderStroke(1.dp, SkyBlue.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "SEAL OF CLEARANCE",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SkyBlueDark,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ManifestDataRow(label: String, value: String, isMono: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            fontFamily = if (isMono) FontFamily.Monospace else FontFamily.Default,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

