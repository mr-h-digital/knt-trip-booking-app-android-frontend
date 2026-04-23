package com.kntransport.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.kntransport.app.R
import com.kntransport.app.data.SampleData
import com.kntransport.app.data.User
import com.kntransport.app.data.UserRole
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*

@Composable
fun AdminUserDetailScreen(
    user          : User,
    onBack        : () -> Unit,
    onEdit        : () -> Unit = {},
    onAssignVehicle: () -> Unit = {},
) {
    val c = LocalAppColors.current

    val (roleTint, roleLabel, roleIcon) = when (user.role) {
        UserRole.DRIVER  -> Triple(KntYellow,  "Driver",        Icons.Rounded.LocalShipping)
        UserRole.ADMIN   -> Triple(KntOrange,  "Administrator", Icons.Rounded.AdminPanelSettings)
        UserRole.COMMUTER -> Triple(KntBlue,   "Commuter",      Icons.Rounded.DirectionsBus)
    }

    val initials = user.name.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")

    var showDeactivateDialog by remember { mutableStateOf(false) }
    var deactivated          by remember { mutableStateOf(false) }

    if (showDeactivateDialog) {
        AlertDialog(
            onDismissRequest = { showDeactivateDialog = false },
            containerColor   = c.surface2,
            icon = {
                Icon(Icons.Rounded.PersonOff, null, tint = StatusRed, modifier = Modifier.size(28.dp))
            },
            title = {
                Text("Deactivate Account?", style = MaterialTheme.typography.titleMedium, color = c.textBright)
            },
            text = {
                Text(
                    "Are you sure you want to deactivate ${user.name}'s account? They will no longer be able to log in.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textMuted,
                )
            },
            confirmButton = {
                Button(
                    onClick = { deactivated = true; showDeactivateDialog = false },
                    colors  = ButtonDefaults.buttonColors(containerColor = StatusRed, contentColor = Color.White),
                    shape   = RoundedCornerShape(10.dp),
                ) { Text("Deactivate") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeactivateDialog = false },
                    shape   = RoundedCornerShape(10.dp),
                    border  = BorderStroke(1.dp, c.borderColor),
                    colors  = ButtonDefaults.outlinedButtonColors(contentColor = c.textMuted),
                ) { Text("Cancel") }
            },
        )
    }

    KntScaffold(
        title   = "User Detail",
        onBack  = onBack,
        actions = {
            IconButton(onClick = onEdit) {
                Icon(Icons.Rounded.Edit, "Edit user", tint = KntWhite)
            }
        },
    ) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv)
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Avatar header ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
                contentAlignment = Alignment.Center,
            ) {
                HeroBgImage(resId = R.drawable.hero_bg_2, modifier = Modifier.fillMaxSize(), darkOverlay = 0.62f)
                Box(Modifier.fillMaxSize().background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(androidx.compose.ui.graphics.Color.Transparent, c.bgDeep.copy(0.7f))
                    )
                ))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(roleTint.copy(0.8f), roleTint.copy(0.4f)))
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            initials,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(user.name, style = MaterialTheme.typography.headlineSmall, color = c.textBright)
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = roleTint.copy(alpha = 0.15f),
                    ) {
                        Row(
                            Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Icon(roleIcon, null, tint = roleTint, modifier = Modifier.size(14.dp))
                            Text(roleLabel, style = MaterialTheme.typography.labelMedium, color = roleTint)
                        }
                    }
                    if (deactivated) {
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = StatusRed.copy(alpha = 0.15f),
                        ) {
                            Text(
                                "Deactivated",
                                style    = MaterialTheme.typography.labelSmall,
                                color    = StatusRed,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }

            Column(Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(8.dp))

                // ── Account details ───────────────────────────────────────
                SectionHeader(title = "Account Details")
                KntCard {
                    UserDetailRow(Icons.Rounded.Badge,  "Full Name", user.name)
                    KntDivider()
                    UserDetailRow(Icons.Rounded.Email,  "Email",     user.email)
                    KntDivider()
                    UserDetailRow(Icons.Rounded.Phone,  "Phone",     user.phone)
                    KntDivider()
                    UserDetailRow(Icons.Rounded.Key,    "User ID",   user.id)
                }

                Spacer(Modifier.height(16.dp))

                // ── Role-specific activity ────────────────────────────────
                SectionHeader(title = "Activity")
                KntCard {
                    when (user.role) {
                        UserRole.DRIVER -> {
                            UserActivityRow(Icons.Rounded.DirectionsBus, "Trips Assigned", "12", c.blue)
                            KntDivider()
                            UserActivityRow(Icons.Rounded.CheckCircle,   "Trips Completed", "10", StatusGreen)
                            KntDivider()
                            UserActivityRow(Icons.Rounded.Star,          "Average Rating",  "4.3 ★", KntYellow)
                        }
                        UserRole.COMMUTER -> {
                            UserActivityRow(Icons.Rounded.DirectionsBus, "Total Trips",     "8",  c.blue)
                            KntDivider()
                            UserActivityRow(Icons.Rounded.Groups,        "Lift Club Subs",  "2",  c.yellow)
                            KntDivider()
                            UserActivityRow(Icons.Rounded.CheckCircle,   "Completed Trips", "6",  StatusGreen)
                        }
                        UserRole.ADMIN -> {
                            UserActivityRow(Icons.Rounded.People,              "Users Managed", "7",  c.blue)
                            KntDivider()
                            UserActivityRow(Icons.Rounded.AdminPanelSettings,  "Role",          "Administrator", KntOrange)
                        }
                    }
                }

                // ── Assigned Vehicle (drivers only) ──────────────────────
                if (user.role == UserRole.DRIVER) {
                    Spacer(Modifier.height(16.dp))
                    SectionHeader(title = "Assigned Vehicle")
                    val vehicle = SampleData.vehicles.firstOrNull { it.assignedDriverId == user.id }
                    if (vehicle != null) {
                        KntCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier.size(44.dp).clip(RoundedCornerShape(10.dp))
                                        .background(c.yellow.copy(0.12f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(Icons.Rounded.DirectionsBus, null, tint = c.yellow, modifier = Modifier.size(22.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        "${vehicle.colour} ${vehicle.make} ${vehicle.model}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = c.textBright,
                                    )
                                    Text(
                                        "${vehicle.plate} · ${vehicle.year}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = c.textMuted,
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = StatusGreen.copy(0.12f),
                                ) {
                                    Text(
                                        "Assigned",
                                        style    = MaterialTheme.typography.labelSmall,
                                        color    = StatusGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    )
                                }
                            }
                        }
                    } else {
                        Surface(
                            shape    = RoundedCornerShape(14.dp),
                            color    = c.surface2,
                            border   = BorderStroke(1.dp, KntOrange.copy(0.3f)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Rounded.DirectionsBus, null, tint = c.textDim, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Text("No vehicle assigned", style = MaterialTheme.typography.bodyMedium, color = c.textMuted, modifier = Modifier.weight(1f))
                                TextButton(onClick = onAssignVehicle) {
                                    Text("Assign", style = MaterialTheme.typography.labelMedium, color = KntOrange)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── Admin actions ─────────────────────────────────────────
                SectionHeader(title = "Actions")

                KntPrimaryButton(
                    text    = "Edit Profile",
                    onClick = onEdit,
                    icon    = Icons.Rounded.Edit,
                )

                Spacer(Modifier.height(10.dp))

                if (!deactivated) {
                    Button(
                        onClick  = { showDeactivateDialog = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = StatusRed.copy(alpha = 0.12f),
                            contentColor   = StatusRed,
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                    ) {
                        Icon(Icons.Rounded.PersonOff, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Deactivate Account", style = MaterialTheme.typography.labelLarge)
                    }
                } else {
                    Button(
                        onClick  = { deactivated = false },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = StatusGreen.copy(alpha = 0.12f),
                            contentColor   = StatusGreen,
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                    ) {
                        Icon(Icons.Rounded.PersonAdd, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Reactivate Account", style = MaterialTheme.typography.labelLarge)
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun UserDetailRow(icon: ImageVector, label: String, value: String) {
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
private fun UserActivityRow(icon: ImageVector, label: String, value: String, tint: Color) {
    val c = LocalAppColors.current
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = c.textBright, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = tint))
    }
}
