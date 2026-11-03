package com.sktech.wastetrack.ui.screens.invoice

import android.content.Intent
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sktech.wastetrack.domain.model.invoice.BiomassInvoice
import com.sktech.wastetrack.domain.model.invoice.EWayBillUtils
import com.sktech.wastetrack.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceHistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCreate: () -> Unit,
    viewModel: InvoiceGeneratorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedInvoiceForDetail by remember { mutableStateOf<BiomassInvoice?>(null) }

    BackHandler {
        onNavigateBack()
    }

    val totalValue = remember(state.invoices) {
        state.invoices.sumOf { it.totalAmount }
    }
    val totalVolume = remember(state.invoices) {
        state.invoices.sumOf { it.quantityMt }
    }
    val totalGst = remember(state.invoices) {
        state.invoices.sumOf { it.cgstAmount + it.sgstAmount }
    }

    val filteredList = remember(state.invoices, state.historyFilter) {
        when (state.historyFilter) {
            "Paid & Settled" -> state.invoices.filter { it.status == "PAID & SETTLED" }
            "Pending Clearance" -> state.invoices.filter { it.status != "PAID & SETTLED" }
            else -> state.invoices
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "GST Invoices & E-Way Bills",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Statutory Tax Ledger & Gatepass Archive",
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
                actions = {
                    IconButton(onClick = onNavigateToCreate) {
                        Icon(
                            imageVector = Icons.Filled.AddCard,
                            contentDescription = "New Invoice",
                            tint = HarvestGreen
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
                onClick = onNavigateToCreate,
                containerColor = HarvestGreen,
                contentColor = Color.White,
                icon = { Icon(Icons.Filled.PostAdd, contentDescription = null) },
                text = { Text("Draft E-Way Bill", fontWeight = FontWeight.Bold) }
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
                                    text = "Total Invoiced Trade Value",
                                    color = HarvestGreenLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "INR ${String.format("%,.2f", totalValue)}",
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
                                Text("${String.format("%.1f", totalVolume)} MT", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            }
                            Column {
                                Text("Total GST Remitted", color = DarkTextSecondary, style = MaterialTheme.typography.bodySmall)
                                Text("INR ${String.format("%,.2f", totalGst)}", fontWeight = FontWeight.Bold, color = GoldenWheatLight, fontSize = 14.sp)
                            }
                            Column {
                                Text("Consignments", color = DarkTextSecondary, style = MaterialTheme.typography.bodySmall)
                                Text("${state.invoices.size} Manifests", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val filters = listOf("All", "Paid & Settled", "Pending Clearance")
                    items(filters) { filter ->
                        FilterChip(
                            selected = state.historyFilter == filter,
                            onClick = { viewModel.setHistoryFilter(filter) },
                            label = { Text(filter) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HarvestGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            items(filteredList) { inv ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedInvoiceForDetail = inv },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = inv.invoiceNumber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "EWB: ${inv.eWayBillNumber}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = GoldenWheatLight
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (inv.status == "PAID & SETTLED") HarvestGreen.copy(alpha = 0.2f) else WarningOrange.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = inv.status,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (inv.status == "PAID & SETTLED") HarvestGreenLight else WarningOrange
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("From: ${inv.consignorName}", style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("To: ${inv.consigneeName}", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = inv.vehicleNumber,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(
                                    text = "${inv.quantityMt} MT",
                                    fontSize = 12.sp,
                                    color = DarkTextSecondary
                                )
                            }
                            Text(
                                text = "INR ${String.format("%,.2f", inv.totalAmount)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = GoldenWheatLight
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    selectedInvoiceForDetail?.let { inv ->
        val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
        AlertDialog(
            onDismissRequest = { selectedInvoiceForDetail = null },
            title = {
                Text(
                    text = "Consignment Gatepass Manifest",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Date: ${dateFormat.format(Date(inv.dateMillis))}", fontSize = 12.sp, color = DarkTextSecondary)
                    HorizontalDivider()
                    Text("E-Way Bill: ${inv.eWayBillNumber}", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text("Invoice No: ${inv.invoiceNumber}", fontWeight = FontWeight.Bold)
                    Text("MPCB Form 10: ${inv.mpcbForm10Ref}", fontSize = 12.sp, color = DarkTextSecondary)
                    Text("Commodity: ${inv.commodity} (HSN ${inv.hsnCode})", fontSize = 13.sp)
                    Text("Consignor: ${inv.consignorName} (${inv.consignorGstin})", fontSize = 12.sp)
                    Text("Consignee: ${inv.consigneeName} (${inv.consigneeGstin})", fontSize = 12.sp)
                    Text("Vehicle: ${inv.vehicleNumber}", fontWeight = FontWeight.SemiBold)
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Gross Payable:", fontWeight = FontWeight.Bold)
                        Text("INR ${String.format("%,.2f", inv.totalAmount)}", fontWeight = FontWeight.Bold, color = GoldenWheatLight)
                    }
                    Text(
                        text = EWayBillUtils.numberToWords(inv.totalAmount),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = DarkTextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Form GST EWB-01 Transit Manifest\n" +
                                        "EWB: ${inv.eWayBillNumber}\n" +
                                        "Invoice: ${inv.invoiceNumber}\n" +
                                        "Vehicle: ${inv.vehicleNumber}\n" +
                                        "Biomass: ${inv.quantityMt} MT ${inv.commodity}\n" +
                                        "Amount: INR ${String.format("%,.2f", inv.totalAmount)}"
                            )
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Manifest"))
                        selectedInvoiceForDetail = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                ) {
                    Text("Share Manifest")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedInvoiceForDetail = null }) {
                    Text("Close")
                }
            }
        )
    }
}

