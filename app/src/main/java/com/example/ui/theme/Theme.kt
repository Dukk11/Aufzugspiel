package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

enum class AppThemeMode(val label: String, val emoji: String) {
    SYSTEM("System", "🌓"),
    DARK("Dunkel", "🌙"),
    LIGHT("Hell", "☀️")
}

@Immutable
data class StudioExtendedColors(
    val isDark: Boolean,
    val emerald: Color,
    val gold: Color,
    val sky: Color,
    val coral: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    val glassBackdrop: Color,
    val hairlineBorder: Color
)

val LocalStudioColors = staticCompositionLocalOf {
    StudioExtendedColors(
        isDark = true,
        emerald = StudioEmeraldDark,
        gold = StudioGoldDark,
        sky = StudioSkyDark,
        coral = StudioCoralDark,
        surfaceContainerLowest = StudioDarkSurfaceContainerLowest,
        surfaceContainerLow = StudioDarkSurfaceContainerLow,
        surfaceContainer = StudioDarkSurfaceContainer,
        surfaceContainerHigh = StudioDarkSurfaceContainerHigh,
        surfaceContainerHighest = StudioDarkSurfaceContainerHighest,
        glassBackdrop = Color(0xCC0D1119),
        hairlineBorder = StudioDarkOutlineVariant
    )
}

object StudioTheme {
    val colors: StudioExtendedColors
        @Composable
        get() = LocalStudioColors.current
}

private val StudioDarkColorScheme: ColorScheme = darkColorScheme(
    primary = StudioDarkPrimary,
    onPrimary = StudioDarkOnPrimary,
    primaryContainer = StudioDarkPrimaryContainer,
    onPrimaryContainer = StudioDarkOnPrimaryContainer,
    secondary = StudioDarkSecondary,
    onSecondary = StudioDarkOnSecondary,
    secondaryContainer = StudioDarkSecondaryContainer,
    onSecondaryContainer = StudioDarkOnSecondaryContainer,
    tertiary = StudioDarkTertiary,
    onTertiary = StudioDarkOnTertiary,
    tertiaryContainer = StudioDarkTertiaryContainer,
    onTertiaryContainer = StudioDarkOnTertiaryContainer,
    background = StudioDarkBackground,
    onBackground = StudioDarkOnBackground,
    surface = StudioDarkSurface,
    onSurface = StudioDarkOnSurface,
    surfaceVariant = StudioDarkSurfaceVariant,
    onSurfaceVariant = StudioDarkOnSurfaceVariant,
    outline = StudioDarkOutline,
    outlineVariant = StudioDarkOutlineVariant,
    surfaceContainerLowest = StudioDarkSurfaceContainerLowest,
    surfaceContainerLow = StudioDarkSurfaceContainerLow,
    surfaceContainer = StudioDarkSurfaceContainer,
    surfaceContainerHigh = StudioDarkSurfaceContainerHigh,
    surfaceContainerHighest = StudioDarkSurfaceContainerHighest
)

private val StudioLightColorScheme: ColorScheme = lightColorScheme(
    primary = StudioLightPrimary,
    onPrimary = StudioLightOnPrimary,
    primaryContainer = StudioLightPrimaryContainer,
    onPrimaryContainer = StudioLightOnPrimaryContainer,
    secondary = StudioLightSecondary,
    onSecondary = StudioLightOnSecondary,
    secondaryContainer = StudioLightSecondaryContainer,
    onSecondaryContainer = StudioLightOnSecondaryContainer,
    tertiary = StudioLightTertiary,
    onTertiary = StudioLightOnTertiary,
    tertiaryContainer = StudioLightTertiaryContainer,
    onTertiaryContainer = StudioLightOnTertiaryContainer,
    background = StudioLightBackground,
    onBackground = StudioLightOnBackground,
    surface = StudioLightSurface,
    onSurface = StudioLightOnSurface,
    surfaceVariant = StudioLightSurfaceVariant,
    onSurfaceVariant = StudioLightOnSurfaceVariant,
    outline = StudioLightOutline,
    outlineVariant = StudioLightOutlineVariant,
    surfaceContainerLowest = StudioLightSurfaceContainerLowest,
    surfaceContainerLow = StudioLightSurfaceContainerLow,
    surfaceContainer = StudioLightSurfaceContainer,
    surfaceContainerHigh = StudioLightSurfaceContainerHigh,
    surfaceContainerHighest = StudioLightSurfaceContainerHighest
)

val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val useDarkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> systemDark
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDarkTheme -> StudioDarkColorScheme
        else -> StudioLightColorScheme
    }

    val extendedColors = if (useDarkTheme) {
        StudioExtendedColors(
            isDark = true,
            emerald = StudioEmeraldDark,
            gold = StudioGoldDark,
            sky = StudioSkyDark,
            coral = StudioCoralDark,
            surfaceContainerLowest = colorScheme.surfaceContainerLowest,
            surfaceContainerLow = colorScheme.surfaceContainerLow,
            surfaceContainer = colorScheme.surfaceContainer,
            surfaceContainerHigh = colorScheme.surfaceContainerHigh,
            surfaceContainerHighest = colorScheme.surfaceContainerHighest,
            glassBackdrop = colorScheme.surfaceContainer.copy(alpha = 0.90f),
            hairlineBorder = colorScheme.outlineVariant
        )
    } else {
        StudioExtendedColors(
            isDark = false,
            emerald = StudioEmeraldLight,
            gold = StudioGoldLight,
            sky = StudioSkyLight,
            coral = StudioCoralLight,
            surfaceContainerLowest = colorScheme.surfaceContainerLowest,
            surfaceContainerLow = colorScheme.surfaceContainerLow,
            surfaceContainer = colorScheme.surfaceContainer,
            surfaceContainerHigh = colorScheme.surfaceContainerHigh,
            surfaceContainerHighest = colorScheme.surfaceContainerHighest,
            glassBackdrop = colorScheme.surfaceContainerLowest.copy(alpha = 0.94f),
            hairlineBorder = colorScheme.outlineVariant
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDarkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !useDarkTheme
            }
        }
    }

    CompositionLocalProvider(LocalStudioColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = ExpressiveShapes,
            content = content
        )
    }
}
