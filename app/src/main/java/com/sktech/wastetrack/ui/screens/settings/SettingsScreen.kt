package com.sktech.wastetrack.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sktech.wastetrack.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    onLogoutSuccess: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    val languages = listOf("English", "मराठी", "हिंदी")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Profile & Settings", fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val user = state.currentUser
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = HarvestGreenContainer,
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = user?.name?.take(1) ?: "U",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = HarvestGreenDark
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = user?.name ?: "AgriStock User", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text(text = user?.phone ?: "+91 98220 00000", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                            if (!user?.organizationName.isNullOrBlank()) {
                                Text(text = user!!.organizationName, style = MaterialTheme.typography.labelSmall, color = HarvestGreen)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = HarvestGreenContainer
                        ) {
                            Text(
                                text = user?.role?.displayName ?: "Farmer",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = HarvestGreenDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Verified, contentDescription = null, tint = HarvestGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("KYC Verification Status", style = MaterialTheme.typography.bodySmall)
                        }
                        Text("Verified Producer", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = HarvestGreen)
                    }
                }
            }

            Text("Security & Authentication", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Fingerprint, contentDescription = null, tint = HarvestGreen)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Biometric Lock & Gatepass Authorization", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text("Fingerprint / Face ID authentication", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                            }
                        }
                        Switch(
                            checked = state.isBiometricEnabled,
                            onCheckedChange = { viewModel.toggleBiometrics(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = HarvestGreen)
                        )
                    }
                }
            }

            Text("Preferences & Localization", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Column {
                        Text("Display Language", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            languages.forEach { lang ->
                                val isSelected = state.selectedLanguage == lang
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.selectLanguage(lang) },
                                    label = { Text(lang, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = HarvestGreen,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.Notifications, contentDescription = null, tint = HarvestGreen)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Mandi Price & Auction Alerts", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                Text("Real-time SMS & push alerts", style = MaterialTheme.typography.labelSmall, color = DarkTextSecondary)
                            }
                        }
                        Switch(
                            checked = state.isNotificationsEnabled,
                            onCheckedChange = { viewModel.toggleNotifications(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = HarvestGreen)
                        )
                    }
                }
            }

            Text("App & Platform Architecture", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Application", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                        Text("AgriStock (WasteTrack Engine)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Build Edition", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                        Text("Team AstraNyx Hackathon 2026", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Local Persistence", style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
                        Text("Room SQLite Offline First", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = HarvestGreen)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out of Session", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Confirm Sign Out", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to sign out from your active AgriStock session?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout(onSuccess = onLogoutSuccess)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sign Out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
