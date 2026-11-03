package com.sktech.wastetrack.ui.screens.invoice

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sktech.wastetrack.domain.model.invoice.AgriHsnCatalog
import com.sktech.wastetrack.domain.model.invoice.AgriHsnItem
import com.sktech.wastetrack.domain.model.invoice.BiomassInvoice
import com.sktech.wastetrack.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    onNavigateBack: () -> Unit,
    viewModel: InvoiceViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val totalValue = remember(state.invoices) {
        state.invoices.sumOf { it.totalAmount }
    }
    val totalTonnage = remember(state.invoices) {
        state.invoices.sumOf { it.quantityMt }
    }
    val totalGst = remember(state.invoices) {
        state.invoices.sumOf { it.cgstAmount + it.sgstAmount }
    }

    val filteredInvoices = remember(state.invoices, state.selectedFilter) {
        when (state.selectedFilter) {
            "Paid" -> state.invoices.filter { it.status == "PAID & SETTLED" }
            "Pending" -> state.invoices.filter { it.status != "PAID & SETTLED" }
            else -> state.invoices
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "GST Biomass Invoices & E-Way Bills",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "MPCB Electronic Gatepass & Tax Ledger",
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
                onClick = { viewModel.openCreateDialog() },
                containerColor = HarvestGreen,
                contentColor = Color.White,
                icon = { Icon(Icons.Filled.PostAdd, contentDescription = null) },
                text = { Text("Draft GST Invoice", fontWeight = FontWeight.Bold) }
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkHarvestGreenContainer),
                    border = BorderStroke(1.dp, HarvestGreenSoft.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Biomass Billing Value",
                                    color = HarvestGreenLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "₹${String.format("%,.2f", totalValue)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = Color.White
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = HarvestGreen.copy(alpha = 0.3f),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Receipt,
                                        contentDescription = null,
                                        tint = GoldenWheatLight,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = HarvestGreenSoft.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Invoiced Volume", color = DarkTextSecondary, style = MaterialTheme.typography.bodySmall)
                                Text("${String.format("%.1f", totalTonnage)} MT", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            }
                            Column {
                                Text("GST Remitted (5%)", color = DarkTextSecondary, style = MaterialTheme.typography.bodySmall)
                                Text("₹${String.format("%,.2f", totalGst)}", fontWeight = FontWeight.Bold, color = GoldenWheatLight, fontSize = 14.sp)
                            }
                            Column {
                                Text("Tax Regime", color = DarkTextSecondary, style = MaterialTheme.typography.bodySmall)
                                Text("CGST + SGST (2.5%)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val filters = listOf("All", "Paid", "Pending")
                    items(filters) { filter ->
                        FilterChip(
                            selected = state.selectedFilter == filter,
                            onClick = { viewModel.setFilter(filter) },
                            label = { Text(filter) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HarvestGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Electronic Invoices & Manifests (${filteredInvoices.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(filteredInvoices, key = { it.id }) { invoice ->
                InvoiceCard(
                    invoice = invoice,
                    onViewSlip = { viewModel.selectInvoiceForPreview(invoice) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (state.isCreateDialogOpen) {
        CreateInvoiceDialog(
            onDismiss = { viewModel.closeCreateDialog() },
            onSave = { consignor, consignee, hsn, qty, rate, vehicle, mpcb ->
                viewModel.createInvoice(consignor, consignee, hsn, qty, rate, vehicle, mpcb)
            }
        )
    }

    state.selectedInvoiceForPreview?.let { previewInvoice ->
        InvoicePreviewDialog(
            invoice = previewInvoice,
            onDismiss = { viewModel.selectInvoiceForPreview(null) },
            onExportPdf = {
                Toast.makeText(context, "Exporting GST Invoice ${previewInvoice.invoiceNumber} PDF...", Toast.LENGTH_SHORT).show()
                viewModel.selectInvoiceForPreview(null)
            }
        )
    }
}

@Composable
private fun InvoiceCard(
    invoice: BiomassInvoice,
    onViewSlip: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, HarvestGreen.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = invoice.invoiceNumber,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = invoice.eWayBillNumber,
                        style = MaterialTheme.typography.labelSmall,
                        color = SkyBlueDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (invoice.status.contains("PAID")) HarvestGreenContainer else DarkGoldenWheatContainer
                ) {
                    Text(
                        text = invoice.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (invoice.status.contains("PAID")) HarvestGreenDark else GoldenWheatLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${invoice.consignorName} → ${invoice.consigneeName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(shape = RoundedCornerShape(6.dp), color = SkyBlueContainer) {
                    Text(
                        text = "HSN ${invoice.hsnCode} • ${invoice.commodity}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SkyBlueDark
                    )
                }
                Surface(shape = RoundedCornerShape(6.dp), color = GoldenWheatContainer) {
                    Text(
                        text = "${invoice.quantityMt} MT @ ₹${invoice.ratePerMt.toInt()}/MT",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = GoldenWheatDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Taxable: ₹${String.format("%,.0f", invoice.taxableAmount)} + GST (5%): ₹${String.format("%,.0f", invoice.cgstAmount + invoice.sgstAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Total: ₹${String.format("%,.2f", invoice.totalAmount)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = HarvestGreenDark
                    )
                }
                Button(
                    onClick = onViewSlip,
                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tax Slip", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Vehicle: ${invoice.vehicleNumber} • MPCB Ref: ${invoice.mpcbForm10Ref} • ${dateFormat.format(Date(invoice.dateMillis))}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontSize = 11.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateInvoiceDialog(
    onDismiss: () -> Unit,
    onSave: (
        consignor: String,
        consignee: String,
        hsnItem: AgriHsnItem,
        qty: Double,
        rate: Double,
        vehicle: String,
        mpcb: String
    ) -> Unit
) {
    var consignorName by remember { mutableStateOf("Sinnar Agro FPO Producer Ltd") }
    var consigneeName by remember { mutableStateOf("Mahindra Bio-Energy Plant, Igatpuri") }
    var selectedHsn by remember { mutableStateOf(AgriHsnCatalog.items.first()) }
    var quantityText by remember { mutableStateOf("45.0") }
    var rateText by remember { mutableStateOf(selectedHsn.benchmarkRatePerMt.toString()) }
    var vehicleNumber by remember { mutableStateOf("MH-15-EG-4921") }
    var mpcbReference by remember { mutableStateOf("MPCB-F10-NSK-2026-9142") }

    val qty = quantityText.toDoubleOrNull() ?: 0.0
    val rate = rateText.toDoubleOrNull() ?: 0.0
    val taxable = qty * rate
    val gst = taxable * 0.05
    val total = taxable + gst + (taxable * 0.01)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Draft GST Biomass Invoice", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = consignorName,
                    onValueChange = { consignorName = it },
                    label = { Text("Consignor (Farmer / FPO)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = consigneeName,
                    onValueChange = { consigneeName = it },
                    label = { Text("Consignee (Industry Buyer)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Select HSN Code & Commodity", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(AgriHsnCatalog.items) { item ->
                        FilterChip(
                            selected = selectedHsn == item,
                            onClick = {
                                selectedHsn = item
                                rateText = item.benchmarkRatePerMt.toString()
                            },
                            label = { Text("HSN ${item.hsnCode} • ${item.commodityName}", fontSize = 11.sp) }
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Quantity (MT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = rateText,
                        onValueChange = { rateText = it },
                        label = { Text("Rate (₹/MT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = vehicleNumber,
                        onValueChange = { vehicleNumber = it },
                        label = { Text("Vehicle No.") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = mpcbReference,
                        onValueChange = { mpcbReference = it },
                        label = { Text("MPCB Gatepass") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = HarvestGreenContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Taxable: ₹${String.format("%,.0f", taxable)} • CGST (2.5%): ₹${String.format("%,.0f", gst / 2)}", fontSize = 11.sp, color = HarvestGreenDark)
                        Text("SGST (2.5%): ₹${String.format("%,.0f", gst / 2)} • TCS (1%): ₹${String.format("%,.0f", taxable * 0.01)}", fontSize = 11.sp, color = HarvestGreenDark)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Gross Total: ₹${String.format("%,.2f", total)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = HarvestGreenDark)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(consignorName, consigneeName, selectedHsn, qty, rate, vehicleNumber, mpcbReference)
                },
                colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
            ) {
                Text("Generate Invoice")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun InvoicePreviewDialog(
    invoice: BiomassInvoice,
    onDismiss: () -> Unit,
    onExportPdf: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Official GST Tax Invoice", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(invoice.invoiceNumber, style = MaterialTheme.typography.labelSmall, color = SkyBlueDark)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(shape = RoundedCornerShape(8.dp), color = SkyBlueContainer, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("e-Way Bill No:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SkyBlueDark)
                        Text(invoice.eWayBillNumber, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SkyBlueDark, fontFamily = FontFamily.Monospace)
                    }
                }

                Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Consignor: ${invoice.consignorName}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("GSTIN: ${invoice.consignorGstin}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Consignee: ${invoice.consigneeName}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("GSTIN: ${invoice.consigneeGstin}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                PreviewSlipRow("HSN Classification", "HSN ${invoice.hsnCode} (${invoice.commodity})")
                PreviewSlipRow("Billed Quantity", "${invoice.quantityMt} MT")
                PreviewSlipRow("Contract Rate", "₹${invoice.ratePerMt.toInt()} / MT")
                PreviewSlipRow("Taxable Amount", "₹${String.format("%,.2f", invoice.taxableAmount)}")
                PreviewSlipRow("CGST (2.5%)", "₹${String.format("%,.2f", invoice.cgstAmount)}")
                PreviewSlipRow("SGST (2.5%)", "₹${String.format("%,.2f", invoice.sgstAmount)}")
                PreviewSlipRow("TCS u/s 206C (1%)", "₹${String.format("%,.2f", invoice.tcsAmount)}")

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Invoice Amount:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("₹${String.format("%,.2f", invoice.totalAmount)}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = HarvestGreenDark)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Transport Vehicle: ${invoice.vehicleNumber} • Manifest: ${invoice.mpcbForm10Ref}", fontSize = 11.sp, color = DarkTextSecondary)
                Text("Timestamp: ${dateFormat.format(Date(invoice.dateMillis))}", fontSize = 10.sp, color = DarkTextSecondary)
            }
        },
        confirmButton = {
            Button(
                onClick = onExportPdf,
                colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
            ) {
                Icon(imageVector = Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export PDF Slip")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun PreviewSlipRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}
