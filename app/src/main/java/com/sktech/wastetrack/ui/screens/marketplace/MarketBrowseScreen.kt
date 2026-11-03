package com.sktech.wastetrack.ui.screens.marketplace

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sktech.wastetrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketBrowseScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: MarketBrowseViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val categories = listOf("All", "Cotton", "Bagasse", "Soybean", "Wheat", "Paddy")

    val filteredLots = remember(state.searchQuery, state.selectedCategory, state.lots) {
        state.lots.filter { lot ->
            (state.selectedCategory == "All" || lot.category.equals(state.selectedCategory, ignoreCase = true)) &&
            (state.searchQuery.isBlank() || lot.residueTitle.contains(state.searchQuery, ignoreCase = true) || lot.village.contains(state.searchQuery, ignoreCase = true))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Agricultural Residue Lots", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search by residue type, village, or farmer...") },
                leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = "Search", tint = HarvestGreen) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = state.selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectCategory(cat) },
                        label = { Text(cat, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = HarvestGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredLots, key = { it.id }) { lot ->
                    MarketLotCard(
                        lot = lot,
                        onBidClick = { viewModel.openBidSheet(lot) }
                    )
                }
            }
        }
    }

    if (state.selectedLotForBid != null) {
        val lot = state.selectedLotForBid!!
        AlertDialog(
            onDismissRequest = { viewModel.closeBidSheet() },
            title = {
                Text("Place Procurement Bid", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "${lot.cropName} • ${lot.residueTitle}",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Producer: ${lot.farmerName} (${lot.village}, ${lot.distanceKm} km)",
                        style = MaterialTheme.typography.bodySmall,
                        color = DarkTextSecondary
                    )
                    OutlinedTextField(
                        value = state.bidQuantityInput,
                        onValueChange = { viewModel.updateBidQuantity(it) },
                        label = { Text("Quantity Needed (Metric Tons)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.bidPriceInput,
                        onValueChange = { viewModel.updateBidPrice(it) },
                        label = { Text("Offer Rate per Ton (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (state.isBidSubmitted) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = HarvestGreenContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Bid submitted to farmer successfully!",
                                color = HarvestGreenDark,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(10.dp),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!state.isBidSubmitted) {
                            viewModel.submitBid()
                        } else {
                            viewModel.closeBidSheet()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                ) {
                    Text(if (state.isBidSubmitted) "Done" else "Submit Bid", fontWeight = FontWeight.Bold)
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
private fun MarketLotCard(
    lot: ResidueMarketLot,
    onBidClick: () -> Unit
) {
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
                            Icon(imageVector = Icons.Filled.Recycling, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(18.dp))
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
                        text = "${lot.quantityMt} MT Lot",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = GoldenWheatDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = lot.residueTitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Asking Rate", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    Text(
                        text = "₹${String.format("%,.0f", lot.pricePerTonInr)} / Ton",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = HarvestGreen
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.NearMe, contentDescription = null, tint = DarkTextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${lot.distanceKm} km away", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                }
                Button(
                    onClick = onBidClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Place Bid", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
