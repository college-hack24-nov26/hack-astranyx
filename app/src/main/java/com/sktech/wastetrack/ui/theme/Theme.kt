package com.sktech.wastetrack.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AgriStockDarkScheme = darkColorScheme(
    primary = HarvestGreenSoft,
    onPrimary = Color(0xFF042F1E),
    primaryContainer = DarkHarvestGreenContainer,
    onPrimaryContainer = HarvestGreenSoft,

    secondary = GoldenWheatLight,
    onSecondary = Color(0xFF2A2210),
    secondaryContainer = DarkGoldenWheatContainer,
    onSecondaryContainer = GoldenWheatLight,

    tertiary = SkyBlueLight,
    onTertiary = Color(0xFF0F1E33),
    tertiaryContainer = DarkSkyBlueContainer,
    onTertiaryContainer = SkyBlueLight,

    background = DarkCanvas,
    onBackground = DarkTextPrimary,
    surface = DarkCard,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkCardElevated,
    onSurfaceVariant = DarkTextSecondary,

    error = ErrorRedLight,
    onError = Color.White,
    errorContainer = DarkErrorContainer,
    onErrorContainer = ErrorRedLight,

    outline = DarkCardBorder,
    outlineVariant = DarkCardBorderFocus,
    inverseSurface = LightCard,
    inverseOnSurface = TextPrimary,
    surfaceTint = HarvestGreenSoft,
)

private val AgriStockLightScheme = lightColorScheme(
    primary = HarvestGreen,
    onPrimary = Color.White,
    primaryContainer = HarvestGreenContainer,
    onPrimaryContainer = HarvestGreenDark,

    secondary = GoldenWheat,
    onSecondary = Color(0xFF3A2E1F),
    secondaryContainer = GoldenWheatContainer,
    onSecondaryContainer = GoldenWheatDark,

    tertiary = SkyBlue,
    onTertiary = Color.White,
    tertiaryContainer = SkyBlueContainer,
    onTertiaryContainer = SkyBlueDark,

    background = LightCanvas,
    onBackground = TextPrimary,
    surface = LightCard,
    onSurface = TextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondary,

    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorContainer,
    onErrorContainer = ErrorRed,

    outline = LightCardBorder,
    outlineVariant = LightCardBorderFocus,
    inverseSurface = DarkCard,
    inverseOnSurface = DarkTextPrimary,
    surfaceTint = HarvestGreen,
)

private val DarkColorScheme = AgriStockDarkScheme
private val LightColorScheme = AgriStockLightScheme

@Composable
fun AgriStockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AgriStockDarkScheme else AgriStockLightScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

@Composable
fun WastetrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    AgriStockTheme(darkTheme = darkTheme, content = content)
}
