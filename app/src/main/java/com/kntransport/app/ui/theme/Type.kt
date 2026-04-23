package com.kntransport.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.kntransport.app.R

val SyneFontFamily = FontFamily(
    Font(R.font.syne_regular,   FontWeight.Normal),
    Font(R.font.syne_semibold,  FontWeight.SemiBold),
    Font(R.font.syne_bold,      FontWeight.Bold),
    Font(R.font.syne_extrabold, FontWeight.ExtraBold),
)

val DmSansFontFamily = FontFamily(
    Font(R.font.dm_sans_regular,  FontWeight.Normal),
    Font(R.font.dm_sans_medium,   FontWeight.Medium),
    Font(R.font.dm_sans_semibold, FontWeight.SemiBold),
)

val KntTypography = Typography(
    displayLarge  = TextStyle(fontFamily = SyneFontFamily,   fontWeight = FontWeight.ExtraBold, fontSize = 42.sp, lineHeight = 46.sp),
    displayMedium = TextStyle(fontFamily = SyneFontFamily,   fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, lineHeight = 36.sp),
    displaySmall  = TextStyle(fontFamily = SyneFontFamily,   fontWeight = FontWeight.Bold,      fontSize = 24.sp, lineHeight = 28.sp),
    headlineLarge  = TextStyle(fontFamily = SyneFontFamily,  fontWeight = FontWeight.Bold,      fontSize = 22.sp, lineHeight = 26.sp),
    headlineMedium = TextStyle(fontFamily = SyneFontFamily,  fontWeight = FontWeight.Bold,      fontSize = 18.sp, lineHeight = 22.sp),
    headlineSmall  = TextStyle(fontFamily = SyneFontFamily,  fontWeight = FontWeight.SemiBold,  fontSize = 15.sp, lineHeight = 20.sp),
    bodyLarge   = TextStyle(fontFamily = DmSansFontFamily,   fontWeight = FontWeight.Normal,    fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium  = TextStyle(fontFamily = DmSansFontFamily,   fontWeight = FontWeight.Normal,    fontSize = 13.sp, lineHeight = 20.sp),
    bodySmall   = TextStyle(fontFamily = DmSansFontFamily,   fontWeight = FontWeight.Normal,    fontSize = 11.sp, lineHeight = 16.sp),
    labelLarge  = TextStyle(fontFamily = DmSansFontFamily,   fontWeight = FontWeight.SemiBold,  fontSize = 13.sp, letterSpacing = 0.3.sp),
    labelMedium = TextStyle(fontFamily = DmSansFontFamily,   fontWeight = FontWeight.SemiBold,  fontSize = 11.sp, letterSpacing = 0.5.sp),
    labelSmall  = TextStyle(fontFamily = DmSansFontFamily,   fontWeight = FontWeight.SemiBold,  fontSize = 10.sp, letterSpacing = 0.5.sp),
    titleLarge  = TextStyle(fontFamily = SyneFontFamily,     fontWeight = FontWeight.Bold,      fontSize = 20.sp),
    titleMedium = TextStyle(fontFamily = DmSansFontFamily,   fontWeight = FontWeight.SemiBold,  fontSize = 14.sp),
    titleSmall  = TextStyle(fontFamily = DmSansFontFamily,   fontWeight = FontWeight.Medium,    fontSize = 12.sp),
)
