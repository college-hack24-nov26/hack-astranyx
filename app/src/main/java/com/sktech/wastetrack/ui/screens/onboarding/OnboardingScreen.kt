package com.sktech.wastetrack.ui.screens.onboarding

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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
    val imageAsset: String,
    val primaryColor: Color,
    val accentColor: Color,
    val previewTag: String
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

    val pages = remember(currentLang) {
        listOf(
            OnboardingPageData(
                title = context.getString(R.string.onb1_title),
                categoryBadge = context.getString(R.string.onb1_badge),
                description = context.getString(R.string.onb1_desc),
                imageAsset = "onboarding_farmer_sugarcane.jpg",
                primaryColor = AgriPrimaryGreen,
                accentColor = AgriMossGreen,
                previewTag = "Maharashtra Sugarcane Cultivation"
            ),
            OnboardingPageData(
                title = context.getString(R.string.onb2_title),
                categoryBadge = context.getString(R.string.onb2_badge),
                description = context.getString(R.string.onb2_desc),
                imageAsset = "onboarding_residue_harvest.jpg",
                primaryColor = AgriEarthyGold,
                accentColor = Color(0xFFD48B28),
                previewTag = "Sugarcane Stubble & Dry Leaves"
            ),
            OnboardingPageData(
                title = context.getString(R.string.onb3_title),
                categoryBadge = context.getString(R.string.onb3_badge),
                description = context.getString(R.string.onb3_desc),
                imageAsset = "onboarding_farmer_smartphone.jpg",
                primaryColor = AgriDeepForest,
                accentColor = AgriPrimaryGreen,
                previewTag = "AgriStock Smart Bidding"
            ),
            OnboardingPageData(
                title = context.getString(R.string.onb4_title),
                categoryBadge = context.getString(R.string.onb4_badge),
                description = context.getString(R.string.onb4_desc),
                imageAsset = "onboarding_biomass_factory.jpg",
                primaryColor = SkyBlueDark,
                accentColor = SkyBlue,
                previewTag = "Biomass Refinery & Processing"
            )
        )
    }

    fun completeOnboarding() {
        val prefs = context.getSharedPreferences("agristock_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("onboarding_completed", true).apply()
        onFinish()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val page = pages[pageIndex]
            val bitmap = remember(page.imageAsset) {
                runCatching {
                    context.assets.open("onboarding/${page.imageAsset}").use {
                        BitmapFactory.decodeStream(it)?.asImageBitmap()
                    }
                }.getOrNull()
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = page.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(page.primaryColor)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colorStops = arrayOf(
                                    0.0f to Color.Black.copy(alpha = 0.55f),
                                    0.25f to Color.Black.copy(alpha = 0.15f),
                                    0.45f to Color.Black.copy(alpha = 0.45f),
                                    0.70f to Color.Black.copy(alpha = 0.82f),
                                    1.0f to Color.Black.copy(alpha = 0.95f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            top = 90.dp,
                            bottom = 150.dp,
                            start = 24.dp,
                            end = 24.dp
                        )
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.Start
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = page.primaryColor.copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = page.categoryBadge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.2.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = page.title,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 36.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = page.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFE2E8F0),
                        lineHeight = 24.sp
                    )


                }
            }
        }

        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
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
                            tint = Color.White
                        )
                        Text(
                            text = when (currentLang) {
                                "hi" -> "हिंदी"
                                "mr" -> "मराठी"
                                else -> "English"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White.copy(alpha = 0.7f)
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
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    TextButton(
                        onClick = { completeOnboarding() },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.White
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.skip),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        Color.White.copy(alpha = 0.35f)
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
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = BorderStroke(
                            1.dp,
                            Color.White.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous",
                                modifier = Modifier.size(18.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Back",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
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
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (pagerState.currentPage < 3) stringResource(R.string.next) else "Get Started",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
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
