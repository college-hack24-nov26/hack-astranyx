package com.sktech.wastetrack.ui.components.tutorial

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.VolumeOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sktech.wastetrack.R
import com.sktech.wastetrack.domain.agent.AgentLanguage
import com.sktech.wastetrack.ui.theme.*
import com.sktech.wastetrack.util.LocaleHelper
import com.sktech.wastetrack.util.SpeechManager

@Composable
fun FarmerWelcomeDialog(
    farmerName: String,
    onStartTour: () -> Unit,
    onDismiss: () -> Unit,
    speechManager: SpeechManager? = null,
    modifier: Modifier = Modifier
) {
    val currentLang by LocaleHelper.currentLanguage.collectAsStateWithLifecycle()
    val isSpeaking by speechManager?.isSpeaking?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) }

    val agentLang = remember(currentLang) {
        when (currentLang.lowercase()) {
            "mr" -> AgentLanguage.MARATHI
            "hi" -> AgentLanguage.HINDI
            else -> AgentLanguage.ENGLISH
        }
    }

    val welcomeTtsText = stringResource(R.string.farmer_welcome_tts)

    DisposableEffect(Unit) {
        onDispose {
            speechManager?.stopSpeaking()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(horizontal = 20.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .widthIn(max = 480.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 12.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Surface(
                            onClick = {
                                if (isSpeaking) {
                                    speechManager?.stopSpeaking()
                                } else {
                                    speechManager?.speak(
                                        text = welcomeTtsText,
                                        language = agentLang,
                                        utteranceId = "welcome_dialog_tts"
                                    )
                                }
                            },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSpeaking) AgriPrimaryGreen else AgriPrimaryGreen.copy(alpha = 0.1f),
                            border = BorderStroke(
                                1.dp,
                                if (isSpeaking) AgriPrimaryGreen else AgriPrimaryGreen.copy(alpha = 0.25f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Outlined.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = stringResource(if (isSpeaking) R.string.tutorial_stop_audio else R.string.tutorial_listen),
                                    tint = if (isSpeaking) Color.White else AgriPrimaryGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = stringResource(if (isSpeaking) R.string.tutorial_stop_audio else R.string.tutorial_listen),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSpeaking) Color.White else AgriDeepForest
                                )
                            }
                        }


                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = stringResource(R.string.close),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))


                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        AgriPrimaryGreen.copy(alpha = 0.15f),
                                        AgriPrimaryGreen.copy(alpha = 0.05f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Agriculture,
                            contentDescription = null,
                            tint = AgriPrimaryGreen,
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))


                    val displayName = farmerName.trim().ifBlank { "शेतकरी" }
                    Text(
                        text = stringResource(R.string.farmer_welcome_title, displayName),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(R.string.farmer_welcome_subtitle),
                        style = MaterialTheme.typography.labelLarge,
                        color = AgriPrimaryGreen,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.farmer_welcome_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))


                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CleanFeatureItem(
                            icon = Icons.Filled.DocumentScanner,
                            iconColor = AgriPrimaryGreen,
                            title = stringResource(R.string.farmer_welcome_feat_1),
                            description = stringResource(R.string.farmer_welcome_feat_1_desc)
                        )
                        CleanFeatureItem(
                            icon = Icons.Filled.TrendingUp,
                            iconColor = AgriEarthyGold,
                            title = stringResource(R.string.farmer_welcome_feat_2),
                            description = stringResource(R.string.farmer_welcome_feat_2_desc)
                        )
                        CleanFeatureItem(
                            icon = Icons.Filled.WbSunny,
                            iconColor = Color(0xFF3B82F6),
                            title = stringResource(R.string.farmer_welcome_feat_3),
                            description = stringResource(R.string.farmer_welcome_feat_3_desc)
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))


                    Button(
                        onClick = {
                            speechManager?.stopSpeaking()
                            onStartTour()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AgriPrimaryGreen,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(R.string.start_farmer_tour),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))


                    TextButton(
                        onClick = {
                            speechManager?.stopSpeaking()
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.skip_tour),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CleanFeatureItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
