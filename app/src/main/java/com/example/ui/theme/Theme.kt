package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CyberDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = CyberBackground,
    primaryContainer = CyberSurfaceCard,
    onPrimaryContainer = NeonCyan,
    secondary = ElectricBlue,
    onSecondary = TextPrimary,
    secondaryContainer = CyberSurfaceVariant,
    onSecondaryContainer = TextCyan,
    tertiary = CyberMagenta,
    onTertiary = TextPrimary,
    tertiaryContainer = MagentaGlow,
    onTertiaryContainer = CyberMagenta,
    background = CyberBackground,
    onBackground = TextPrimary,
    surface = CyberSurface,
    onSurface = TextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = CyberBorder,
    outlineVariant = CyberBorderGlow,
    error = CyberError,
    onError = TextPrimary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberDarkColorScheme,
        typography = Typography,
        content = content
    )
}
