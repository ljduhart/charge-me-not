package com.artie.chargemenot.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightMeadowColorScheme = lightColorScheme(
    primary = MeadowGreen,
    onPrimary = MeadowWhite,
    primaryContainer = MeadowGreenLight,
    onPrimaryContainer = MeadowGreenDark,
    secondary = MeadowEarth,
    onSecondary = MeadowWhite,
    secondaryContainer = MeadowEarthLight,
    onSecondaryContainer = Color(0xFF3E2F1F),
    tertiary = MeadowBlush,
    onTertiary = Color(0xFF4A2C2E),
    background = MeadowCream,
    onBackground = MeadowGreenDark,
    surface = MeadowWhite,
    onSurface = MeadowGreenDark,
    surfaceVariant = Color(0xFFE8EDE4),
    onSurfaceVariant = MeadowEarth,
    error = WeedRed,
    onError = MeadowWhite
)

private val DarkMeadowColorScheme = darkColorScheme(
    primary = MeadowGreenLight,
    onPrimary = MeadowGreenDark,
    primaryContainer = MeadowGreenDark,
    onPrimaryContainer = MeadowGreenLight,
    secondary = MeadowEarthLight,
    onSecondary = Color(0xFF2A2015),
    background = Color(0xFF1A2419),
    onBackground = MeadowCream,
    surface = Color(0xFF243024),
    onSurface = MeadowCream,
    surfaceVariant = Color(0xFF2E3D2E),
    onSurfaceVariant = MeadowSage,
    error = WeedRed,
    onError = MeadowWhite
)

@Composable
fun ChargeMeNotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkMeadowColorScheme else LightMeadowColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MeadowTypography,
        content = content
    )
}
