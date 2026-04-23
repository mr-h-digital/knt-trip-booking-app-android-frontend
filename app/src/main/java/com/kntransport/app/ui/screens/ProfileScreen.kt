package com.kntransport.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.kntransport.app.data.SampleData
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*

@Composable
fun ProfileScreen(
    onBack        : () -> Unit,
    onSignOut     : () -> Unit = {},
    onAppearance  : () -> Unit = {},
    onEditProfile : () -> Unit = {},
) {
    val c    = LocalAppColors.current
    val user = SampleData.currentUser
    var showSignOutDialog by remember { mutableStateOf(false) }
    val themeMode = LocalThemeMode.current

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            containerColor   = c.surface1,
            icon = {
                Icon(Icons.Rounded.Logout, null, tint = StatusRed, modifier = Modifier.size(28.dp))
            },
            title = {
                Text("Sign Out", style = MaterialTheme.typography.headlineSmall, color = c.textBright)
            },
            text = {
                Text(
                    "Are you sure you want to sign out of your account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textMuted,
                )
            },
            confirmButton = {
                Button(
                    onClick = { showSignOutDialog = false; onSignOut() },
                    colors  = ButtonDefaults.buttonColors(containerColor = StatusRed, contentColor = Color.White),
                    shape   = RoundedCornerShape(10.dp),
                ) {
                    Text("Sign Out", style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showSignOutDialog = false },
                    shape   = RoundedCornerShape(10.dp),
                    border  = BorderStroke(1.dp, c.borderColor),
                    colors  = ButtonDefaults.outlinedButtonColors(contentColor = c.textMuted),
                ) {
                    Text("Cancel", style = MaterialTheme.typography.labelLarge)
                }
            },
        )
    }

    KntScaffold(title = "My Profile", onBack = onBack) { pv ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pv).verticalScroll(rememberScrollState())
        ) {
            // Avatar header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(c.headerEnd, c.bgDeep)))
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        UserAvatar(
                            name      = user.name,
                            avatarUri = user.avatarUri,
                            size      = 88.dp,
                            onClick   = onEditProfile,
                        )
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(c.blue)
                                .border(2.dp, c.bgDeep, CircleShape)
                                .clickable { onEditProfile() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.Edit, null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(13.dp))
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(user.name, style = MaterialTheme.typography.headlineMedium, color = c.textBright)
                    Text(user.role.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                }
            }

            Column(Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(8.dp))
                SectionHeader(title = "Account Details", action = "Edit", onAction = onEditProfile)
                KntCard {
                    ProfileRow(Icons.Rounded.Email, "Email", user.email)
                    KntDivider()
                    ProfileRow(Icons.Rounded.Phone, "Phone", user.phone)
                    KntDivider()
                    ProfileRow(Icons.Rounded.Person, "Role", user.role.name.lowercase().replaceFirstChar { it.uppercase() })
                }

                Spacer(Modifier.height(16.dp))
                SectionHeader(title = "My Activity")
                KntCard {
                    ActivityRow(Icons.Rounded.DirectionsBus, "Total Trips",        "${SampleData.myTrips.size}", c.blue)
                    KntDivider()
                    ActivityRow(Icons.Rounded.Groups, "Lift Club Subscriptions",   "${SampleData.myLiftClubSubscriptions.size}", c.yellow)
                    KntDivider()
                    ActivityRow(Icons.Rounded.CheckCircle, "Completed Trips",
                        "${SampleData.myTrips.count { it.status == com.kntransport.app.data.TripStatus.COMPLETED }}", StatusGreen)
                }

                Spacer(Modifier.height(16.dp))
                SectionHeader(title = "Support")
                KntCard {
                    ProfileRow(Icons.Rounded.Chat, "WhatsApp Support", "+27 78 778 4182")
                    KntDivider()
                    ProfileRow(Icons.Rounded.Language, "Website", "www.ktransport.co.za")
                    KntDivider()
                    ProfileRow(Icons.Rounded.LocationOn, "Base", "Beacon Valley, Mitchell's Plain")
                }

                Spacer(Modifier.height(16.dp))
                SectionHeader(title = "Preferences")
                KntCard(onClick = onAppearance) {
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = when (themeMode) {
                                ThemeMode.DARK   -> Icons.Rounded.DarkMode
                                ThemeMode.LIGHT  -> Icons.Rounded.LightMode
                                ThemeMode.SYSTEM -> Icons.Rounded.SettingsBrightness
                            },
                            contentDescription = null,
                            tint = c.blue,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Appearance", style = MaterialTheme.typography.labelMedium, color = c.textMuted)
                            Text(
                                when (themeMode) {
                                    ThemeMode.DARK   -> "Dark"
                                    ThemeMode.LIGHT  -> "Light"
                                    ThemeMode.SYSTEM -> "System default"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = c.textBright,
                            )
                        }
                        Icon(
                            Icons.Rounded.ChevronRight, null,
                            tint = c.textDim, modifier = Modifier.size(18.dp),
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Sign out
                Button(
                    onClick  = { showSignOutDialog = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = StatusRed.copy(alpha = 0.12f),
                        contentColor   = StatusRed,
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp),
                ) {
                    Icon(Icons.Rounded.Logout, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Sign Out", style = MaterialTheme.typography.labelLarge)
                }

                Spacer(Modifier.height(16.dp))

                // Branding footer
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                ) {
                    KntLogoBadge(size = 48.dp)
                    Spacer(Modifier.height(8.dp))
                    Text("K&T Transport", style = MaterialTheme.typography.titleMedium, color = c.textBright)
                    Text("Moving Communities Forward", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                    Spacer(Modifier.height(4.dp))
                    Text("v1.0.0", style = MaterialTheme.typography.labelSmall, color = c.textDim)

                    Spacer(Modifier.height(24.dp))

                    // Developer attribution
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 32.dp),
                        color    = c.borderColor,
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Designed & Developed by",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color        = c.textDim,
                            letterSpacing = 0.5.sp,
                        ),
                    )
                    Spacer(Modifier.height(10.dp))
                    androidx.compose.foundation.Image(
                        painter            = androidx.compose.ui.res.painterResource(com.kntransport.app.R.drawable.mrh_digital_logo),
                        contentDescription = "Mr H Digital",
                        modifier           = Modifier.width(130.dp),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Digital Solutions for Growing Businesses",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color        = c.textDim,
                            letterSpacing = 0.3.sp,
                        ),
                    )
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ProfileRow(icon: ImageVector, label: String, value: String) {
    val c = LocalAppColors.current
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = c.blue, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = c.textMuted)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = c.textBright)
        }
    }
}

@Composable
private fun ActivityRow(icon: ImageVector, label: String, value: String, tint: androidx.compose.ui.graphics.Color) {
    val c = LocalAppColors.current
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = c.textBright, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.titleMedium.copy(color = tint))
    }
}
