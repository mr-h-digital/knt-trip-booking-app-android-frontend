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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.kntransport.app.R
import com.kntransport.app.data.*
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*
import java.time.format.DateTimeFormatter

@Composable
fun DriverTripDetailScreen(
    tripId : String,
    onBack : () -> Unit,
) {
    val c    = LocalAppColors.current
    val trip = remember(tripId) { SampleData.driverTrips.find { it.id == tripId } }

    if (trip == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    var currentStatus by remember { mutableStateOf(trip.status) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingStatus     by remember { mutableStateOf<TripStatus?>(null) }

    val dateFmt = DateTimeFormatter.ofPattern("EEE, d MMM yyyy")
    val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

    if (showConfirmDialog && pendingStatus != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor   = c.surface2,
            title = {
                Text(
                    when (pendingStatus) {
                        TripStatus.IN_PROGRESS -> "Start Trip?"
                        TripStatus.COMPLETED   -> "Complete Trip?"
                        TripStatus.CANCELLED   -> "Cancel Trip?"
                        else -> "Update Status?"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = c.textBright,
                )
            },
            text = {
                Text(
                    when (pendingStatus) {
                        TripStatus.IN_PROGRESS -> "Confirm that you have picked up ${trip.commuterName} and the trip has started."
                        TripStatus.COMPLETED   -> "Confirm that you have safely dropped off ${trip.commuterName} and the trip is complete."
                        TripStatus.CANCELLED   -> "Are you sure you want to cancel this trip? The commuter will be notified."
                        else -> "Update the trip status?"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textMuted,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        currentStatus    = pendingStatus!!
                        showConfirmDialog = false
                        pendingStatus    = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (pendingStatus) {
                            TripStatus.CANCELLED -> StatusRed
                            TripStatus.COMPLETED -> StatusGreen
                            else                 -> c.blue
                        }
                    ),
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel", color = c.textMuted)
                }
            },
        )
    }

    KntScaffold(title = "Trip Detail", onBack = onBack) { pv ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pv)
                .verticalScroll(rememberScrollState()),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            ) {
                HeroBgImage(resId = R.drawable.hero_bg_4, modifier = Modifier.fillMaxSize(), darkOverlay = 0.52f)
                Column(Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 12.dp)) {
                    Text(
                        trip.commuterName,
                        style = MaterialTheme.typography.titleMedium.copy(color = KntWhite),
                    )
                    Text(
                        "${trip.pickupAddress}  →  ${trip.dropAddress}",
                        style = MaterialTheme.typography.bodySmall.copy(color = KntYellow),
                        maxLines = 1,
                    )
                }
            }

            Column(Modifier.padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(16.dp))

            // ── Status banner ─────────────────────────────────────────────
            val (bannerColor, bannerLabel) = when (currentStatus) {
                TripStatus.IN_PROGRESS -> KntBlue to "Trip In Progress"
                TripStatus.CONFIRMED   -> KntYellow to "Confirmed — Ready to Start"
                TripStatus.COMPLETED   -> StatusGreen to "Trip Completed"
                TripStatus.CANCELLED   -> StatusRed to "Trip Cancelled"
                else                   -> KntMuted to currentStatus.name.replace("_", " ")
            }
            Surface(
                shape    = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                color    = bannerColor.copy(0.12f),
                border   = BorderStroke(1.dp, bannerColor.copy(0.4f)),
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(bannerColor))
                    Spacer(Modifier.width(10.dp))
                    Text(bannerLabel, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = bannerColor)
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Commuter info ─────────────────────────────────────────────
            SectionHeader(title = "Commuter")
            Spacer(Modifier.height(10.dp))
            Surface(
                shape  = RoundedCornerShape(14.dp),
                color  = c.surface2,
                border = BorderStroke(1.dp, c.borderColor),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape)
                            .background(Brush.linearGradient(listOf(KntBlue.copy(0.7f), KntBlueBright.copy(0.5f)))),
                        contentAlignment = Alignment.Center,
                    ) {
                        val initials = trip.commuterName.split(" ")
                            .mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
                        Text(initials, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(trip.commuterName, style = MaterialTheme.typography.titleSmall, color = c.textBright)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Rounded.Person, null, tint = c.textDim, modifier = Modifier.size(12.dp))
                            Text("${trip.passengers} passenger${if (trip.passengers > 1) "s" else ""}", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                        }
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Rounded.Phone, null, tint = c.blue, modifier = Modifier.size(22.dp))
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Rounded.Message, null, tint = c.yellow, modifier = Modifier.size(22.dp))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Route ─────────────────────────────────────────────────────
            SectionHeader(title = "Route")
            Spacer(Modifier.height(10.dp))
            Surface(
                shape  = RoundedCornerShape(14.dp),
                color  = c.surface2,
                border = BorderStroke(1.dp, c.borderColor),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.size(10.dp).clip(CircleShape).background(c.blue))
                            Box(Modifier.width(2.dp).height(24.dp).background(c.borderColor))
                            Box(Modifier.size(10.dp).clip(CircleShape).background(KntYellow))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                            Column {
                                Text("Pickup", style = MaterialTheme.typography.labelSmall, color = c.textDim)
                                Text(trip.pickupAddress, style = MaterialTheme.typography.bodyMedium, color = c.textBright)
                            }
                            Column {
                                Text("Drop-off", style = MaterialTheme.typography.labelSmall, color = c.textDim)
                                Text(trip.dropAddress, style = MaterialTheme.typography.bodyMedium, color = c.textBright)
                            }
                        }
                    }
                    KntDivider()
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        InfoRow(Icons.Rounded.CalendarMonth, "Date", trip.date.format(dateFmt))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        InfoRow(Icons.Rounded.Schedule, "Time", trip.time.format(timeFmt))
                    }
                    if (trip.notes.isNotBlank()) {
                        Row {
                            InfoRow(Icons.Rounded.Notes, "Notes", trip.notes)
                        }
                    }
                }
            }

            // ── Earnings ──────────────────────────────────────────────────
            if (trip.quotedAmount != null) {
                Spacer(Modifier.height(20.dp))
                Surface(
                    shape  = RoundedCornerShape(14.dp),
                    color  = c.surface2,
                    border = BorderStroke(1.dp, KntYellow.copy(0.3f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Payments, null, tint = KntYellow, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Trip Earnings", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                            GradientText(
                                text   = "R${String.format("%.2f", trip.quotedAmount)}",
                                style  = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                colors = listOf(KntYellow, KntOrange),
                            )
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = StatusGreen.copy(0.12f)) {
                            Text("On completion", style = MaterialTheme.typography.labelSmall, color = StatusGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }
            }

            // ── Action buttons ────────────────────────────────────────────
            Spacer(Modifier.height(28.dp))

            when (currentStatus) {
                TripStatus.CONFIRMED, TripStatus.QUOTE_ACCEPTED -> {
                    KntPrimaryButton(
                        text    = "Start Trip",
                        onClick = { pendingStatus = TripStatus.IN_PROGRESS; showConfirmDialog = true },
                        icon    = Icons.Rounded.PlayArrow,
                    )
                    Spacer(Modifier.height(8.dp))
                    KntSecondaryButton(
                        text    = "Cancel Trip",
                        onClick = { pendingStatus = TripStatus.CANCELLED; showConfirmDialog = true },
                        icon    = Icons.Rounded.Cancel,
                    )
                }
                TripStatus.IN_PROGRESS -> {
                    KntPrimaryButton(
                        text    = "Complete Trip",
                        onClick = { pendingStatus = TripStatus.COMPLETED; showConfirmDialog = true },
                        icon    = Icons.Rounded.CheckCircle,
                    )
                }
                TripStatus.COMPLETED -> {
                    Surface(
                        shape    = RoundedCornerShape(14.dp),
                        color    = StatusGreen.copy(0.1f),
                        border   = BorderStroke(1.dp, StatusGreen.copy(0.3f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CheckCircle, null, tint = StatusGreen, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Trip completed", style = MaterialTheme.typography.titleSmall, color = StatusGreen)
                        }
                    }
                }
                TripStatus.CANCELLED -> {
                    Surface(
                        shape    = RoundedCornerShape(14.dp),
                        color    = StatusRed.copy(0.1f),
                        border   = BorderStroke(1.dp, StatusRed.copy(0.3f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Cancel, null, tint = StatusRed, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Trip cancelled", style = MaterialTheme.typography.titleSmall, color = StatusRed)
                        }
                    }
                }
                else -> {}
            }

            Spacer(Modifier.height(32.dp))
            } // close inner padding Column
        }
    }
}
