package com.sktech.wastetrack.ui.screens.order

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sktech.wastetrack.domain.model.OrderItem
import com.sktech.wastetrack.domain.model.OrderStatus
import com.sktech.wastetrack.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderListScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToTracking: (String) -> Unit = {},
    viewModel: OrderListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val filterTabs = listOf("All", "Escrow Locked", "In Transit", "Delivered", "Settled")

    val filteredOrders = remember(state.orders, state.selectedFilter) {
        when (state.selectedFilter) {
            "Escrow Locked" -> state.orders.filter { it.status == OrderStatus.ESCROW_LOCKED }
            "In Transit" -> state.orders.filter { it.status == OrderStatus.IN_TRANSIT }
            "Delivered" -> state.orders.filter { it.status == OrderStatus.DELIVERED }
            "Settled" -> state.orders.filter { it.status == OrderStatus.SETTLED }
            else -> state.orders
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Procurement Contracts", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Biomass Orders & Escrow Ledger", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                EscrowOverviewCard(
                    totalLocked = state.totalEscrowLocked,
                    totalMt = state.totalProcuredMt
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filterTabs) { tab ->
                        val isSelected = state.selectedFilter == tab
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectFilter(tab) },
                            label = { Text(tab, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HarvestGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            items(filteredOrders, key = { it.id }) { order ->
                OrderContractCard(
                    order = order,
                    onReleaseEscrow = { viewModel.openPayoutConfirmation(order) },
                    onTrackShipment = { onNavigateToTracking(order.id) }
                )
            }
        }
    }

    if (state.selectedOrderForPayout != null) {
        val order = state.selectedOrderForPayout!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissPayoutConfirmation() },
            title = {
                Text("Release Escrow Payment", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Dual-weighbridge verification has confirmed delivery of ${order.quantityMt} MT. Releasing funds will instantly credit the farmer.",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = HarvestGreenLight.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Payee Farmer:", style = MaterialTheme.typography.labelSmall)
                                Text(order.sellerFarmerName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Contract Value:", style = MaterialTheme.typography.labelSmall)
                                Text(String.format(Locale.US, "?%,.0f", order.totalAmount), fontWeight = FontWeight.Bold, color = HarvestGreenDark, fontSize = 14.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Smart Contract Hash:", style = MaterialTheme.typography.labelSmall)
                                Text(order.contractHash, style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmEscrowRelease() },
                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                ) {
                    Text("Disburse ?${String.format(Locale.US, "%,.0f", order.totalAmount)}")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissPayoutConfirmation() }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (state.showPayoutSuccessDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissSuccessDialog() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null, tint = HarvestGreen)
                    Text("Escrow Released", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "Tripartite escrow funds have been successfully settled to the farmer bank account via instant clearing.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissSuccessDialog() },
                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                ) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
private fun EscrowOverviewCard(
    totalLocked: Double,
    totalMt: Double
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
                            Icon(imageVector = Icons.Filled.Shield, contentDescription = null, tint = HarvestGreenSoft, modifier = Modifier.size(20.dp))
                        }
                    }
                    Column {
                        Text("Escrow Capital Vault", fontWeight = FontWeight.Bold, color = HarvestGreenSoft, fontSize = 15.sp)
                        Text("Tripartite Bank Guaranteed", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = HarvestGreenSoft.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "100% SECURE",
                        color = HarvestGreenSoft,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Escrow Protected Capital", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    Text(
                        text = String.format(Locale.US, "?%,.0f", totalLocked),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.White
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Committed Biomass", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    Text(
                        text = "${String.format(Locale.US, "%,.0f", totalMt)} MT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = GoldenWheatLight
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderContractCard(
    order: OrderItem,
    onReleaseEscrow: () -> Unit,
    onTrackShipment: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(order.orderNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(dateFormat.format(Date(order.createdAtMillis)), style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                }

                Surface(
                    color = when (order.status) {
                        OrderStatus.ESCROW_LOCKED -> SkyBlueContainer
                        OrderStatus.IN_TRANSIT -> GoldenWheatContainer
                        OrderStatus.DELIVERED -> HarvestGreenContainer
                        OrderStatus.SETTLED -> Color(0xFFE8F8F5)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = order.status.displayName,
                        color = when (order.status) {
                            OrderStatus.ESCROW_LOCKED -> SkyBlueDark
                            OrderStatus.IN_TRANSIT -> GoldenWheatDark
                            OrderStatus.DELIVERED -> HarvestGreenDark
                            OrderStatus.SETTLED -> Color(0xFF117A65)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text(text = order.residueTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp)

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Quantity", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                        Text("${order.quantityMt} MT", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Column {
                        Text("Rate / MT", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                        Text(String.format(Locale.US, "?%,.0f", order.pricePerMt), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total Contract Value", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                        Text(String.format(Locale.US, "?%,.0f", order.totalAmount), fontWeight = FontWeight.Bold, color = HarvestGreenDark, fontSize = 15.sp)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Filled.Person, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(16.dp))
                    Text(order.sellerFarmerName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Filled.LocalShipping, contentDescription = null, tint = DarkTextSecondary, modifier = Modifier.size(16.dp))
                    Text(order.vehicleNumber, style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (order.status) {
                    OrderStatus.DELIVERED -> {
                        Button(
                            onClick = onReleaseEscrow,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                        ) {
                            Icon(imageVector = Icons.Filled.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Release Escrow Payment (Weighed)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    OrderStatus.IN_TRANSIT -> {
                        OutlinedButton(
                            onClick = onTrackShipment,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Track Logistics Corridor", fontSize = 12.sp)
                        }
                    }
                    OrderStatus.SETTLED -> {
                        Surface(
                            color = HarvestGreenLight.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null, tint = HarvestGreenDark, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Payment Cleared to Farmer UPI", color = HarvestGreenDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                    else -> {
                        OutlinedButton(
                            onClick = onTrackShipment,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("View Contract Terms & GatePass", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
