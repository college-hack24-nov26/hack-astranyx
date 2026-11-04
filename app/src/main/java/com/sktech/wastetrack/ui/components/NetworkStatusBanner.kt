package com.sktech.wastetrack.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sktech.wastetrack.R
import com.sktech.wastetrack.ui.theme.*
import com.sktech.wastetrack.util.NetworkMonitor
import com.sktech.wastetrack.util.NetworkStatus
import kotlinx.coroutines.delay

@Composable
fun NetworkStatusBanner(
    networkMonitor: NetworkMonitor,
    modifier: Modifier = Modifier
) {
    val status by networkMonitor.networkStatus.collectAsState()
    var showRestoredBanner by remember { mutableStateOf(false) }
    var previousStatus by remember { mutableStateOf<NetworkStatus?>(null) }

    LaunchedEffect(status) {
        if (previousStatus is NetworkStatus.Disconnected && status is NetworkStatus.Connected) {
            showRestoredBanner = true
            delay(3000L)
            showRestoredBanner = false
        }
        previousStatus = status
    }

    val isOffline = status is NetworkStatus.Disconnected
    val isCaptive = status is NetworkStatus.CaptivePortal
    val shouldShow = isOffline || isCaptive || showRestoredBanner

    AnimatedVisibility(
        visible = shouldShow,
        enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
        modifier = modifier
    ) {
        when {
            showRestoredBanner -> {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = adaptiveEmeraldContainer(),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = stringResource(R.string.network_online_cd),
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.net_back_online),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }
                }
            }

            isCaptive -> {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = adaptiveOrangeContainer(),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = stringResource(R.string.network_captive_portal_cd),
                            tint = SafetyOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.net_wifi_login_required),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SafetyOrange
                        )
                    }
                }
            }

            isOffline -> {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = adaptiveOrangeContainer(),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CloudOff,
                            contentDescription = stringResource(R.string.network_offline_cd),
                            tint = SafetyOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.net_offline_mode),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SafetyOrange,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}
