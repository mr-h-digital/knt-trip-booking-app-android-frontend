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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.kntransport.app.R
import com.kntransport.app.network.ApiResult
import com.kntransport.app.network.VehicleDto
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*
import com.kntransport.app.viewmodel.AdminViewModel

@Composable
fun AdminVehicleDetailScreen(
    vehicle        : VehicleDto,
    onBack         : () -> Unit,
    onEdit         : () -> Unit = {},
    onAssignDriver : () -> Unit = {},
    viewModel      : AdminViewModel = viewModel(),
) {
    val c               = LocalAppColors.current
    val deactivateState by viewModel.deactivateState.collectAsState()
    var showDialog      by remember { mutableStateOf(false) }
    var deactivated     by remember { mutableStateOf(!vehicle.active) }
    var errorMessage    by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(deactivateState) {
        when (val s = deactivateState) {
            is ApiResult.Success -> { deactivated = true; viewModel.resetDeactivateState() }
            is ApiResult.Error   -> { errorMessage = s.message; viewModel.resetDeactivateState() }
            else -> {}
        }
    }

    val snackbarState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        errorMessage?.let { snackbarState.showSnackbar(it); errorMessage = null }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
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
                    onClick = { showDialog = false; viewModel.deactivateVehicle(vehicle.id) },
                    colors  = ButtonDefaults.buttonColors(containerColor = StatusRed, contentColor = Color.White),
                    shape   = RoundedCornerShape(10.dp),
                ) { Text("Deactivate") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDialog = false },
                    shape   = RoundedCornerShape(10.dp),
                    border  = BorderStroke(1.dp, c.borderColor),
                    colors  = ButtonDefaults.outlinedButtonColors(contentColor = c.textMuted),
                ) { Text("Cancel") }
            },
        )
    }

    KntScaffold(
        title        = "Vehicle Detail",
        onBack       = onBack,
        snackbarHost = { SnackbarHost(snackbarState) },
        actions      = {
            IconButton(onClick = onEdit) {
                Icon(Icons.Rounded.Edit, "Edit vehicle", tint = KntWhite)
            }
        },
    ) { pv ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pv).verticalScroll(rememberScrollState()),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(160.dp)
                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (!vehicle.photoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model              = ImageRequest.Builder(LocalContext.current).data(vehicle.photoUrl).build(),
                        contentDescription = "${vehicle.make} ${vehicle.model}",
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize(),
                    )
                    Box(
                        Modifier.fillMaxSize().background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                listOf(Color.Black.copy(0.25f), Color.Black.copy(0.65f))
                            )
                        )
                    )
                } else {
                    HeroBgImage(resId = R.drawable.hero_bg_4, modifier = Modifier.fillMaxSize(), darkOverlay = 0.58f)
                }
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
                        vehicle.vehicleType.lowercase().replaceFirstChar { it.uppercase() })
                    if (vehicle.notes.isNotBlank()) {
                        KntDivider()
                        InfoRow(Icons.Rounded.Notes, "Notes", vehicle.notes)
                    }
                }

                Spacer(Modifier.height(16.dp))
                SectionHeader(title = "Assigned Driver")

                if (vehicle.assignedDriverName != null) {
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
                                Text(vehicle.assignedDriverName, style = MaterialTheme.typography.titleSmall, color = c.textBright)
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

                val isDeactivating = deactivateState is ApiResult.Loading
                if (!deactivated) {
                    Button(
                        onClick  = { showDialog = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = StatusRed.copy(alpha = 0.12f),
                            contentColor   = StatusRed,
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                        enabled   = !isDeactivating,
                    ) {
                        if (isDeactivating) {
                            CircularProgressIndicator(color = StatusRed, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Rounded.Block, null, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("Deactivate Vehicle", style = MaterialTheme.typography.labelLarge)
                    }
                } else {
                    Surface(
                        shape    = RoundedCornerShape(14.dp),
                        color    = c.surface2,
                        border   = BorderStroke(1.dp, c.borderColor),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(Icons.Rounded.Block, null, tint = StatusRed, modifier = Modifier.size(18.dp))
                            Text("Vehicle is deactivated", style = MaterialTheme.typography.bodyMedium, color = c.textMuted)
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
