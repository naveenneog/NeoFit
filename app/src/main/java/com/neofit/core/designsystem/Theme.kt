package com.neofit.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = NeoSaffron,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE0CC),
    onPrimaryContainer = NeoSaffronDark,
    secondary = NeoGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCFEEEA),
    onSecondaryContainer = NeoGreenDark,
    tertiary = NeoBerry,
    onTertiary = Color.White,
    background = NeoCloud,
    onBackground = NeoInk,
    surface = NeoSurfaceLight,
    onSurface = NeoInk,
    surfaceVariant = Color(0xFFF1E7DE),
    onSurfaceVariant = Color(0xFF5A5247),
    outline = Color(0xFFCBB9A8),
    error = Color(0xFFBA1A1A),
)

private val DarkColors = darkColorScheme(
    primary = NeoSaffron,
    onPrimary = Color(0xFF3A1A00),
    primaryContainer = NeoSaffronDark,
    onPrimaryContainer = Color(0xFFFFE0CC),
    secondary = Color(0xFF4EC3B5),
    onSecondary = Color(0xFF00362F),
    secondaryContainer = NeoGreenDark,
    onSecondaryContainer = Color(0xFFCFEEEA),
    tertiary = Color(0xFFFF8A93),
    onTertiary = Color(0xFF5A0010),
    background = NeoBgDark,
    onBackground = Color(0xFFEDE0D4),
    surface = NeoSurfaceDark,
    onSurface = Color(0xFFEDE0D4),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    error = Color(0xFFFFB4AB),
)

@Composable
fun NeoFitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = NeoTypography,
        content = content,
    )
}
