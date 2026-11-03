package com.sktech.wastetrack.ui.screens.onboarding

import android.app.Activity
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sktech.wastetrack.R
import com.sktech.wastetrack.ui.theme.*
import com.sktech.wastetrack.util.LocaleHelper
import kotlinx.coroutines.launch

private data class OnboardingPageData(
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val primaryColor: Color,
    val accentColor: Color,
    val badges: List<String>
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })
    val currentLang by LocaleHelper.currentLanguage.collectAsStateWithLifecycle()
    var showLanguageMenu by remember { mutableStateOf(false) }

    val pages = listOf(
        OnboardingPageData(
            title = "Farm & Crop Intelligence",
            subtitle = stringResource(R.string.precision_agriculture_badge),
            description = "Map and monitor your farm plots. Track crop lifecycles from sowing to harvest with soil-tailored weather advisories and agroplans.",
            icon = Icons.Filled.Agriculture,
            primaryColor = AgriPrimaryGreen,
            accentColor = AgriMossGreen,
            badges = listOf("Plot Mapping", "Soil Health", "Weather Radar")
        ),
        OnboardingPageData(
            title = stringResource(R.string.ai_crop_waste_valuation),
            subtitle = stringResource(R.string.instant_edge_ai_badge),
            description = "Transform agricultural waste into valuable stock. Snap crop residue with your camera to evaluate moisture, quality grade, and real-time market value.",
            icon = Icons.Filled.AutoAwesome,
            primaryColor = AgriEarthyGold,
            accentColor = Color(0xFFD48B28),
            badges = listOf("Photo Scan", "Quality Grading", "Indicative Pricing")
        ),
        OnboardingPageData(
            title = stringResource(R.string.fair_bidding_marketplace),
            subtitle = stringResource(R.string.zero_middlemen_label),
            description = "Directly connect with certified food processors, biofuel refiners, and compost makers. Transparent bidding, escrow payments, and verifiable QR handshakes.",
            icon = Icons.Filled.Storefront,
            primaryColor = AgriDeepForest,
            accentColor = AgriPrimaryGreen,
            badges = listOf("Live Bidding", "Escrow Wallet", "QR Handshake")
        )
    )

    fun completeOnboarding() {
        val prefs = context.getSharedPreferences("agristock_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("onboarding_completed", true).apply()
        onFinish()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.clip(RoundedCornerShape(20.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable { showLanguageMenu = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Filled.Language,
                                contentDescription = stringResource(R.string.cd_language),
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = when (currentLang) {
                                    "hi" -> "हिंदी"
                                    "mr" -> "मराठी"
                                    else -> "English"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showLanguageMenu,
                        onDismissRequest = { showLanguageMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("English (EN)") },
                            onClick = {
                                LocaleHelper.setLanguage(context, "en")
                                showLanguageMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("हिंदी (Hindi)") },
                            onClick = {
                                LocaleHelper.setLanguage(context, "hi")
                                showLanguageMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("मराठी (Marathi)") },
                            onClick = {
                                LocaleHelper.setLanguage(context, "mr")
                                showLanguageMenu = false
                            }
                        )
                    }
                }

                if (pagerState.currentPage < 2) {
                    TextButton(
                        onClick = { completeOnboarding() },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.skip),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                val page = pages[pageIndex]

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        page.primaryColor.copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(2.dp, page.primaryColor.copy(alpha = 0.4f)),
                            shadowElevation = 8.dp,
                            modifier = Modifier.size(110.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = page.icon,
                                    contentDescription = page.title,
                                    modifier = Modifier.size(54.dp),
                                    tint = page.primaryColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = page.primaryColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = page.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = page.primaryColor,
                            letterSpacing = 1.2.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = page.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = page.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        page.badges.forEach { badge ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(page.primaryColor, CircleShape)
                                    )
                                    Text(
                                        text = badge,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        val isSelected = pagerState.currentPage == index
                        val width by animateDpAsState(
                            targetValue = if (isSelected) 28.dp else 8.dp,
                            label = "indicator_width"
                        )
                        val color = if (isSelected) {
                            pages[pagerState.currentPage].primaryColor
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        }

                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(width)
                                .clip(RoundedCornerShape(4.dp))
                                .background(color)
                        )
                    }
                }

                Button(
                    onClick = {
                        if (pagerState.currentPage < 2) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            completeOnboarding()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = pages[pagerState.currentPage].primaryColor,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (pagerState.currentPage < 2) stringResource(R.string.next) else "Get Started with AgriStock",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
