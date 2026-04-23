package com.kntransport.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.kntransport.app.ui.components.KntScaffold
import com.kntransport.app.ui.theme.*

@Composable
fun AppearanceScreen(onBack: () -> Unit) {
    val c         = LocalAppColors.current
    val themeMode = LocalThemeMode.current
    val toggle    = LocalThemeModeToggle.current

    KntScaffold(title = "Appearance", onBack = onBack) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(32.dp))

            Text(
                "Choose a theme",
                style = MaterialTheme.typography.headlineSmall,
                color = c.textBright,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Select how K&T Transport looks on your device",
                style = MaterialTheme.typography.bodyMedium,
                color = c.textMuted,
            )

            Spacer(Modifier.height(40.dp))

            // Three theme cards side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ThemeOptionCard(
                    modifier  = Modifier.weight(1f),
                    label     = "Light",
                    icon      = Icons.Rounded.LightMode,
                    selected  = themeMode == ThemeMode.LIGHT,
                    onClick   = { toggle(ThemeMode.LIGHT) },
                    preview   = { LightPhonePreview() },
                )
                ThemeOptionCard(
                    modifier  = Modifier.weight(1f),
                    label     = "Dark",
                    icon      = Icons.Rounded.DarkMode,
                    selected  = themeMode == ThemeMode.DARK,
                    onClick   = { toggle(ThemeMode.DARK) },
                    preview   = { DarkPhonePreview() },
                )
                ThemeOptionCard(
                    modifier  = Modifier.weight(1f),
                    label     = "System",
                    icon      = Icons.Rounded.SettingsBrightness,
                    selected  = themeMode == ThemeMode.SYSTEM,
                    onClick   = { toggle(ThemeMode.SYSTEM) },
                    preview   = { SystemPhonePreview() },
                )
            }

            Spacer(Modifier.height(32.dp))

            // Current selection summary
            val modeLabel = when (themeMode) {
                ThemeMode.LIGHT  -> "Light mode is active"
                ThemeMode.DARK   -> "Dark mode is active"
                ThemeMode.SYSTEM -> "Following system setting"
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(c.surface1)
                    .border(1.dp, c.borderColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Icon(
                    imageVector = when (themeMode) {
                        ThemeMode.LIGHT  -> Icons.Rounded.LightMode
                        ThemeMode.DARK   -> Icons.Rounded.DarkMode
                        ThemeMode.SYSTEM -> Icons.Rounded.SettingsBrightness
                    },
                    contentDescription = null,
                    tint = c.yellow,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(modeLabel, style = MaterialTheme.typography.bodyMedium, color = c.textMuted)
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun ThemeOptionCard(
    modifier : Modifier,
    label    : String,
    icon     : androidx.compose.ui.graphics.vector.ImageVector,
    selected : Boolean,
    onClick  : () -> Unit,
    preview  : @Composable () -> Unit,
) {
    val c           = LocalAppColors.current
    val borderColor by animateColorAsState(
        targetValue   = if (selected) c.yellow else c.borderColor,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label         = "border",
    )
    val bgColor by animateColorAsState(
        targetValue   = if (selected) c.yellow.copy(alpha = 0.07f) else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label         = "bg",
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Mini phone illustration
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.52f)
                .shadow(if (selected) 6.dp else 2.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp)),
        ) {
            preview()
        }

        Spacer(Modifier.height(10.dp))

        // Icon + Label row
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon, null,
                tint     = if (selected) c.yellow else c.textMuted,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color      = if (selected) c.yellow else c.textMuted,
                ),
            )
        }

        Spacer(Modifier.height(6.dp))

        // Selection indicator dot
        Box(
            Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (selected) c.yellow else Color.Transparent)
        )
    }
}

// ── Mini phone illustrations ─────────────────────────────────────────────────

@Composable
private fun DarkPhonePreview() {
    val bg      = Color(0xFF061220)
    val surface = Color(0xFF0A1E35)
    val header  = Color(0xFF0A2540)
    val blue    = Color(0xFF1A8FE3)
    val yellow  = Color(0xFFE8C14A)
    val muted   = Color(0xFF4A6A80)
    PhoneChrome(bg = bg) {
        // Status bar
        Box(Modifier.fillMaxWidth().height(6.dp).background(Color(0xFF071828)))
        // Header bar
        Box(
            Modifier.fillMaxWidth().height(18.dp)
                .background(Brush.verticalGradient(listOf(Color(0xFF061220), header)))
        ) {
            Box(Modifier.align(Alignment.BottomEnd).width(14.dp).height(1.dp).background(yellow))
        }
        Spacer(Modifier.weight(1f).background(bg))
        // Content rows
        Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            // Card 1
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp))
                    .background(surface).padding(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(blue))
                Spacer(Modifier.width(5.dp))
                Column {
                    Box(Modifier.width(40.dp).height(3.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF7BB8D4)))
                    Spacer(Modifier.height(2.dp))
                    Box(Modifier.width(28.dp).height(2.dp).clip(RoundedCornerShape(2.dp)).background(muted))
                }
            }
            // Card 2
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp))
                    .background(surface).padding(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(yellow))
                Spacer(Modifier.width(5.dp))
                Column {
                    Box(Modifier.width(36.dp).height(3.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF7BB8D4)))
                    Spacer(Modifier.height(2.dp))
                    Box(Modifier.width(22.dp).height(2.dp).clip(RoundedCornerShape(2.dp)).background(muted))
                }
            }
            // Button
            Box(
                Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(4.dp))
                    .background(Brush.horizontalGradient(listOf(blue, blue.copy(alpha = 0.7f))))
            )
        }
        // Bottom nav
        Box(
            Modifier.fillMaxWidth().height(16.dp)
                .background(Color(0xFF071828))
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(4) { i ->
                    Box(Modifier.size(if (i == 0) 6.dp else 5.dp).clip(CircleShape)
                        .background(if (i == 0) yellow else muted))
                }
            }
        }
    }
}

