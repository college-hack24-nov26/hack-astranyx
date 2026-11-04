package com.sktech.wastetrack.ui.screens.analytics

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sktech.wastetrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmProfitLossScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMandiTrends: () -> Unit,
    viewModel: FarmProfitLossViewModel = hiltViewModel()
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
                            text = "Farm Profit & Loss Ledger",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Input Expenses vs Stubble Valorization",
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
                    IconButton(onClick = onNavigateToMandiTrends) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ShowChart,
                            contentDescription = "Mandi Trends",
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
                onClick = { viewModel.openAddDialog() },
                containerColor = HarvestGreen,
                contentColor = Color.White,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Record Expense", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        val ledger = state.ledger

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
                    border = BorderStroke(1.dp, HarvestGreenSoft.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Net Farm Take-Home Profit",
                                    color = HarvestGreenLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "INR ${String.format("%,.0f", ledger.netProfit)}",
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
                                        imageVector = Icons.Filled.AccountBalance,
                                        contentDescription = null,
                                        tint = GoldenWheatLight,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = HarvestGreenSoft.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Gross Crop Sale", color = DarkTextSecondary, fontSize = 11.sp)
                                Text("INR ${String.format("%,.0f", ledger.primaryHarvestRevenue)}", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 13.sp)
                            }
                            Column {
                                Text("Stubble Valorization", color = DarkTextSecondary, fontSize = 11.sp)
                                Text("+INR ${String.format("%,.0f", ledger.stubbleResidueRevenue)}", fontWeight = FontWeight.Bold, color = GoldenWheatLight, fontSize = 13.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Total Expenses", color = DarkTextSecondary, fontSize = 11.sp)
                                Text("-INR ${String.format("%,.0f", ledger.totalCost)}", fontWeight = FontWeight.Bold, color = WarningOrange, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = HarvestGreen.copy(alpha = 0.25f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Stubble Extra Income Share:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                                Text(
                                    text = "+${String.format("%.1f", ledger.stubbleRevenueSharePercent)}% Extra Profit Boost",
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
                    text = "Operational Input Cost Ledger (${ledger.expenses.size} Entries)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(ledger.expenses) { exp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = exp.category,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HarvestGreenLight
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = exp.description,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Text(
                                text = exp.dateText,
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkTextSecondary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "INR ${String.format("%,.0f", exp.amountInr)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = WarningOrange
                            )
                            IconButton(onClick = { viewModel.deleteExpense(exp.id) }) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = DarkTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    if (state.showAddDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeAddDialog() },
            title = {
                Text("Record Farm Input Expense", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Select Expense Category:", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(state.availableCategories) { cat ->
                            FilterChip(
                                selected = state.selectedCategory == cat,
                                onClick = { viewModel.updateCategory(cat) },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = state.descriptionInput,
                        onValueChange = { viewModel.updateDescription(it) },
                        label = { Text("Expense Details (e.g. 2 Bags DAP Fertilizer)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = state.amountInput,
                        onValueChange = { viewModel.updateAmount(it) },
                        label = { Text("Amount (INR)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.addExpense() },
                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                ) {
                    Text("Record Expense")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeAddDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

