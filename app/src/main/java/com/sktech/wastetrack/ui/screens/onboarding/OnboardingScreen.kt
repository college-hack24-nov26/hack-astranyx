package com.sktech.wastetrack.ui.screens.onboarding

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sktech.wastetrack.R
import com.sktech.wastetrack.ui.theme.AgriDeepForest
import com.sktech.wastetrack.ui.theme.AgriEarthyGold
import com.sktech.wastetrack.ui.theme.AgriMossGreen
import com.sktech.wastetrack.ui.theme.AgriPrimaryGreen
import com.sktech.wastetrack.ui.theme.SkyBlue
import com.sktech.wastetrack.ui.theme.SkyBlueDark
import com.sktech.wastetrack.ui.theme.WastetrackTheme
import com.sktech.wastetrack.util.LocaleHelper
import kotlinx.coroutines.launch

private data class OnboardingPageData(
    val title: String,
    val categoryBadge: String,
    val description: String,
    val icon: ImageVector,
    val primaryColor: Color,
    val accentColor: Color,
    val previewTag: String,
    val previewMetric: String,
    val badges: List<String>
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 4 })
    val currentLang by LocaleHelper.currentLanguage.collectAsStateWithLifecycle()
    var showLanguageMenu by remember { mutableStateOf(false) }

    val pages = listOf(
        OnboardingPageData(
            title = "Farm & Crop Intelligence",
            categoryBadge = "PRECISION AGRICULTURE",
            description = "Map and monitor your farm plots digitally. Track crop lifecycles from sowing to harvest with soil-tailored weather advisories and personalized agroplans.",
            icon = Icons.Filled.Agriculture,
            primaryColor = AgriPrimaryGreen,
            accentColor = AgriMossGreen,
            previewTag = "Plot #104 · Sowing Stage",
            previewMetric = "Soil Moisture: 84% · High Health",
            badges = listOf("Plot Mapping", "Soil Health", "Weather Radar")
        ),
        OnboardingPageData(
            title = "AI Crop Residue Valuation",
            categoryBadge = "EDGE AI COMPUTER VISION",
            description = "Transform agricultural waste into profitable inventory. Snap crop residue with your camera to evaluate moisture, quality grade, and discover fair market valuation instantly.",
            icon = Icons.Filled.AutoAwesome,
            primaryColor = AgriEarthyGold,
            accentColor = Color(0xFFD48B28),
            previewTag = "AI Scanner: Wheat Straw",
            previewMetric = "Grade A · Est. ₹1,850 / Quintal",
            badges = listOf("Photo Scan", "Quality Grading", "Instant Price")
        ),
        OnboardingPageData(
            title = "Fair Bidding Marketplace",
            categoryBadge = "ZERO MIDDLEMEN TRADING",
            description = "Directly connect with verified bio-fuel plants, food processors, and compost recyclers. Transparent competitive bidding backed by 100% escrow payment protection.",
            icon = Icons.Filled.Storefront,
            primaryColor = AgriDeepForest,
            accentColor = AgriPrimaryGreen,
            previewTag = "Active Biofuel Escrow",
            previewMetric = "₹42,500 Locked · Bank Verified",
            badges = listOf("Live Bidding", "Escrow Wallet", "Fast Payout")
        ),
        OnboardingPageData(
            title = "Smart Logistics & Gatepass QR",
            categoryBadge = "DISPATCH & AUDIT VERIFICATION",
            description = "Automate vehicle dispatch and tracking with certified logistics drivers. Generate tamper-proof cryptographic QR gatepasses for instantaneous weighbridge clearance.",
            icon = Icons.Filled.LocalShipping,
            primaryColor = SkyBlueDark,
            accentColor = SkyBlue,
            previewTag = "Gatepass #GP-8490",
            previewMetric = "Weighbridge Handshake: Ready",
            badges = listOf("Driver Dispatch", "QR Gatepass", "GPS Routing")
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

                if (pagerState.currentPage < 3) {
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
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    OnboardingImagePlaceholder(
                        page = page,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = page.primaryColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = page.categoryBadge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = page.primaryColor,
                            letterSpacing = 1.2.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
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

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = page.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 23.sp,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        page.badges.forEach { badge ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = androidx.compose.foundation.BorderStroke(
                                    0.8.dp,
                                    MaterialTheme.colorScheme.outlineVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
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
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        val isSelected = pagerState.currentPage == index
                        val width by animateDpAsState(
                            targetValue = if (isSelected) 30.dp else 8.dp,
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
                                .clickable {
                                    scope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnimatedVisibility(
                        visible = pagerState.currentPage > 0,
                        enter = fadeIn() + expandHorizontally(),
                        exit = fadeOut() + shrinkHorizontally(),
                        modifier = Modifier.weight(if (pagerState.currentPage == 3) 0.35f else 0.4f)
                    ) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Previous",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Back",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (pagerState.currentPage < 3) {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                completeOnboarding()
                            }
                        },
                        modifier = Modifier
                            .weight(if (pagerState.currentPage == 0) 1f else if (pagerState.currentPage == 3) 0.65f else 0.6f)
                            .height(54.dp),
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
                                text = if (pagerState.currentPage < 3) stringResource(R.string.next) else "Get Started",
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
}

@Composable
private fun OnboardingImagePlaceholder(
    page: OnboardingPageData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            page.primaryColor.copy(alpha = 0.25f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            page.primaryColor.copy(alpha = 0.12f),
                            page.accentColor.copy(alpha = 0.04f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    page.primaryColor.copy(alpha = 0.35f),
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
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp,
                            page.primaryColor.copy(alpha = 0.5f)
                        ),
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(62.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = page.icon,
                                contentDescription = page.title,
                                modifier = Modifier.size(32.dp),
                                tint = page.primaryColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(
                        0.8.dp,
                        MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = page.previewTag,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = page.primaryColor
                        )
                        Text(
                            text = page.previewMetric,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OnboardingScreenPreview() {
    WastetrackTheme {
        OnboardingScreen(onFinish = {})
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OnboardingScreenDarkPreview() {
    WastetrackTheme(darkTheme = true) {
        OnboardingScreen(onFinish = {})
    }
}