@Composable
private fun LightPhonePreview() {
    val bg      = Color(0xFFF0F6FC)
    val surface = Color(0xFFE2EEF8)
    val header  = Color(0xFF0A2540)
    val blue    = Color(0xFF1A8FE3)
    val yellow  = Color(0xFFE8C14A)
    val muted   = Color(0xFF6A9AB5)
    PhoneChrome(bg = bg) {
        Box(Modifier.fillMaxWidth().height(6.dp).background(Color(0xFF061220)))
        Box(
            Modifier.fillMaxWidth().height(18.dp)
                .background(Brush.verticalGradient(listOf(Color(0xFF061220), header)))
        ) {
            Box(Modifier.align(Alignment.BottomEnd).width(14.dp).height(1.dp).background(yellow))
        }
        Spacer(Modifier.weight(1f).background(bg))
        Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp))
                    .background(surface).padding(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(blue))
                Spacer(Modifier.width(5.dp))
                Column {
                    Box(Modifier.width(40.dp).height(3.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF2A5A7A)))
                    Spacer(Modifier.height(2.dp))
                    Box(Modifier.width(28.dp).height(2.dp).clip(RoundedCornerShape(2.dp)).background(muted))
                }
            }
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp))
                    .background(surface).padding(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(yellow))
                Spacer(Modifier.width(5.dp))
                Column {
                    Box(Modifier.width(36.dp).height(3.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF2A5A7A)))
                    Spacer(Modifier.height(2.dp))
                    Box(Modifier.width(22.dp).height(2.dp).clip(RoundedCornerShape(2.dp)).background(muted))
                }
            }
            Box(
                Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(4.dp))
                    .background(Brush.horizontalGradient(listOf(blue, blue.copy(alpha = 0.7f))))
            )
        }
        Box(
            Modifier.fillMaxWidth().height(16.dp)
                .background(Color(0xFFE8F2FB))
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(4) { i ->
                    Box(Modifier.size(if (i == 0) 6.dp else 5.dp).clip(CircleShape)
                        .background(if (i == 0) yellow else muted))
                }
            }
        }
    }
}

@Composable
private fun SystemPhonePreview() {
    // Split diagonally: left=dark, right=light
    val darkBg   = Color(0xFF061220)
    val lightBg  = Color(0xFFF0F6FC)
    val header   = Color(0xFF0A2540)
    val blue     = Color(0xFF1A8FE3)
    val yellow   = Color(0xFFE8C14A)
    val darkMuted  = Color(0xFF4A6A80)
    val lightMuted = Color(0xFF6A9AB5)

    PhoneChrome(bg = darkBg) {
        Box(Modifier.fillMaxSize()) {
            // Dark half (left)
            Box(Modifier.fillMaxHeight().fillMaxWidth(0.5f).background(darkBg))
            // Light half (right)
            Box(Modifier.fillMaxHeight().fillMaxWidth(0.5f).align(Alignment.TopEnd).background(lightBg))
            // Diagonal divider
            Canvas(Modifier.fillMaxSize()) {
                drawLine(
                    color       = Color(0xFF1A8FE3).copy(alpha = 0.6f),
                    start       = androidx.compose.ui.geometry.Offset(size.width * 0.4f, 0f),
                    end         = androidx.compose.ui.geometry.Offset(size.width * 0.6f, size.height),
                    strokeWidth = 1.5f,
                )
            }
            // Overlay UI chrome
            Column(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxWidth().height(6.dp).background(Color(0xFF061220)))
                Box(
                    Modifier.fillMaxWidth().height(18.dp)
                        .background(Brush.verticalGradient(listOf(Color(0xFF061220), header)))
                ) {
                    Box(Modifier.align(Alignment.BottomEnd).width(14.dp).height(1.dp).background(yellow))
                }
                Spacer(Modifier.weight(1f))
                Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp))
                            .background(Color(0x800A1E35)).padding(5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(blue))
                        Spacer(Modifier.width(5.dp))
                        Column {
                            Box(Modifier.width(40.dp).height(3.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF7BB8D4)))
                            Spacer(Modifier.height(2.dp))
                            Box(Modifier.width(28.dp).height(2.dp).clip(RoundedCornerShape(2.dp)).background(darkMuted))
                        }
                    }
                    Box(
                        Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(4.dp))
                            .background(Brush.horizontalGradient(listOf(blue, blue.copy(alpha = 0.7f))))
                    )
                }
                Box(Modifier.fillMaxWidth().height(16.dp).background(Color(0xFF071828))) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        repeat(4) { i ->
                            Box(Modifier.size(if (i == 0) 6.dp else 5.dp).clip(CircleShape)
                                .background(if (i == 0) yellow else lightMuted))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhoneChrome(bg: Color, content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1A2A3A))
            .padding(3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(bg),
        ) {
            content()
        }
        // Notch
        Box(
            Modifier.align(Alignment.TopCenter).padding(top = 7.dp)
                .width(12.dp).height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF1A2A3A))
        )
    }
}
