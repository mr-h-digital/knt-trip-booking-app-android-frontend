package com.kntransport.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ── KNT Brand palette (from website: --black #061220, --dark #0a2540, --yellow #e8c14a, --blue #1a8fe3, --accent #e85c2a)
val KntYellow       = Color(0xFFE8C14A)
val KntYellowDim    = Color(0xFFC9A030)
val KntBlue         = Color(0xFF1A8FE3)
val KntBlueBright   = Color(0xFF3AAFF5)
val KntOrange       = Color(0xFFE85C2A)
val KntDark         = Color(0xFF0A2540)
val KntBlack        = Color(0xFF061220)
val KntPanel        = Color(0xFF0D2E4D)
val KntCard         = Color(0xFF0F3358)
val KntMuted        = Color(0xFF7BB8D4)
val KntWhite        = Color(0xFFE8F4FC)
val KntBorder       = Color(0x381A8FE3)

val StatusGreen     = Color(0xFF4CAF50)
val StatusAmber     = Color(0xFFFF9800)
val StatusRed       = Color(0xFFE85C2A)

@Immutable
data class AppColors(
    val bgDeep          : Color,
    val bgGradientTop   : Color,
    val bgGradientMid   : Color,
    val bgGradientBottom: Color,
    val surface1        : Color,
    val surface2        : Color,
    val surface3        : Color,
    val textBright      : Color,
    val textMuted       : Color,
    val textDim         : Color,
    val borderColor     : Color,
    val navBackground   : Color,
    val headerStart     : Color,
    val headerEnd       : Color,
    val headerText      : Color,
    val headerTextMuted : Color,
    val logoBg          : Color,
    val yellow          : Color,
    val blue            : Color,
    val blueBright      : Color,
    val orange          : Color,
)

val DarkAppColors = AppColors(
    bgDeep           = KntBlack,
    bgGradientTop    = KntBlack,
    bgGradientMid    = KntBlack,
    bgGradientBottom = KntBlack,
    surface1         = Color(0xFF0A1E35),
    surface2         = KntDark,
    surface3         = KntPanel,
    textBright       = KntWhite,
    textMuted        = KntMuted,
    textDim          = Color(0xFF4A6A80),
    borderColor      = KntBorder,
    navBackground    = Color(0xFF071828),
    headerStart      = KntBlack,
    headerEnd        = KntDark,
    headerText       = KntWhite,
    headerTextMuted  = KntMuted,
    logoBg           = Color.Transparent,
    yellow           = KntYellow,
    blue             = KntBlue,
    blueBright       = KntBlueBright,
    orange           = KntOrange,
)

val LightAppColors = AppColors(
    bgDeep           = Color(0xFFF0F6FC),
    bgGradientTop    = Color(0xFFDCEEFA),   // header-blue wash bleeding into content
    bgGradientMid    = Color(0xFFEDF4FB),   // neutral mid
    bgGradientBottom = Color(0xFFFAF6EC),   // warm yellow-cream at bottom
    surface1         = Color(0xFFFFFFFF),
    surface2         = Color(0xFFE8F3FC),
    surface3         = Color(0xFFD4E6F3),
    textBright       = Color(0xFF061220),
    textMuted        = Color(0xFF2A5A7A),
    textDim          = Color(0xFF6A9AB5),
    borderColor      = Color(0x401A8FE3),
    navBackground    = Color(0xFFFFFFFF),
    headerStart      = Color(0xFF0D3060),
    headerEnd        = Color(0xFF1A5A9A),
    headerText       = KntWhite,
    headerTextMuted  = Color(0xFFB0D4EE),
    logoBg           = Color(0xFF0A2540),
    yellow           = KntYellow,
    blue             = KntBlue,
    blueBright       = KntBlueBright,
    orange           = KntOrange,
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }
