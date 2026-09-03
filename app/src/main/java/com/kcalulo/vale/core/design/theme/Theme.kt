package com.kcalulo.vale.core.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    onPrimary = Color.White,
    primaryContainer = SoftLavender,
    onPrimaryContainer = PurpleDeep,
    secondary = ValePink,
    onSecondary = Color.White,
    secondaryContainer = PinkSoft,
    onSecondaryContainer = StatusNotWorthIt,
    tertiary = MintGreen,
    onTertiary = Color.White,
    tertiaryContainer = MintSoft,
    onTertiaryContainer = StatusOnTrack,
    background = ValeLight,
    onBackground = NeutralDark,
    surface = Color.White,
    onSurface = NeutralDark,
    surfaceVariant = SoftLavender,
    onSurfaceVariant = NeutralGray,
    outline = LavenderMid,
    outlineVariant = GraySoft,
    error = StatusNotWorthIt,
    onError = Color.White,
    errorContainer = PinkSoft,
    onErrorContainer = StatusNotWorthIt,
    surfaceTint = PrimaryPurple,
    inverseSurface = NeutralDark,
    inverseOnSurface = ValeLight,
    inversePrimary = PurpleBright,
    scrim = NeutralDark,
)

private val DarkColorScheme = darkColorScheme(
    primary = PurpleBright,
    onPrimary = Color.White,
    primaryContainer = LavenderDark,
    onPrimaryContainer = LavenderMid,
    secondary = ValePink,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4A2A3A),
    onSecondaryContainer = Color(0xFFFFB3D1),
    tertiary = MintGreen,
    onTertiary = NeutralDark,
    tertiaryContainer = Color(0xFF1E3B31),
    onTertiaryContainer = Color(0xFF8AE6C3),
    background = DarkBackground,
    onBackground = Color(0xFFF2EFF8),
    surface = DarkSurface,
    onSurface = Color(0xFFF2EFF8),
    surfaceVariant = DarkSurfaceHigh,
    onSurfaceVariant = Color(0xFFB4AFC4),
    outline = Color(0xFF4C4560),
    outlineVariant = Color(0xFF332E44),
    error = Color(0xFFFF8AB8),
    onError = NeutralDark,
    errorContainer = Color(0xFF4A2A3A),
    onErrorContainer = Color(0xFFFFB3D1),
    surfaceTint = PurpleBright,
    inverseSurface = Color(0xFFF2EFF8),
    inverseOnSurface = NeutralDark,
    inversePrimary = PrimaryPurple,
    scrim = Color.Black,
)

@Composable
fun ValeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Dynamic color is intentionally disabled — Vale always wears its own brand purple.
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
