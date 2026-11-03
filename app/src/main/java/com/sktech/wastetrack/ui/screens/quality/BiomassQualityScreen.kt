package com.sktech.wastetrack.ui.screens.quality

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sktech.wastetrack.domain.model.quality.BiomassQualityReport
import com.sktech.wastetrack.domain.model.quality.QualityGrade
import com.sktech.wastetrack.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiomassQualityScreen(
    onNavigateBack: () -> Unit,
    viewModel: BiomassQualityViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val filteredReports = remember(state.reports, state.selectedFilter) {
        when (state.selectedFilter) {
            "Grade A" -> state.reports.filter { it.grade == QualityGrade.GRADE_A_PREMIUM }
            "Penalty" -> state.reports.filter { it.grade == QualityGrade.GRADE_C_PENALTY || it.grade == QualityGrade.GRADE_B_STANDARD }
            "Rejected" -> state.reports.filter { it.grade == QualityGrade.REJECTED }
            "Pending" -> state.reports.filter { !it.isEscrowApproved }
            else -> state.reports
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Gate Lab Quality & Moisture Audit",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "MNRE & IS 18134 • Kasturi Shinde (QA Lead)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openEntryDialog(true) },
                icon = { Icon(Icons.Filled.Science, contentDescription = null) },
                text = { Text("Log Gate Test", fontWeight = FontWeight.Bold) },
                containerColor = HarvestGreen,
                contentColor = Color.White
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                LaboratoryBannerCard()
            }

            item {
                QualityStatsRow(reports = state.reports)
            }

            item {
                val filters = listOf("All", "Grade A", "Penalty", "Pending", "Rejected")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filters) { filter ->
                        FilterChip(
                            selected = state.selectedFilter == filter,
                            onClick = { viewModel.setFilter(filter) },
                            label = { Text(filter, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HarvestGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            items(filteredReports, key = { it.id }) { report ->
                QualityReportCard(
                    report = report,
                    onViewCertificate = { viewModel.selectReportForCertificate(report) },
                    onAuthorizeEscrow = {
                        viewModel.approveEscrow(report.id)
                        Toast.makeText(context, "Escrow Released for Lot ${report.lotNumber}", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (state.isEntryDialogOpen) {
        NewGateTestDialog(
            onDismiss = { viewModel.openEntryDialog(false) },
            onSubmit = { orderId, lotNo, commodity, fpo, vehicle, gcv, moisture, ash, escrow ->
                viewModel.submitGateLabTest(orderId, lotNo, commodity, fpo, vehicle, gcv, moisture, ash, escrow)
                Toast.makeText(context, "Lab Certificate Generated Successfully", Toast.LENGTH_SHORT).show()
            }
        )
    }

    state.selectedReportForCertificate?.let { report ->
        LabCertificateModal(
            report = report,
            onDismiss = { viewModel.selectReportForCertificate(null) },
            onAuthorizeEscrow = {
                viewModel.approveEscrow(report.id)
                Toast.makeText(context, "Escrow Released for Lot ${report.lotNumber}", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun LaboratoryBannerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkHarvestGreenContainer),
        border = BorderStroke(1.dp, HarvestGreenSoft.copy(alpha = 0.35f))
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
                    color = HarvestGreenSoft.copy(alpha = 0.2f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Biotech,
                            contentDescription = null,
                            tint = HarvestGreenSoft,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "Nashik Central Bio-Energy Lab",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = HarvestGreenSoft
                    )
                    Text(
                        text = "Calorific Bomb Bomb-Calorimetry & Dual Halogen Moisture QA",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = HarvestGreenSoft.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "ISO 17025",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = HarvestGreenSoft,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun QualityStatsRow(reports: List<BiomassQualityReport>) {
    val total = reports.size
    val avgGcv = if (total > 0) reports.map { it.grossCalorificValueKcal }.average() else 0.0
    val avgMoisture = if (total > 0) reports.map { it.moisturePercentage }.average() else 0.0

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Avg Calorific", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${String.format("%,.0f", avgGcv)} kcal",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = HarvestGreen
                )
                Text("Boiler fuel standard", fontSize = 10.sp, color = DarkTextSecondary)
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Avg Moisture", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${String.format("%.1f", avgMoisture)}%",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (avgMoisture <= 12.0) SuccessGreen else WarningOrange
                )
                Text("Target ≤ 12.0%", fontSize = 10.sp, color = DarkTextSecondary)
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Batch Samples", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$total Tested",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text("Gate arrivals", fontSize = 10.sp, color = DarkTextSecondary)
            }
        }
    }
}

@Composable
private fun QualityReportCard(
    report: BiomassQualityReport,
    onViewCertificate: () -> Unit,
    onAuthorizeEscrow: () -> Unit
) {
    val gradeColor = when (report.grade) {
        QualityGrade.GRADE_A_PREMIUM -> SuccessGreen
        QualityGrade.GRADE_B_STANDARD -> HarvestGreen
        QualityGrade.GRADE_C_PENALTY -> WarningOrange
        QualityGrade.REJECTED -> Color(0xFFE53935)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = report.certificateNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${report.commodity} • ${report.lotNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = gradeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = report.grade.label,
                        color = gradeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("GCV Energy", fontSize = 10.sp, color = DarkTextSecondary)
                        Text(
                            text = "${report.grossCalorificValueKcal.toInt()} kcal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = HarvestGreen
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Moisture", fontSize = 10.sp, color = DarkTextSecondary)
                        Text(
                            text = "${report.moisturePercentage}%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (report.moisturePercentage <= 12.0) SuccessGreen else WarningOrange
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Ash Content", fontSize = 10.sp, color = DarkTextSecondary)
                        Text(
                            text = "${report.ashPercentage}%",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Truck Entry", fontSize = 10.sp, color = DarkTextSecondary)
                        Text(
                            text = report.vehicleNumber,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
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
                    if (report.moisturePenaltyDeductionInr > 0.0) {
                        Text(
                            text = "Penalty Deducted: ₹${String.format("%,.0f", report.moisturePenaltyDeductionInr)}",
                            fontSize = 11.sp,
                            color = WarningOrange,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "Net Release: ₹${String.format("%,.0f", report.netEscrowReleaseAmount)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onViewCertificate,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Certificate", fontSize = 12.sp)
                    }

                    if (!report.isEscrowApproved && report.grade != QualityGrade.REJECTED) {
                        Button(
                            onClick = onAuthorizeEscrow,
                            colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Release", fontSize = 12.sp)
                        }
                    } else if (report.isEscrowApproved) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SuccessGreen.copy(alpha = 0.15f),
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Text(
                                text = "Settled",
                                color = SuccessGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewGateTestDialog(
    onDismiss: () -> Unit,
    onSubmit: (orderId: String, lotNo: String, commodity: String, fpo: String, vehicle: String, gcv: Double, moisture: Double, ash: Double, escrow: Double) -> Unit
) {
    var orderId by remember { mutableStateOf("ORD-2026-9520") }
    var lotNo by remember { mutableStateOf("LOT-COTTON-144") }
    var commodity by remember { mutableStateOf("Baled Dry Cotton Stalks") }
    var fpo by remember { mutableStateOf("Sinnar Agro FPO Cooperative") }
    var vehicle by remember { mutableStateOf("MH-15-EG-7721") }
    var gcvText by remember { mutableStateOf("3920") }
    var moistureText by remember { mutableStateOf("11.5") }
    var ashText by remember { mutableStateOf("4.2") }
    var escrowText by remember { mutableStateOf("125000") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New Gate Lab Test",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = null)
                    }
                }

                OutlinedTextField(
                    value = lotNo,
                    onValueChange = { lotNo = it },
                    label = { Text("Lot Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = vehicle,
                    onValueChange = { vehicle = it },
                    label = { Text("Vehicle Registration No.") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = gcvText,
                        onValueChange = { gcvText = it },
                        label = { Text("GCV (kcal/kg)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = moistureText,
                        onValueChange = { moistureText = it },
                        label = { Text("Moisture %") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = ashText,
                        onValueChange = { ashText = it },
                        label = { Text("Ash Content %") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = escrowText,
                        onValueChange = { escrowText = it },
                        label = { Text("Gross Escrow (₹)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = {
                        val gcv = gcvText.toDoubleOrNull() ?: 3800.0
                        val moisture = moistureText.toDoubleOrNull() ?: 12.0
                        val ash = ashText.toDoubleOrNull() ?: 4.0
                        val escrow = escrowText.toDoubleOrNull() ?: 100000.0
                        onSubmit(orderId, lotNo, commodity, fpo, vehicle, gcv, moisture, ash, escrow)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                ) {
                    Icon(imageVector = Icons.Filled.Science, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Analyze & Generate Certificate", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LabCertificateModal(
    report: BiomassQualityReport,
    onDismiss: () -> Unit,
    onAuthorizeEscrow: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "LAB QUALITY CERTIFICATE",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = HarvestGreenDark
                        )
                        Text(
                            text = report.certificateNumber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarkTextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = null)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    text = "Consignment & Supplier Info",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Supplier: ${report.supplierFpo}", fontSize = 12.sp)
                Text("Commodity: ${report.commodity}", fontSize = 12.sp)
                Text("Lot Number: ${report.lotNumber} • Vehicle: ${report.vehicleNumber}", fontSize = 12.sp)
                Text("Tested At: ${dateFormat.format(Date(report.dateMillis))}", fontSize = 11.sp, color = DarkTextSecondary)

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("PROXIMATE ANALYSIS (IS 18134)", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Gross Calorific Value (GCV):", fontSize = 12.sp)
                            Text("${report.grossCalorificValueKcal.toInt()} kcal/kg", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Moisture Content:", fontSize = 12.sp)
                            Text("${report.moisturePercentage}%", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Ash & Silica:", fontSize = 12.sp)
                            Text("${report.ashPercentage}%", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Volatile Matter:", fontSize = 12.sp)
                            Text("${String.format("%.1f", report.volatileMatterPercentage)}%", fontSize = 12.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Fixed Carbon:", fontSize = 12.sp)
                            Text("${String.format("%.1f", report.fixedCarbonPercentage)}%", fontSize = 12.sp)
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
                        Text("Quality Classification:", fontSize = 11.sp, color = DarkTextSecondary)
                        Text(report.grade.label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Escrow Clearance:", fontSize = 11.sp, color = DarkTextSecondary)
                        Text("₹${String.format("%,.0f", report.netEscrowReleaseAmount)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = HarvestGreen)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Digital Audit Stamp:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = report.digitalSignatureHash,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp,
                            color = DarkTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(report.digitalSignatureHash))
                            Toast.makeText(context, "Certificate Hash Copied", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Stamp", fontSize = 12.sp)
                    }

                    if (!report.isEscrowApproved && report.grade != QualityGrade.REJECTED) {
                        Button(
                            onClick = onAuthorizeEscrow,
                            colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Approve Escrow", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

