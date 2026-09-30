package com.example.pelecarddemo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.pelecarddemo.R

/** The default UI font (400/600/700). */
val Manrope = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
)

/**
 * Display font (semibold only). Per design/DESIGN.md, apply this directly at the call site —
 * never as a Typography default — and only for: the big amount on the main screen, the amount
 * on the receipt, and screen titles such as "Sign to confirm …".
 */
val Fraunces = FontFamily(Font(R.font.fraunces_semibold, FontWeight.SemiBold))

/** Uppercase section labels ("NEW PAYMENT", "RECEIPT #…", "CONVERSION"). */
val SectionLabel = TextStyle(
    fontFamily = Manrope,
    fontWeight = FontWeight.Bold,
    fontSize = 13.sp,
    letterSpacing = 0.1.em,
)

private val defaults = Typography()

val PeleDemoTypography = Typography(
    displayLarge = defaults.displayLarge.copy(fontFamily = Manrope),
    displayMedium = defaults.displayMedium.copy(fontFamily = Manrope),
    displaySmall = defaults.displaySmall.copy(fontFamily = Manrope),
    headlineLarge = defaults.headlineLarge.copy(fontFamily = Manrope),
    headlineMedium = defaults.headlineMedium.copy(fontFamily = Manrope),
    headlineSmall = defaults.headlineSmall.copy(fontFamily = Manrope),
    titleLarge = defaults.titleLarge.copy(fontFamily = Manrope, fontWeight = FontWeight.SemiBold),
    titleMedium = defaults.titleMedium.copy(fontFamily = Manrope, fontWeight = FontWeight.SemiBold),
    titleSmall = defaults.titleSmall.copy(fontFamily = Manrope, fontWeight = FontWeight.SemiBold),
    bodyLarge = defaults.bodyLarge.copy(fontFamily = Manrope),
    bodyMedium = defaults.bodyMedium.copy(fontFamily = Manrope),
    bodySmall = defaults.bodySmall.copy(fontFamily = Manrope),
    labelLarge = defaults.labelLarge.copy(fontFamily = Manrope, fontWeight = FontWeight.Bold),
    labelMedium = defaults.labelMedium.copy(fontFamily = Manrope, fontWeight = FontWeight.Bold),
    labelSmall = defaults.labelSmall.copy(fontFamily = Manrope, fontWeight = FontWeight.Bold),
)
