package com.example.pelecarddemo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val BrandLightColors = lightColorScheme(
    primary = BrandGreen,
    onPrimary = BrandPaper,
    primaryContainer = BrandGreenContainer,
    onPrimaryContainer = BrandInk,
    secondary = BrandGreen,
    onSecondary = BrandPaper,
    secondaryContainer = BrandGreenContainer,
    onSecondaryContainer = BrandInk,
    background = BrandSand,
    onBackground = BrandInk,
    surface = BrandPaper,
    onSurface = BrandInk,
    surfaceVariant = BrandSandDark,
    onSurfaceVariant = BrandInkSoft,
    outline = BrandOutline,
    outlineVariant = BrandDivider,
    error = BrandStampRed,
    onError = BrandPaper,
    errorContainer = BrandErrorContainer,
    onErrorContainer = BrandStampRed,
)

/**
 * Brand "Receipt" redesign theme (see design/DESIGN.md). Dynamic color is never used, and dark
 * theme is out of scope for now, so the same light brand palette applies in both system modes.
 */
@Composable
fun PeleDemoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BrandLightColors,
        typography = PeleDemoTypography,
        content = content,
    )
}
