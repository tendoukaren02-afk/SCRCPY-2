package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyanPrimary,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF9DF0FF),
    secondary = ElectricBlue,
    onSecondary = Color(0xFF003548),
    secondaryContainer = Color(0xFF004D68),
    onSecondaryContainer = Color(0xFFC2E8FF),
    tertiary = EmeraldSuccess,
    onTertiary = Color(0xFF003920),
    tertiaryContainer = Color(0xFF005230),
    onTertiaryContainer = Color(0xFF8CF8BE),
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    error = RoseError,
    onError = Color.White
)

private val LightColorScheme = darkColorScheme( // We prefer sleek high-contrast dark theme for low-latency streaming tools
    primary = CyanPrimaryVariant,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF9DF0FF),
    secondary = ElectricBlue,
    onSecondary = Color.White,
    tertiary = EmeraldSuccess,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    error = RoseError
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek cyber dark theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
