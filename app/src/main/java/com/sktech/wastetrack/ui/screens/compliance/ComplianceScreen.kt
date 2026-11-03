package com.sktech.wastetrack.ui.screens.compliance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sktech.wastetrack.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplianceScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: ComplianceViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("ESG Regulatory Compliance", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("MPCB Clean Air Mandates & Green Registry", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.triggerExportReport() }) {
                        Icon(imageVector = Icons.Filled.Download, contentDescription = "Export Audit Report", tint = HarvestGreen)
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
                EsgQuotaCard(
                    fulfilledMt = state.fulfilledTonnageMt,
                    targetMt = state.annualTargetMt,
                    percent = state.fulfillmentPercentage,
                    co2Saved = state.co2TotalAvertedMt
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Verified ESG Regulatory Certificates",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = HarvestGreenLight.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = "${state.certificates.size} CERTIFIED",
                            color = HarvestGreenDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            items(state.certificates, key = { it.id }) { cert ->
                CertificateCard(
                    cert = cert,
                    onClick = { viewModel.selectCertificate(cert) }
                )
            }
        }
    }

    if (state.selectedCertificate != null) {
        val cert = state.selectedCertificate!!
        val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }

        AlertDialog(
            onDismissRequest = { viewModel.dismissCertificateDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Filled.VerifiedUser, contentDescription = null, tint = HarvestGreen)
                    Text("Official ESG Certificate", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = HarvestGreenLight.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(cert.certificateNumber, fontWeight = FontWeight.Bold, color = HarvestGreenDark, fontSize = 15.sp)
                            Text(cert.title, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            Text("Issuing Authority: ${cert.issuingAuthority}", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                            HorizontalDivider(color = HarvestGreen.copy(alpha = 0.25f), thickness = 1.dp)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Biomass Volume:", style = MaterialTheme.typography.labelSmall)
                                Text("${cert.tonnageMt} MT", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("CO? Offset:", style = MaterialTheme.typography.labelSmall)
                                Text("${cert.co2SavedMt} MT CO?e", fontWeight = FontWeight.Bold, color = HarvestGreenDark, fontSize = 13.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Audit Date:", style = MaterialTheme.typography.labelSmall)
                                Text(dateFormat.format(Date(cert.issueDateMillis)), style = MaterialTheme.typography.labelSmall)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("SHA Signature:", style = MaterialTheme.typography.labelSmall)
                                Text(cert.digitalHash, style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissCertificateDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                ) {
                    Text("Close Pass")
                }
            }
        )
    }

    if (state.showExportToast) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissToast() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null, tint = HarvestGreen)
                    Text("Audit Manifest Exported", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text("Official MPCB Form 10 Electronic Manifest & Stubble Mitigation Registry exported for statutory submission.")
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
private fun EsgQuotaCard(
    fulfilledMt: Double,
    targetMt: Double,
    percent: Float,
    co2Saved: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkHarvestGreenContainer),
        border = BorderStroke(1.dp, HarvestGreenSoft.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = CircleShape,
                        color = HarvestGreenSoft.copy(alpha = 0.2f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Filled.Balance, contentDescription = null, tint = HarvestGreenSoft, modifier = Modifier.size(20.dp))
                        }
                    }
                    Column {
                        Text("Annual ESG Quota Fulfillment", fontWeight = FontWeight.Bold, color = HarvestGreenSoft, fontSize = 15.sp)
                        Text("MPCB Stubble Burning Mitigation Mandate", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = HarvestGreenSoft.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "MPCB COMPLIANT",
                        color = HarvestGreenSoft,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${String.format(Locale.US, "%.0f", percent * 100f)}% Target Achieved",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    Text(
                        text = "${String.format(Locale.US, "%,.0f", fulfilledMt)} / ${String.format(Locale.US, "%,.0f", targetMt)} MT",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = GoldenWheatLight
                    )
                }

                LinearProgressIndicator(
                    progress = { percent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = HarvestGreenSoft,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }

            Surface(
                color = Color.Black.copy(alpha = 0.25f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Certified Net Carbon Saved", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                        Text("${String.format(Locale.US, "%.1f", co2Saved)} MT CO?e", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    }
                    Surface(
                        color = HarvestGreenSoft.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "FORM 10 FILED",
                            color = HarvestGreenSoft,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CertificateCard(
    cert: EsgCertificate,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                Column {
                    Text(cert.certificateNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(cert.categoryName, style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                }

                Surface(
                    color = HarvestGreenContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${cert.tonnageMt} MT VERIFIED",
                        color = HarvestGreenDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text(cert.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(cert.issuingAuthority, style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Lock, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(cert.digitalHash.take(10) + "...", style = MaterialTheme.typography.labelSmall, color = HarvestGreenDark)
                    }
                }
            }
        }
    }
}
