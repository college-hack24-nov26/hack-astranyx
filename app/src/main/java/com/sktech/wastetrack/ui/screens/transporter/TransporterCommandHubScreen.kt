package com.sktech.wastetrack.ui.screens.transporter

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sktech.wastetrack.domain.model.transporter.FleetVehicle
import com.sktech.wastetrack.domain.model.transporter.StubbleLoad
import com.sktech.wastetrack.domain.model.transporter.TripProfitLedger
import com.sktech.wastetrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransporterCommandHubScreen(
    onNavigateBack: () -> Unit,
    onNavigateToFleetMap: () -> Unit,
    onNavigateToWeighbridge: () -> Unit,
    viewModel: TransporterCommandViewModel = hiltViewModel()
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
                            text = "Fleet Logistics Command Hub",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Load Board | Green Permits | Freight P&L",
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
                    IconButton(onClick = onNavigateToFleetMap) {
                        Icon(
                            imageVector = Icons.Filled.Map,
                            contentDescription = "Corridor Map",
                            tint = HarvestGreen
                        )
                    }
                    IconButton(onClick = onNavigateToWeighbridge) {
                        Icon(
                            imageVector = Icons.Filled.Scale,
                            contentDescription = "Weighbridge Slips",
                            tint = GoldenWheatLight
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            TabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = HarvestGreen
            ) {
                Tab(
                    selected = state.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = { Text("Load Board", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Filled.LocalShipping, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = state.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = { Text("Fleet Roster", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Filled.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = state.selectedTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    text = { Text("Trip P&L", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                    icon = { Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            when (state.selectedTab) {
                0 -> LoadBoardTabContent(
                    loads = state.loads,
                    onOpenBid = { viewModel.openBidding(it) },
                    onInstantAccept = { viewModel.instantAcceptLoad(it.id) }
                )
                1 -> FleetRosterTabContent(
                    vehicles = state.vehicles,
                    onStatusChange = { v, s -> viewModel.updateVehicleStatus(v, s) },
                    onCallDriver = { phone ->
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                        context.startActivity(intent)
                    }
                )
                2 -> TripProfitTabContent(
                    ledgers = state.tripLedgers,
                    onSignPod = { viewModel.signPod(it) },
                    onShareSlip = { trip ->
                        val shareText = "Trip P&L Manifest\n" +
                                "Trip: ${trip.tripId}\n" +
                                "Vehicle: ${trip.vehicleNumber}\n" +
                                "Route: ${trip.route}\n" +
                                "Gross Revenue: INR ${String.format("%,.2f", trip.grossFreightRevenue)}\n" +
                                "Diesel Cost: INR ${String.format("%,.2f", trip.dieselTotalCost)}\n" +
                                "FASTag Tolls: INR ${String.format("%,.2f", trip.fastagTollCost)}\n" +
                                "Net Transporter Profit: INR ${String.format("%,.2f", trip.netProfit)} (${String.format("%.1f", trip.profitMarginPercent)}%)\n" +
                                "e-POD Gate Verification: ${trip.ePodToken}"
                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Trip P&L Slip"))
                    }
                )
            }
        }
    }

    state.biddingLoad?.let { load ->
        AlertDialog(
            onDismissRequest = { viewModel.closeBidding() },
            title = {
                Text("Submit Freight Quote", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${load.tonnageMt} MT ${load.cropResidue}",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = "${load.originHub} -> ${load.destinationPlant} (${load.distanceKm} km)",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary
                    )
                    Text(
                        text = "Benchmark Rate: INR ${load.suggestedFreightRatePerMt} / MT",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldenWheatLight
                    )

                    OutlinedTextField(
                        value = state.bidRateInput,
                        onValueChange = { viewModel.updateBidRate(it) },
                        label = { Text("Your Quote Rate (INR/MT)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    val enteredRate = state.bidRateInput.toDoubleOrNull() ?: 0.0
                    val totalQuoted = enteredRate * load.tonnageMt
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DarkHarvestGreenContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Freight Value:", fontSize = 13.sp, color = HarvestGreenLight)
                            Text(
                                text = "INR ${String.format("%,.2f", totalQuoted)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = GoldenWheatLight
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.submitBid(load.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                ) {
                    Text("Submit Quote")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeBidding() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun LoadBoardTabContent(
    loads: List<StubbleLoad>,
    onOpenBid: (StubbleLoad) -> Unit,
    onInstantAccept: (StubbleLoad) -> Unit
) {
    val totalTonnage = remember(loads) { loads.sumOf { it.tonnageMt } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pending Farm-Gate Stubble Loads",
                            color = HarvestGreenLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${loads.size} Active Haul Contracts",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Total Available Cargo: ${String.format("%.1f", totalTonnage)} MT",
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkTextSecondary
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = HarvestGreen.copy(alpha = 0.3f),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.LocalShipping,
                                contentDescription = null,
                                tint = GoldenWheatLight,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        items(loads) { load ->
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = HarvestGreen.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${load.tonnageMt} MT",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = HarvestGreenLight
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (load.status == "AVAILABLE") GoldenWheat.copy(alpha = 0.2f) else HarvestGreen.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = load.status,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (load.status == "AVAILABLE") GoldenWheatLight else HarvestGreenLight
                            )
                        }
                    }

                    Text(
                        text = load.cropResidue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.Place, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(16.dp))
                        Text(load.originHub, fontSize = 12.sp, color = DarkTextSecondary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.Factory, contentDescription = null, tint = GoldenWheatLight, modifier = Modifier.size(16.dp))
                        Text(load.destinationPlant, fontSize = 12.sp, color = DarkTextSecondary)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Suggested Freight:", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                            Text(
                                text = "INR ${load.suggestedFreightRatePerMt}/MT (${load.distanceKm} km)",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Freight Value:", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                            Text(
                                text = "INR ${String.format("%,.2f", load.totalFreightValue)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = GoldenWheatLight
                            )
                        }
                    }

                    if (load.status == "AVAILABLE") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onOpenBid(load) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Quote / Bid", fontSize = 12.sp)
                            }
                            Button(
                                onClick = { onInstantAccept(load) },
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                            ) {
                                Text("Instant Accept", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FleetRosterTabContent(
    vehicles: List<FleetVehicle>,
    onStatusChange: (String, String) -> Unit,
    onCallDriver: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
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
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Fleet Regulatory & Statutory Status",
                        color = HarvestGreenLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "MPCB Stubble Transit & FASTag Registry",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Vehicles", color = DarkTextSecondary, fontSize = 11.sp)
                            Text("${vehicles.size} Trucks", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column {
                            Text("Green Compliance", color = DarkTextSecondary, fontSize = 11.sp)
                            Text("100% MPCB Valid", fontWeight = FontWeight.Bold, color = HarvestGreenLight)
                        }
                        Column {
                            Text("FASTag Pool", color = DarkTextSecondary, fontSize = 11.sp)
                            val totalFastag = vehicles.sumOf { it.fastagBalance }
                            Text("INR ${String.format("%,.0f", totalFastag)}", fontWeight = FontWeight.Bold, color = GoldenWheatLight)
                        }
                    }
                }
            }
        }

        items(vehicles) { v ->
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                                text = v.vehicleNumber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White
                            )
                            Text(v.modelName, style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (v.status) {
                                "AVAILABLE FOR PICKUP" -> HarvestGreen.copy(alpha = 0.25f)
                                "IN TRANSIT" -> GoldenWheat.copy(alpha = 0.25f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Text(
                                text = v.status,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (v.status) {
                                    "AVAILABLE FOR PICKUP" -> HarvestGreenLight
                                    "IN TRANSIT" -> GoldenWheatLight
                                    else -> Color.White
                                }
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Driver: ${v.driverName}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("FASTag Balance: INR ${String.format("%,.2f", v.fastagBalance)}", fontSize = 12.sp, color = GoldenWheatLight)
                        }
                        IconButton(onClick = { onCallDriver(v.driverPhone) }) {
                            Icon(Icons.Filled.Call, contentDescription = "Call Driver", tint = HarvestGreen)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tare: ${v.tareWeightKg} kg | GVW: ${v.grossVehicleWeightKg} kg", fontSize = 12.sp, color = DarkTextSecondary)
                        Text("Capacity: ${v.payloadCapacityMt} MT", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("MPCB Permit:", fontSize = 11.sp, color = DarkTextSecondary)
                                Text(v.mpcbPermitExpiry, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = HarvestGreenLight)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("PUC Certificate:", fontSize = 11.sp, color = DarkTextSecondary)
                                Text(v.pucExpiry, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (v.pucExpiry.contains("Renewal")) WarningOrange else Color.White)
                            }
                        }
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val statuses = listOf("AVAILABLE FOR PICKUP", "IN TRANSIT", "AT WEIGHBRIDGE", "MAINTENANCE")
                        items(statuses) { st ->
                            FilterChip(
                                selected = v.status == st,
                                onClick = { onStatusChange(v.vehicleNumber, st) },
                                label = { Text(st, fontSize = 10.sp) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TripProfitTabContent(
    ledgers: List<TripProfitLedger>,
    onSignPod: (String) -> Unit,
    onShareSlip: (TripProfitLedger) -> Unit
) {
    val totalRevenue = remember(ledgers) { ledgers.sumOf { it.grossFreightRevenue } }
    val totalProfit = remember(ledgers) { ledgers.sumOf { it.netProfit } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
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
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Freight Operational P&L Ledger",
                                color = HarvestGreenLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "INR ${String.format("%,.2f", totalProfit)} Net Profit",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = GoldenWheatLight
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = HarvestGreen.copy(alpha = 0.3f),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = GoldenWheatLight,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = HarvestGreenSoft.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Gross Billing", color = DarkTextSecondary, fontSize = 11.sp)
                            Text("INR ${String.format("%,.2f", totalRevenue)}", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column {
                            Text("Avg Profit Margin", color = DarkTextSecondary, fontSize = 11.sp)
                            val margin = if (totalRevenue > 0) (totalProfit / totalRevenue) * 100 else 0.0
                            Text("${String.format("%.1f", margin)}%", fontWeight = FontWeight.Bold, color = HarvestGreenLight)
                        }
                        Column {
                            Text("Trips Logged", color = DarkTextSecondary, fontSize = 11.sp)
                            Text("${ledgers.size} Completed", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        items(ledgers) { trip ->
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                                text = trip.tripId,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Text(trip.route, style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = HarvestGreen.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "+INR ${String.format("%,.0f", trip.netProfit)} (${String.format("%.0f", trip.profitMarginPercent)}%)",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = HarvestGreenLight
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Gross Freight Revenue:", fontSize = 12.sp, color = DarkTextSecondary)
                                Text("INR ${String.format("%,.2f", trip.grossFreightRevenue)}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = GoldenWheatLight)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Diesel (${trip.dieselLiters} L @ INR 92.5):", fontSize = 12.sp, color = DarkTextSecondary)
                                Text("-INR ${String.format("%,.2f", trip.dieselTotalCost)}", fontSize = 12.sp, color = WarningOrange)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("FASTag Electronic Tolls:", fontSize = 12.sp, color = DarkTextSecondary)
                                Text("-INR ${String.format("%,.2f", trip.fastagTollCost)}", fontSize = 12.sp, color = WarningOrange)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Driver Batta Allowance:", fontSize = 12.sp, color = DarkTextSecondary)
                                Text("-INR ${String.format("%,.2f", trip.driverBattaAllowance)}", fontSize = 12.sp, color = WarningOrange)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Unloading Labour Charge:", fontSize = 12.sp, color = DarkTextSecondary)
                                Text("-INR ${String.format("%,.2f", trip.unloadingFee)}", fontSize = 12.sp, color = WarningOrange)
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Net Freight Margin:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    "INR ${String.format("%,.2f", trip.netProfit)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = HarvestGreenLight
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = if (trip.isPodSigned) Icons.Filled.CheckCircle else Icons.Filled.PendingActions,
                                contentDescription = null,
                                tint = if (trip.isPodSigned) HarvestGreenLight else WarningOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (trip.isPodSigned) "e-POD Gate Sign-off OK" else "e-POD Pending Sign-off",
                                fontSize = 12.sp,
                                color = if (trip.isPodSigned) HarvestGreenLight else WarningOrange
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (!trip.isPodSigned) {
                                TextButton(onClick = { onSignPod(trip.tripId) }) {
                                    Text("Sign e-POD", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            IconButton(onClick = { onShareSlip(trip) }) {
                                Icon(Icons.Filled.Share, contentDescription = "Share", tint = HarvestGreen, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

