package com.sktech.wastetrack.ui.screens.notifications

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sktech.wastetrack.domain.model.notification.AgroNotification
import com.sktech.wastetrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMarket: () -> Unit = {},
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedNotifForDetail by remember { mutableStateOf<AgroNotification?>(null) }

    BackHandler {
        onNavigateBack()
    }

    state.message?.let { msg ->
        LaunchedEffect(msg) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Agro-Subsidies & MPCB Alerts",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Government Schemes • Regulatory Bulletins",
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
                    if (state.unreadCount > 0) {
                        TextButton(onClick = { viewModel.markAllAsRead() }) {
                            Text("Mark All Read", fontSize = 12.sp, color = HarvestGreen, fontWeight = FontWeight.Bold)
                        }
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
                                    text = "Government Direct Subsidies (DBT)",
                                    color = HarvestGreenLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "INR ${String.format("%,.0f", state.activeSubsidySum)} Pool",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
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
                                        imageVector = Icons.Filled.NotificationsActive,
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
                                Text("MPCB Compliance", color = DarkTextSecondary, fontSize = 11.sp)
                                Text("100% Zero-Burn Verified", fontWeight = FontWeight.Bold, color = HarvestGreenLight, fontSize = 12.sp)
                            }
                            Column {
                                Text("Unread Broadcasts", color = DarkTextSecondary, fontSize = 11.sp)
                                Text("${state.unreadCount} Pending", fontWeight = FontWeight.Bold, color = if (state.unreadCount > 0) GoldenWheatLight else Color.White, fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("DBT Schemes", color = DarkTextSecondary, fontSize = 11.sp)
                                Text("SMAM + CRM Open", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val filterOptions = listOf(
                        "ALL" to "All Alerts",
                        "GOVT_SUBSIDY" to "Govt Subsidies",
                        "MPCB_COMPLIANCE" to "MPCB Bulletins",
                        "PRICE_SURGE" to "Mandi Spikes",
                        "UNREAD" to "Unread (${state.unreadCount})"
                    )
                    items(filterOptions) { opt ->
                        FilterChip(
                            selected = state.selectedFilter == opt.first,
                            onClick = { viewModel.setFilter(opt.first) },
                            label = { Text(opt.second, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = HarvestGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            items(state.filteredList) { notif ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.markAsRead(notif.id)
                            selectedNotifForDetail = notif
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (notif.isRead) DarkCard else DarkCard.copy(alpha = 0.95f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (notif.isRead) MaterialTheme.colorScheme.outline.copy(alpha = 0.15f) else HarvestGreenLight.copy(alpha = 0.5f)
                    )
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
                                color = when (notif.category) {
                                    "GOVT_SUBSIDY" -> GoldenWheat.copy(alpha = 0.25f)
                                    "MPCB_COMPLIANCE" -> ErrorRed.copy(alpha = 0.25f)
                                    "PRICE_SURGE" -> HarvestGreen.copy(alpha = 0.25f)
                                    else -> SkyBlue.copy(alpha = 0.25f)
                                }
                            ) {
                                Text(
                                    text = notif.urgencyBadge,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (notif.category) {
                                        "GOVT_SUBSIDY" -> GoldenWheatLight
                                        "MPCB_COMPLIANCE" -> ErrorRedLight
                                        "PRICE_SURGE" -> HarvestGreenLight
                                        else -> SkyBlueLight
                                    }
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (!notif.isRead) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(GoldenWheatLight, CircleShape)
                                    )
                                }
                                Text(
                                    text = notif.timestampText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DarkTextSecondary,
                                    fontSize = 11.sp
                                )
                                IconButton(
                                    onClick = { viewModel.dismissNotification(notif.id) },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Dismiss",
                                        tint = DarkTextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = notif.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )

                        Text(
                            text = notif.body,
                            style = MaterialTheme.typography.bodySmall,
                            color = DarkTextSecondary,
                            lineHeight = 18.sp
                        )

                        notif.actionText?.let { action ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        viewModel.markAsRead(notif.id)
                                        if (notif.category == "PRICE_SURGE") {
                                            onNavigateToMarket()
                                        } else {
                                            selectedNotifForDetail = notif
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "$action >",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = HarvestGreenLight
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedNotifForDetail?.let { notif ->
        AlertDialog(
            onDismissRequest = { selectedNotifForDetail = null },
            title = {
                Text(
                    text = notif.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DarkHarvestGreenContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Official Category", color = HarvestGreenLight, fontSize = 11.sp)
                            Text(notif.urgencyBadge, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            notif.subsidyAmount?.let { amt ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Subsidy Cap Grant", color = DarkTextSecondary, fontSize = 11.sp)
                                Text("INR ${String.format("%,.0f", amt)} Direct Benefit Transfer", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GoldenWheatLight)
                            }
                        }
                    }

                    Text(
                        text = notif.body,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Redirecting to MahaDBT Agricultural Portal...", Toast.LENGTH_SHORT).show()
                        selectedNotifForDetail = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HarvestGreen)
                ) {
                    Text("Proceed on MahaDBT")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedNotifForDetail = null }) {
                    Text("Close")
                }
            }
        )
    }
}

