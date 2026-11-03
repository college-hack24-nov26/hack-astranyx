package com.sktech.wastetrack.ui.screens.bid

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sktech.wastetrack.domain.model.BuyerQuoteBid
import com.sktech.wastetrack.domain.model.ResidueAuctionLot
import com.sktech.wastetrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BidMarketScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: BidViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val tabs = listOf("Open Auctions", "My Placed Bids")
    val crops = listOf("All", "Cotton", "Sugarcane", "Soybean", "Wheat")

    val filteredLots = remember(state.auctionLots, state.selectedTab, state.selectedCrop, state.searchQuery, state.myQuotes) {
        state.auctionLots.filter { lot ->
            val matchCrop = state.selectedCrop == "All" || lot.cropName.equals(state.selectedCrop, ignoreCase = true)
            val matchSearch = state.searchQuery.isBlank() || lot.title.contains(state.searchQuery, ignoreCase = true) || lot.village.contains(state.searchQuery, ignoreCase = true)
            val matchTab = if (state.selectedTab == "My Placed Bids") state.myQuotes.containsKey(lot.id) else true
            matchCrop && matchSearch && matchTab
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Residue Procurement Auctions", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Live competitive bidding for biomass supply", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = HarvestGreenContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    BidStatCol("Open Lots", "${state.auctionLots.size} Active")
                    VerticalDivider(modifier = Modifier.height(36.dp), color = HarvestGreen.copy(alpha = 0.2f))
                    BidStatCol("My Quotes", "${state.myQuotes.size} Placed")
                    VerticalDivider(modifier = Modifier.height(36.dp), color = HarvestGreen.copy(alpha = 0.2f))
                    BidStatCol("Winning Bids", "${state.myQuotes.values.count { it.isWinning }} Lots")
                }
            }

            PrimaryTabRow(
                selectedTabIndex = tabs.indexOf(state.selectedTab).coerceAtLeast(0),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = HarvestGreen
            ) {
                tabs.forEach { tab ->
                    Tab(
                        selected = state.selectedTab == tab,
                        onClick = { viewModel.setFilterTab(tab) },
                        text = { Text(tab, fontWeight = if (state.selectedTab == tab) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(crops) { crop ->
                    val isSelected = state.selectedCrop == crop
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectCrop(crop) },
                        label = { Text(crop, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredLots, key = { it.id }) { lot ->
                    AuctionLotCard(
                        lot = lot,
                        myQuote = state.myQuotes[lot.id],
                        onBidClick = { viewModel.openBidSheet(lot) }
                    )
                }
            }
        }
    }

    if (state.selectedLotForBid != null) {
        val lot = state.selectedLotForBid!!
        val price = state.bidPriceInput.toDoubleOrNull() ?: 0.0
        val totalAmount = price * lot.quantityMt

        AlertDialog(
            onDismissRequest = { viewModel.closeBidSheet() },
            title = {
                Text("Submit Procurement Quote", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "${lot.cropName} • ${lot.title}",
                        fontWeight = FontWeight.SemiBold,
                        color = HarvestGreen,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Volume: ${lot.quantityMt} MT • Seller: ${lot.farmerName} (${lot.village})",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Current Highest:", style = MaterialTheme.typography.labelSmall)
                            Text("₹${lot.currentHighestBidPerTon.toInt()}/MT", fontWeight = FontWeight.Bold, color = HarvestGreenDark)
                        }
                    }

                    OutlinedTextField(
                        value = state.bidPriceInput,
                        onValueChange = { viewModel.updateBidPrice(it) },
                        label = { Text("Your Bid Rate (₹ / MT)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = state.pickupDaysInput,
                        onValueChange = { viewModel.updatePickupDays(it) },
                        label = { Text("Proposed Pickup (Days from acceptance)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = HarvestGreenContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Total Procurement Outlay", style = MaterialTheme.typography.labelSmall, color = HarvestGreenDark)
                            Text(
                                text = "₹${String.format("%,.0f", totalAmount)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = HarvestGreenDark
                            )
                        }
                    }

                    if (state.isBidSubmitted) {
                        Text(
                            text = "Bid quote submitted to farmer successfully!",
                            color = HarvestGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!state.isBidSubmitted) {
                            viewModel.submitQuote()
                        } else {
                            viewModel.closeBidSheet()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                ) {
                    Text(if (state.isBidSubmitted) "Done" else "Confirm Bid", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeBidSheet() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun BidStatCol(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = HarvestGreenDark)
    }
}

@Composable
private fun AuctionLotCard(
    lot: ResidueAuctionLot,
    myQuote: BuyerQuoteBid?,
    onBidClick: () -> Unit
) {
    val remainingHours = remember(lot.endTimeMillis) {
        val diff = lot.endTimeMillis - System.currentTimeMillis()
        val h = (diff / (1000 * 3600)).coerceAtLeast(0)
        val m = ((diff / (1000 * 60)) % 60).coerceAtLeast(0)
        "${h}h ${m}m"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = HarvestGreenContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Filled.Gavel, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = lot.cropName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "${lot.farmerName} • ${lot.village}", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = GoldenWheatContainer
                ) {
                    Text(
                        text = "Ends in $remainingHours",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = GoldenWheatDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = lot.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Current High Bid", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    Text(
                        text = "₹${lot.currentHighestBidPerTon.toInt()} / MT",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = HarvestGreen
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Volume", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    Text(text = "${lot.quantityMt} MT", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Total Quotes", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    Text(text = "${lot.totalBidsCount} Bids", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = DarkTextSecondary)
                }
            }

            if (myQuote != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (myQuote.isWinning) HarvestGreenContainer else GoldenWheatContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (myQuote.isWinning) "Your Quote: ₹${myQuote.bidPricePerTon.toInt()}/MT (Highest)" else "Your Quote: ₹${myQuote.bidPricePerTon.toInt()}/MT (Outbid)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (myQuote.isWinning) HarvestGreenDark else GoldenWheatDark
                        )
                        Icon(
                            imageVector = if (myQuote.isWinning) Icons.Filled.CheckCircle else Icons.Filled.Info,
                            contentDescription = null,
                            tint = if (myQuote.isWinning) HarvestGreen else GoldenWheatDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onBidClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
            ) {
                Icon(imageVector = Icons.Filled.LocalAtm, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (myQuote != null) "Raise Quote Bid" else "Place Bid Quote", fontWeight = FontWeight.Bold)
            }
        }
    }
}
