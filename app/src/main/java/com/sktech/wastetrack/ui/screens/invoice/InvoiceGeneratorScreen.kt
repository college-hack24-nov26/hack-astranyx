package com.sktech.wastetrack.ui.screens.invoice

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sktech.wastetrack.domain.model.invoice.AgriHsnCatalog
import com.sktech.wastetrack.domain.model.invoice.AgriHsnItem
import com.sktech.wastetrack.domain.model.invoice.BiomassInvoice
import com.sktech.wastetrack.domain.model.invoice.EWayBillUtils
import com.sktech.wastetrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceGeneratorScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: InvoiceGeneratorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    BackHandler {
        onNavigateBack()
    }

    state.message?.let { msg ->
        LaunchedEffect(msg) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    state.error?.let { err ->
        LaunchedEffect(err) {
            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "GST Biomass E-Way Bill Desk",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Form GST EWB-01 & Statutory Invoice Generator",
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
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = "Invoice History",
                            tint = HarvestGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkHarvestGreenContainer),
                    border = BorderStroke(1.dp, HarvestGreenSoft.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = HarvestGreen.copy(alpha = 0.25f),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.VerifiedUser,
                                    contentDescription = null,
                                    tint = GoldenWheatLight,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Rule 138 National E-Way Bill Compliance",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Valid for inter-state and intra-state agricultural biomass transit over 50,000 INR consignment threshold",
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkTextSecondary
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "1. Consignor & Consignee Registry",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Consignor (Seller Farmer / FPO)",
                            style = MaterialTheme.typography.labelMedium,
                            color = HarvestGreenLight,
                            fontWeight = FontWeight.SemiBold
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val fpos = listOf(
                                Triple("Sinnar Agro FPO", "27AABCU9603R1ZM", "Sinnar MIDC Agro Cluster, Nashik - 422103"),
                                Triple("Niphad Bio-Coop", "27AABCN8812K1Z9", "Niphad Sugar Complex, Nashik - 422303"),
                                Triple("Dindori Cluster", "27AABCD4411Q1ZX", "Dindori Agri Hub, Nashik - 422202")
                            )
                            items(fpos) { fpo ->
                                SuggestionChip(
                                    onClick = { viewModel.updateSeller(fpo.first, fpo.second, fpo.third) },
                                    label = { Text(fpo.first, fontSize = 11.sp) }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = state.sellerName,
                            onValueChange = { viewModel.updateSeller(it, state.sellerGstin, state.sellerAddress) },
                            label = { Text("Consignor Business Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = state.sellerGstin,
                            onValueChange = { viewModel.updateSeller(state.sellerName, it, state.sellerAddress) },
                            label = { Text("Consignor GSTIN (15 Digits)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        Text(
                            text = "Consignee (Buyer Industrial Plant)",
                            style = MaterialTheme.typography.labelMedium,
                            color = GoldenWheatLight,
                            fontWeight = FontWeight.SemiBold
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val buyers = listOf(
                                Triple("Mahindra Bio-Energy", "27AAACM1234L1Z5", "Plot C-14, MIDC Igatpuri - 422403"),
                                Triple("Niphad Co-Gen", "27AAACN5542P1Z1", "Niphad Co-Gen Power Project - 422303"),
                                Triple("Malegaon Fuel Corp", "27AAACM9921C1Z3", "Malegaon Green Fuel Pelletization - 423203")
                            )
                            items(buyers) { b ->
                                SuggestionChip(
                                    onClick = { viewModel.updateBuyer(b.first, b.second, b.third) },
                                    label = { Text(b.first, fontSize = 11.sp) }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = state.buyerName,
                            onValueChange = { viewModel.updateBuyer(it, state.buyerGstin, state.buyerAddress) },
                            label = { Text("Consignee Plant Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = state.buyerGstin,
                            onValueChange = { viewModel.updateBuyer(state.buyerName, it, state.buyerAddress) },
                            label = { Text("Consignee GSTIN") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "2. Commodity HSN Classification & Volume",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Select HSN Commodity Catalog",
                            style = MaterialTheme.typography.labelMedium,
                            color = DarkTextSecondary
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(AgriHsnCatalog.items) { item ->
                                val isSelected = state.selectedHsnItem.hsnCode == item.hsnCode
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.selectHsnItem(item) },
                                    label = {
                                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                            Text("HSN ${item.hsnCode}", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            Text(item.commodityName, fontSize = 12.sp)
                                        }
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = HarvestGreen,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = state.quantityMtText,
                                onValueChange = { viewModel.updateQuantity(it) },
                                label = { Text("Quantity (MT)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                            OutlinedTextField(
                                value = state.ratePerMtText,
                                onValueChange = { viewModel.updateRate(it) },
                                label = { Text("Rate (INR/MT)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "3. Transit Route & Transporter Telemetry",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = state.vehicleNumber,
                            onValueChange = { viewModel.updateTransporter(state.transporterName, state.transporterId, it) },
                            label = { Text("Vehicle Registration No (Truck / Tractor)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = state.transporterName,
                                onValueChange = { viewModel.updateTransporter(it, state.transporterId, state.vehicleNumber) },
                                label = { Text("Transporter Name") },
                                modifier = Modifier.weight(1.2f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = state.approxDistanceKmText,
                                onValueChange = { viewModel.updateDistance(it) },
                                label = { Text("Distance (Km)") },
                                modifier = Modifier.weight(0.8f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = GoldenWheatLight,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Computed E-Way Bill Validity:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DarkTextSecondary
                                    )
                                }
                                Text(
                                    text = "${state.validityHours} Hours (Rule 138)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = GoldenWheatLight
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "4. Statutory Tax & Mandi Ledger Breakdown",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
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
                                Text("Inter-State Consignment", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Apply IGST (5%) instead of CGST+SGST", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                            }
                            Switch(
                                checked = state.isInterState,
                                onCheckedChange = { viewModel.toggleInterState(it) }
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("APMC Mandi Cess (1.5%)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Statutory market fee on agricultural residue", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                            }
                            Switch(
                                checked = state.applyMandiCess,
                                onCheckedChange = { viewModel.toggleMandiCess(it) }
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Sec 206C(1H) TCS (1.0%)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Tax collected at source for high value trade", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                            }
                            Switch(
                                checked = state.applyTcs,
                                onCheckedChange = { viewModel.toggleTcs(it) }
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Taxable Biomass Cargo Value", color = DarkTextSecondary, fontSize = 13.sp)
                                    Text("INR ${String.format("%,.2f", state.taxLedger.taxableAmount)}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                                if (state.isInterState) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("IGST (5.0%)", color = DarkTextSecondary, fontSize = 13.sp)
                                        Text("INR ${String.format("%,.2f", state.taxLedger.igstAmount)}", fontSize = 13.sp)
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("CGST (2.5%) + SGST (2.5%)", color = DarkTextSecondary, fontSize = 13.sp)
                                        Text("INR ${String.format("%,.2f", state.taxLedger.cgstAmount + state.taxLedger.sgstAmount)}", fontSize = 13.sp)
                                    }
                                }
                                if (state.applyMandiCess) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("APMC Mandi Cess (1.5%)", color = DarkTextSecondary, fontSize = 13.sp)
                                        Text("INR ${String.format("%,.2f", state.taxLedger.mandiCessAmount)}", fontSize = 13.sp)
                                    }
                                }
                                if (state.applyTcs) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Sec 206C(1H) TCS (1.0%)", color = DarkTextSecondary, fontSize = 13.sp)
                                        Text("INR ${String.format("%,.2f", state.taxLedger.tcsAmount)}", fontSize = 13.sp)
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Total Buyer Invoice Amount", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        "INR ${String.format("%,.2f", state.taxLedger.totalInvoiceAmount)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = GoldenWheatLight
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Net Direct Farmer Settlement", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        "INR ${String.format("%,.2f", state.taxLedger.netFarmerPayout)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = HarvestGreenLight
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { viewModel.generateOfficialInvoice() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen),
                    enabled = !state.isGenerating
                ) {
                    if (state.isGenerating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.PostAdd, contentDescription = null)
                            Text(
                                text = "Generate Form GST EWB-01 & Invoice",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }

    state.generatedInvoice?.let { inv ->
        AlertDialog(
            onDismissRequest = { viewModel.clearGeneratedInvoice() },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = HarvestGreen
                    )
                    Text("Form GST EWB-01 Generated", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DarkHarvestGreenContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("E-Way Bill Number", color = HarvestGreenLight, fontSize = 11.sp)
                            Text(inv.eWayBillNumber, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tax Invoice Number", color = HarvestGreenLight, fontSize = 11.sp)
                            Text(inv.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GoldenWheatLight)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Vehicle Number", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                            Text(inv.vehicleNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("MPCB Form 10 Ref", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                            Text(inv.mpcbForm10Ref, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Biomass Volume", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                            Text("${inv.quantityMt} MT (${inv.commodity})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Invoice", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                            Text("INR ${String.format("%,.2f", inv.totalAmount)}", fontWeight = FontWeight.Bold, color = GoldenWheatLight, fontSize = 14.sp)
                        }
                    }

                    Text(
                        text = EWayBillUtils.numberToWords(inv.totalAmount),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
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
                                "AgriStock E-Way Bill Pass\n" +
                                        "EWB No: ${inv.eWayBillNumber}\n" +
                                        "Invoice: ${inv.invoiceNumber}\n" +
                                        "Vehicle: ${inv.vehicleNumber}\n" +
                                        "Consignee: ${inv.consigneeName}\n" +
                                        "Biomass: ${inv.quantityMt} MT ${inv.commodity}\n" +
                                        "Amount: INR ${String.format("%,.2f", inv.totalAmount)}"
                            )
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share E-Way Bill Pass"))
                        viewModel.clearGeneratedInvoice()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                ) {
                    Text("Share E-Way Bill")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearGeneratedInvoice() }) {
                    Text("Close")
                }
            }
        )
    }
}

