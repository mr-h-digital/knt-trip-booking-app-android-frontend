package com.kntransport.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeMode { LIGHT, DARK, SYSTEM }

val LocalIsDarkTheme      = staticCompositionLocalOf { true }
val LocalThemeMode        = staticCompositionLocalOf { ThemeMode.DARK }
val LocalThemeModeToggle  = staticCompositionLocalOf<(ThemeMode) -> Unit> { {} }

private val KntDarkColorScheme = darkColorScheme(
    primary          = KntBlue,
    onPrimary        = Color.White,
    primaryContainer = KntBorder,
    secondary        = KntYellow,
    onSecondary      = KntBlack,
    tertiary         = KntOrange,
    onTertiary       = Color.White,
    background       = KntBlack,
    onBackground     = KntWhite,
    surface          = Color(0xFF0A1E35),
    onSurface        = KntWhite,
    surfaceVariant   = KntDark,
    onSurfaceVariant = KntMuted,
    outline          = KntBorder,
)

private val KntLightColorScheme = lightColorScheme(
    primary          = KntBlue,
    onPrimary        = Color.White,
    primaryContainer = KntBorder,
    secondary        = KntYellow,
    onSecondary      = KntBlack,
    tertiary         = KntOrange,
    onTertiary       = Color.White,
    background       = Color(0xFFFFFFFF),   // white screen background
    onBackground     = KntWhite,
    surface          = Color(0xFF0A1E35),   // dark card surface
    onSurface        = KntWhite,
    surfaceVariant   = KntDark,
    onSurfaceVariant = KntMuted,
    outline          = KntBorder,
)

@Composable
fun KNTTransportTheme(isDark: Boolean = true, themeMode: ThemeMode = ThemeMode.DARK, content: @Composable () -> Unit) {
    val appColors   = if (isDark) DarkAppColors else LightAppColors
    val colorScheme = if (isDark) KntDarkColorScheme else KntLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            window.statusBarColor     = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            window.decorView.setBackgroundColor(if (isDark) 0xFF061220.toInt() else 0xFFFFFFFF.toInt())
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars     = false
                isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(
        LocalAppColors       provides appColors,
        LocalIsDarkTheme     provides isDark,
        LocalThemeMode       provides themeMode,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = KntTypography,
            content     = content,
        )
    }
}
