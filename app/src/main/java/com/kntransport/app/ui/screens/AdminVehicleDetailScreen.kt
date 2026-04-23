package com.kntransport.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.kntransport.app.R
import com.kntransport.app.data.SampleData
import com.kntransport.app.data.Vehicle
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*

@Composable
fun AdminVehicleDetailScreen(
    vehicle        : Vehicle,
    onBack         : () -> Unit,
    onEdit         : () -> Unit = {},
    onAssignDriver : () -> Unit = {},
) {
    val c = LocalAppColors.current
    var showDeactivateDialog by remember { mutableStateOf(false) }
    var deactivated          by remember { mutableStateOf(!vehicle.active) }

    val assignedDriver = SampleData.vehicles
        .find { it.id == vehicle.id }
        ?.assignedDriverId
        ?.let { dId -> adminSampleUsers.find { it.id == dId } }

    if (showDeactivateDialog) {
        AlertDialog(
            onDismissRequest = { showDeactivateDialog = false },
            containerColor   = c.surface2,
            icon = { Icon(Icons.Rounded.DirectionsBus, null, tint = StatusRed, modifier = Modifier.size(28.dp)) },
            title = { Text("Deactivate Vehicle?", style = MaterialTheme.typography.titleMedium, color = c.textBright) },
            text  = {
                Text(
                    "Deactivating ${vehicle.colour} ${vehicle.make} ${vehicle.model} (${vehicle.plate}) will remove it from the active fleet and unassign it from any driver.",
                    style = MaterialTheme.typography.bodyMedium, color = c.textMuted,
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
        title   = "Vehicle Detail",
        onBack  = onBack,
        actions = {
            IconButton(onClick = onEdit) {
                Icon(Icons.Rounded.Edit, "Edit vehicle", tint = KntWhite)
            }
        },
    ) { pv ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pv).verticalScroll(rememberScrollState()),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(140.dp)
                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)),
                contentAlignment = Alignment.Center,
            ) {
                HeroBgImage(resId = R.drawable.hero_bg_4, modifier = Modifier.fillMaxSize(), darkOverlay = 0.58f)
                Column(
                    Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 14.dp),
                ) {
                    Text(
                        "${vehicle.colour} ${vehicle.make} ${vehicle.model}",
                        style = MaterialTheme.typography.titleLarge.copy(color = KntWhite, fontWeight = FontWeight.Bold),
                    )
                    Text(
                        vehicle.plate,
                        style = MaterialTheme.typography.bodySmall.copy(color = KntYellow),
                    )
                }
                if (deactivated) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StatusRed.copy(0.85f),
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                    ) {
                        Text("Inactive", style = MaterialTheme.typography.labelSmall, color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }

            Column(Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(16.dp))

                SectionHeader(title = "Vehicle Details")
                KntCard {
                    InfoRow(Icons.Rounded.DirectionsBus,      "Make & Model", "${vehicle.make} ${vehicle.model}")
                    KntDivider()
                    InfoRow(Icons.Rounded.Palette,            "Colour",       vehicle.colour)
                    KntDivider()
                    InfoRow(Icons.Rounded.ConfirmationNumber, "Plate",        vehicle.plate)
                    KntDivider()
                    InfoRow(Icons.Rounded.CalendarMonth,      "Year",         vehicle.year.toString())
                    KntDivider()
                    InfoRow(Icons.Rounded.Category,           "Type",
                        vehicle.vehicleType.name.lowercase().replaceFirstChar { it.uppercase() })
                    if (vehicle.notes.isNotBlank()) {
                        KntDivider()
                        InfoRow(Icons.Rounded.Notes, "Notes", vehicle.notes)
                    }
                }

                Spacer(Modifier.height(16.dp))
                SectionHeader(title = "Assigned Driver")

                if (assignedDriver != null) {
                    KntCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(44.dp).clip(RoundedCornerShape(10.dp))
                                    .background(KntYellow.copy(0.12f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Rounded.Person, null, tint = KntYellow, modifier = Modifier.size(22.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(assignedDriver.name, style = MaterialTheme.typography.titleSmall, color = c.textBright)
                                Text(assignedDriver.email, style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                            }
                            Surface(shape = RoundedCornerShape(8.dp), color = KntYellow.copy(0.12f)) {
                                Text("Driver", style = MaterialTheme.typography.labelSmall, color = KntYellow,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }
                } else {
                    Surface(
                        shape  = RoundedCornerShape(14.dp),
                        color  = c.surface2,
                        border = BorderStroke(1.dp, KntOrange.copy(0.3f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.PersonOff, null, tint = c.textDim, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("No driver assigned", style = MaterialTheme.typography.bodyMedium, color = c.textMuted, modifier = Modifier.weight(1f))
                            TextButton(onClick = onAssignDriver) {
                                Text("Assign Driver", style = MaterialTheme.typography.labelMedium, color = KntOrange)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                SectionHeader(title = "Actions")

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
                        Icon(Icons.Rounded.Block, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Deactivate Vehicle", style = MaterialTheme.typography.labelLarge)
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
                        Icon(Icons.Rounded.CheckCircle, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Reactivate Vehicle", style = MaterialTheme.typography.labelLarge)
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
