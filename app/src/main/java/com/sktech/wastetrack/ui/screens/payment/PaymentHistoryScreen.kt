package com.sktech.wastetrack.ui.screens.payment

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.sktech.wastetrack.domain.payment.model.PaymentMethod
import com.sktech.wastetrack.domain.payment.model.PaymentRecord
import com.sktech.wastetrack.domain.payment.model.PaymentStatus
import com.sktech.wastetrack.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToInvoice: (String) -> Unit = {},
    viewModel: PaymentHistoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current

    var selectedPaymentForReceipt by remember { mutableStateOf<PaymentRecord?>(null) }
    var showCheckoutSimulator by remember { mutableStateOf(false) }

    LaunchedEffect(state.feedbackMessage) {
        state.feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    BackHandler {
        if (selectedPaymentForReceipt != null) {
            selectedPaymentForReceipt = null
        } else {
            onNavigateBack()
        }
    }

    selectedPaymentForReceipt?.let { payment ->
        PaymentReceiptDialog(
            payment = payment,
            onDismiss = { selectedPaymentForReceipt = null },
            onViewTaxInvoice = {
                selectedPaymentForReceipt = null
                onNavigateToInvoice(payment.orderId)
            }
        )
    }

    if (showCheckoutSimulator) {
        PaymentCheckoutDialog(
            lotTitle = "Chopped Cotton Stubble (50 MT)",
            cropName = "Cotton",
            farmerName = "Suresh Kankaria",
            farmerPhone = "+91 98224 81099",
            buyerName = "Nashik Bio-Energy Grid Ltd",
            buyerGstin = "27AAACN8190K1ZY",
            weightMt = 50.0,
            ratePerMt = 2900.0,
            onConfirmPayment = { method, txnRef, freight, handling, _ ->
                viewModel.addPaymentRecord(
                    cropName = "Cotton",
                    lotTitle = "Chopped Cotton Stubble (50 MT)",
                    farmerName = "Suresh Kankaria",
                    farmerPhone = "+91 98224 81099",
                    buyerName = "Nashik Bio-Energy Grid Ltd",
                    buyerGstin = "27AAACN8190K1ZY",
                    weightMt = 50.0,
                    ratePerMt = 2900.0,
                    freightAmount = freight,
                    handlingCharges = handling,
                    paymentMethod = method,
                    transactionRef = txnRef
                )
            },
            onDismiss = { showCheckoutSimulator = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Payment & Escrow Ledger",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "B2B Biomass Clearing & Settlement",
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
                    IconButton(onClick = { showCheckoutSimulator = true }) {
                        Icon(Icons.Filled.AddCard, contentDescription = "Simulate Checkout", tint = HarvestGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Consolidated Escrow Valuation",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = adaptiveHarvestContainer()
                            ) {
                                Text(
                                    text = "LIVE CLEARING",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = HarvestGreenDark,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FinancialMiniCard(
                                title = "Settled to Farmer",
                                amount = state.summary.totalSettledAmount,
                                color = HarvestGreen,
                                containerColor = adaptiveHarvestContainer(),
                                modifier = Modifier.weight(1f)
                            )
                            FinancialMiniCard(
                                title = "Locked in Escrow",
                                amount = state.summary.totalEscrowLockedAmount,
                                color = Teal,
                                containerColor = adaptiveTealContainer(),
                                modifier = Modifier.weight(1f)
                            )
                            FinancialMiniCard(
                                title = "Freight & Taxes",
                                amount = state.summary.totalFreightAmount + state.summary.totalTaxCollected,
                                color = WarningOrange,
                                containerColor = adaptiveWarningContainer(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = viewModel::setSearchQuery,
                        placeholder = { Text("Search by Lot, UTR, Farmer, Buyer...") },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        },
                        trailingIcon = {
                            if (state.searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    val filterOptions = listOf(
                        "ALL" to "All Transactions",
                        "ESCROW_LOCKED" to "Escrow Protected",
                        "SETTLED" to "Settled",
                        "DIRECT_PAID" to "Direct Paid"
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(filterOptions) { (key, label) ->
                            val isSelected = state.selectedFilter == key
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setFilter(key) },
                                label = { Text(label, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = HarvestGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            if (state.filteredPayments.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.AccountBalanceWallet,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "No payment records match your filter",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Try clearing search queries or simulate a new checkout transaction",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(state.filteredPayments, key = { it.id }) { payment ->
                    PaymentRecordCard(
                        payment = payment,
                        onCopyUtr = { utr ->
                            clipboardManager.setText(AnnotatedString(utr))
                            snackbarHostState.currentSnackbarData?.dismiss()
                        },
                        onReleaseEscrow = {
                            viewModel.releaseEscrow(payment.id)
                        },
                        onViewReceipt = {
                            selectedPaymentForReceipt = payment
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FinancialMiniCard(
    title: String,
    amount: Double,
    color: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "₹%,.0f".format(amount),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun PaymentRecordCard(
    payment: PaymentRecord,
    onCopyUtr: (String) -> Unit,
    onReleaseEscrow: () -> Unit,
    onViewReceipt: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH) }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewReceipt() }
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = payment.id,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = dateFormat.format(Date(payment.createdAtMillis)),
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val statusBadgeColor = when (payment.paymentStatus) {
                    PaymentStatus.ESCROW_LOCKED -> Teal
                    PaymentStatus.SETTLED -> HarvestGreen
                    PaymentStatus.DIRECT_PAID -> SkyBlueDark
                    PaymentStatus.PENDING -> WarningOrange
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBadgeColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, statusBadgeColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = payment.paymentStatus.label.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusBadgeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = payment.lotTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Producer: ${payment.farmerName}  |  Buyer: ${payment.buyerName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Consignment Weight & Tariff",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "%.1f MT @ ₹%,.0f / MT".format(payment.weightMt, payment.ratePerMt),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Net Payable Amount",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "₹%,.0f".format(payment.totalAmount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = HarvestGreenDark
                        )
                    }
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
                    Icon(
                        Icons.Filled.AccountBalance,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = payment.paymentMethod.title,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Ref: ${payment.transactionReference}",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(
                        onClick = { onCopyUtr(payment.transactionReference) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Filled.ContentCopy,
                            contentDescription = "Copy UTR",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onViewReceipt,
                    modifier = Modifier.weight(1f).height(40.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Outlined.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tax Slip", fontSize = 12.sp)
                }

                if (payment.paymentStatus == PaymentStatus.ESCROW_LOCKED) {
                    Button(
                        onClick = onReleaseEscrow,
                        modifier = Modifier.weight(1.3f).height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                    ) {
                        Icon(Icons.Filled.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Release Escrow", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentReceiptDialog(
    payment: PaymentRecord,
    onDismiss: () -> Unit,
    onViewTaxInvoice: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.ENGLISH) }

    val qrPayload = "AGRISTOCK:TAX-MANIFEST:${payment.id}:${payment.orderId}:${payment.transactionReference}:₹%.2f".format(payment.totalAmount)

    val qrBitmap by produceState<Bitmap?>(initialValue = null, payment.id) {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(qrPayload, BarcodeFormat.QR_CODE, 400, 400)
        val bmp = Bitmap.createBitmap(400, 400, Bitmap.Config.RGB_565)
        for (x in 0 until 400) {
            for (y in 0 until 400) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        value = bmp
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Verified, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(24.dp))
                        Text(
                            text = "Statutory Tax & Escrow Slip",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = adaptiveHarvestContainer(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Transaction Receipt ID: ${payment.id}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Dated: ${dateFormat.format(Date(payment.createdAtMillis))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Smart Escrow Vault: ${payment.escrowVaultId}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = HarvestGreenDark
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ReceiptRow("Commodity / Lot", payment.lotTitle)
                        ReceiptRow("Crop Residue Category", "${payment.cropName} (HSN 1213)")
                        ReceiptRow("Consignment Weight", "%.1f MT".format(payment.weightMt))
                        ReceiptRow("Agreed Price / MT", "₹%,.0f".format(payment.ratePerMt))
                        ReceiptRow("Seller / Farmer", "${payment.farmerName} (${payment.farmerPhone})")
                        ReceiptRow("Buyer Company", payment.buyerName)
                        ReceiptRow("Buyer GSTIN", payment.buyerGstin)
                        ReceiptRow("Bank Settlement Rail", payment.paymentMethod.title)
                        ReceiptRow("Bank UTR / Reference", payment.transactionReference, isMono = true)
                    }

                    HorizontalDivider()

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ReceiptRow("Base Agricultural Value", "₹%,.2f".format(payment.materialCost))
                        ReceiptRow("CGST 2.5%", "₹%,.2f".format(payment.gstAmount / 2.0))
                        ReceiptRow("SGST 2.5%", "₹%,.2f".format(payment.gstAmount / 2.0))
                        ReceiptRow("TCS u/s 206C(1) @ 1%", "₹%,.2f".format(payment.tcsAmount))
                        ReceiptRow("Logistics Freight Tariff", "₹%,.2f".format(payment.freightAmount))
                        ReceiptRow("GTA Freight GST 5%", "₹%,.2f".format(payment.transportGst))
                        ReceiptRow("Weighbridge Handling Charges", "₹%,.2f".format(payment.handlingCharges))

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Grand Total Settled",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = HarvestGreenDark
                            )
                            Text(
                                text = "₹%,.2f".format(payment.totalAmount),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = HarvestGreenDark
                            )
                        }
                    }

                    val bmp = qrBitmap
                    if (bmp != null) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.size(140.dp)
                            ) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Tax Manifest QR",
                                    modifier = Modifier.fillMaxSize().padding(6.dp)
                                )
                            }
                            Text(
                                text = "Scan for State GST & E-Way Bill Verification",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onViewTaxInvoice,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                ) {
                    Icon(Icons.Filled.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open Form GST EWB-01 Desk", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String, isMono: Boolean = false) {
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
            fontFamily = if (isMono) FontFamily.Monospace else FontFamily.Default,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

