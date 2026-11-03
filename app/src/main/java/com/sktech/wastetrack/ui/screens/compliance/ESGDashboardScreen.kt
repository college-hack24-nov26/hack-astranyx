package com.sktech.wastetrack.ui.screens.compliance

import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.sktech.wastetrack.domain.model.SustainabilityDocument
import com.sktech.wastetrack.ui.theme.DarkHarvestGreenContainer
import com.sktech.wastetrack.ui.theme.DarkTextSecondary
import com.sktech.wastetrack.ui.theme.GoldenWheatContainer
import com.sktech.wastetrack.ui.theme.GoldenWheatDark
import com.sktech.wastetrack.ui.theme.GoldenWheatLight
import com.sktech.wastetrack.ui.theme.HarvestGreen
import com.sktech.wastetrack.ui.theme.HarvestGreenContainer
import com.sktech.wastetrack.ui.theme.HarvestGreenDark
import com.sktech.wastetrack.ui.theme.HarvestGreenSoft
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun generateCertificateQRBitmap(content: String, size: Int = 400): Bitmap {
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
fun ESGDashboardScreen(
    onNavigateBack: () -> Unit,
    onNavigateToInvoices: () -> Unit = {},
    viewModel: ESGDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    BackHandler {
        onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ESG Carbon Abatement Desk",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Scope 1/2/3 Stubble Burning Mitigation & MPCB Registry",
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
                            val report = StringBuilder()
                                .appendLine("Agri WasteTrack ESG Carbon Abatement Audit Summary")
                                .appendLine("Reporting Period: Kharif & Rabi 2026")
                                .appendLine("Total Biomass Diverted: ${String.format(Locale.US, "%.1f MT", state.totalBiomassTonnes)}")
                                .appendLine("Scope 1/2/3 Avoided CO2e: +${String.format(Locale.US, "%.2f MT", state.totalCo2AvoidedTonnes)}")
                                .appendLine("PM2.5 Smog Prevented: ${String.format(Locale.US, "%.1f kg", state.totalPm25AvoidedKg)}")
                                .appendLine("Diesel Fuel Displaced: ${String.format(Locale.US, "%,.0f Liters", state.totalDieselReplacedLiters)}")
                                .appendLine("Clean Bio-Energy Generated: ${String.format(Locale.US, "%.1f MWh", state.totalCleanEnergyMwh)}")
                                .appendLine("Compliance Framework: IPCC Tier-1 & MPCB Form 10 Electronic Manifest")
                                .toString()
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, report)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share ESG Audit Summary"))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share Report",
                            tint = HarvestGreen
                        )
                    }
                    IconButton(onClick = onNavigateToInvoices) {
                        Icon(
                            imageVector = Icons.Filled.Receipt,
                            contentDescription = "Invoices",
                            tint = HarvestGreen
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                EsgHeroImpactCard(
                    biomassTonnes = state.totalBiomassTonnes,
                    co2AvoidedTonnes = state.totalCo2AvoidedTonnes,
                    pm25AvoidedKg = state.totalPm25AvoidedKg,
                    dieselLiters = state.totalDieselReplacedLiters,
                    cleanEnergyMwh = state.totalCleanEnergyMwh
                )
            }

            item {
                MethodologyDisclaimerCard()
            }

            item {
                CategoryFilterRow(
                    selectedFilter = state.selectedFilter,
                    onSelectFilter = { viewModel.setFilter(it) }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Verified Certificates (${state.filteredDocuments.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = HarvestGreenContainer
                    ) {
                        Text(
                            text = "100% Traceable",
                            style = MaterialTheme.typography.labelSmall,
                            color = HarvestGreenDark,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            items(state.filteredDocuments, key = { it.documentId }) { doc ->
                EsgCertificateCard(
                    doc = doc,
                    onViewDetail = { viewModel.selectDocument(doc) },
                    onShare = {
                        val shareText = "Agri WasteTrack ESG Certificate\nCert Number: ${doc.certificateNumber}\nCrop: ${doc.cropType}\nTonnage: ${doc.quantityTonnes} MT\nCO2e Averted: +${String.format(Locale.US, "%.2f MT", doc.co2AvoidedTonnes)}\nOrigin: ${doc.farmLocation}\nDestination: ${doc.destinationFacility}\nAuthority: ${doc.issuingAuthority}\nHash: ${doc.digitalHash}"
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Certificate"))
                    }
                )
            }
        }
    }

    if (state.selectedDocument != null) {
        val doc = state.selectedDocument!!
        CertificateDetailDialog(
            doc = doc,
            onDismiss = { viewModel.selectDocument(null) },
            onShare = {
                val shareText = "Agri WasteTrack ESG Certificate\nCert Number: ${doc.certificateNumber}\nCrop: ${doc.cropType}\nTonnage: ${doc.quantityTonnes} MT\nCO2e Averted: +${String.format(Locale.US, "%.2f MT", doc.co2AvoidedTonnes)}\nOrigin: ${doc.farmLocation}\nDestination: ${doc.destinationFacility}\nAuthority: ${doc.issuingAuthority}\nHash: ${doc.digitalHash}"
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                context.startActivity(Intent.createChooser(intent, "Share Certificate"))
            }
        )
    }
}

@Composable
private fun EsgHeroImpactCard(
    biomassTonnes: Double,
    co2AvoidedTonnes: Double,
    pm25AvoidedKg: Double,
    dieselLiters: Double,
    cleanEnergyMwh: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            HarvestGreenDark,
                            DarkHarvestGreenContainer
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = GoldenWheatLight.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Eco,
                                    contentDescription = null,
                                    tint = GoldenWheatLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Zero Field-Burning Abatement",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "IPCC Tier-1 & GHG Protocol Accounting",
                                color = Color.White.copy(alpha = 0.75f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "SCOPE 3",
                            color = GoldenWheatLight,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EsgMetricCell(
                        modifier = Modifier.weight(1f),
                        value = "${biomassTonnes.toInt()} MT",
                        label = "Biomass Diverted",
                        subtext = "${(biomassTonnes * 1000).toInt()} kg stubble saved",
                        accentColor = GoldenWheatLight
                    )
                    EsgMetricCell(
                        modifier = Modifier.weight(1f),
                        value = "+${String.format(Locale.US, "%.1f", co2AvoidedTonnes)} MT",
                        label = "CO2e Abated",
                        subtext = "GHG emissions avoided",
                        accentColor = Color(0xFF81C784)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EsgMetricCell(
                        modifier = Modifier.weight(1f),
                        value = "${pm25AvoidedKg.toInt()} kg",
                        label = "PM2.5 Smog Prevented",
                        subtext = "Ambient air quality index",
                        accentColor = Color(0xFF64B5F6)
                    )
                    EsgMetricCell(
                        modifier = Modifier.weight(1f),
                        value = "${(dieselLiters / 1000).toInt()}k L",
                        label = "Diesel Fuel Replaced",
                        subtext = "${cleanEnergyMwh.toInt()} MWh bio-power generated",
                        accentColor = Color(0xFFFFB74D)
                    )
                }
            }
        }
    }
}

@Composable
private fun EsgMetricCell(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    subtext: String,
    accentColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
            Text(
                text = subtext,
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun MethodologyDisclaimerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = HarvestGreen,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Regulatory Methodology & Audit Framework",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "Emissions factors calculated per IPCC Guidelines for National Greenhouse Gas Inventories: 1.46 kg CO2e avoided per kg stubble diverted from open burning. PM2.5 abatement factor: 0.003 kg/kg. Energy replacement: 0.55 L diesel equivalent/kg. Compliant with Maharashtra Pollution Control Board (MPCB) Solid Waste Rules 2016.",
                style = MaterialTheme.typography.bodySmall,
                color = DarkTextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun CategoryFilterRow(
    selectedFilter: String,
    onSelectFilter: (String) -> Unit
) {
    val filters = listOf(
        "ALL" to "All Streams",
        "BIOFUEL" to "Biofuel Pellets",
        "COGENERATION" to "Power Co-Gen",
        "BIOCHAR" to "Industrial Biochar"
    )

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { (key, label) ->
            val isSelected = selectedFilter == key
            FilterChip(
                selected = isSelected,
                onClick = { onSelectFilter(key) },
                label = { Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = HarvestGreen,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun EsgCertificateCard(
    doc: SustainabilityDocument,
    onViewDetail: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.VerifiedUser,
                        contentDescription = null,
                        tint = HarvestGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = doc.certificateNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = HarvestGreenDark
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = HarvestGreenContainer
                ) {
                    Text(
                        text = "+${String.format(Locale.US, "%.1f", doc.co2AvoidedTonnes)} MT CO2e",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = HarvestGreenDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = doc.cropType, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(
                text = "${doc.quantityTonnes.toInt()} MT Biomass • ${doc.diversionPurpose}",
                style = MaterialTheme.typography.bodySmall,
                color = DarkTextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Factory, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = doc.destinationFacility, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Bolt, contentDescription = null, tint = GoldenWheatDark, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${doc.cleanEnergyMwh.toInt()} MWh Clean Energy • ${(doc.dieselReplacedLiters / 1000).toInt()}k L Diesel Equivalent",
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewDetail,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Filled.QrCode, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Audit QR", fontSize = 12.sp)
                }
                Button(
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                ) {
                    Icon(imageVector = Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CertificateDetailDialog(
    doc: SustainabilityDocument,
    onDismiss: () -> Unit,
    onShare: () -> Unit
) {
    val qrContent = remember(doc) {
        "CERT:${doc.certificateNumber}|ORDER:${doc.orderId}|CROP:${doc.cropType}|QTY:${doc.quantityTonnes}MT|CO2:${doc.co2AvoidedTonnes}MT|FORM10:${doc.form10ManifestNumber}|HASH:${doc.digitalHash}"
    }
    val qrBitmap = remember(qrContent) {
        generateCertificateQRBitmap(qrContent, 400)
    }
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Official Green Certificate",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = doc.issuingAuthority,
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkTextSecondary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = HarvestGreenContainer
                    ) {
                        Text(
                            text = "VERIFIED",
                            style = MaterialTheme.typography.labelSmall,
                            color = HarvestGreenDark,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "Certificate QR",
                    modifier = Modifier
                        .size(160.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )

                Text(
                    text = doc.certificateNumber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = HarvestGreen
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CertRow("Biomass Residue", "${doc.cropType} (${doc.quantityTonnes} MT)")
                        CertRow("Avoided CO2e", "+${String.format(Locale.US, "%.2f MT", doc.co2AvoidedTonnes)}")
                        CertRow("PM2.5 Averted", "${doc.pm25AvoidedKg.toInt()} kg")
                        CertRow("Facility", doc.destinationFacility)
                        CertRow("Form-10 Filing", doc.form10ManifestNumber)
                        CertRow("Audit Date", dateFormat.format(Date(doc.generatedAt)))
                        CertRow("Digital Hash", doc.digitalHash, isMono = true)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Close")
                    }
                    Button(
                        onClick = onShare,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                    ) {
                        Text("Share Certificate", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CertRow(label: String, value: String, isMono: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            fontFamily = if (isMono) FontFamily.Monospace else FontFamily.Default,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
