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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import com.kntransport.app.data.*
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*

@Composable
fun LiftClubDetailScreen(
    clubId      : String,
    onBack      : () -> Unit,
    onQuoteReview: (String) -> Unit,
) {
    val c    = LocalAppColors.current
    val club = SampleData.liftClubs.firstOrNull { it.id == clubId } ?: run {
        Box(Modifier.fillMaxSize().background(c.bgDeep), Alignment.Center) {
            Text("Lift club not found", color = c.textMuted)
        }
        return
    }

    var subscribed  by remember { mutableStateOf(clubId in SampleData.myLiftClubSubscriptions) }
    var showConfirm by remember { mutableStateOf(false) }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            containerColor   = c.surface2,
            titleContentColor = c.textBright,
            textContentColor  = c.textMuted,
            title = { Text("Subscribe to Lift Club") },
            text  = { Text("You'll be added to the subscriber list. Once the quota is met, K&T will confirm and send a quote for your approval.") },
            confirmButton = {
                Button(
                    onClick = { subscribed = true; showConfirm = false },
                    colors  = ButtonDefaults.buttonColors(containerColor = c.blue),
                ) { Text("Subscribe") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Cancel", color = c.textMuted)
                }
            },
        )
    }

    KntScaffold(title = "Lift Club", onBack = onBack) { pv ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pv).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Hero card
            KntCard {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        Modifier.size(52.dp).clip(RoundedCornerShape(14.dp))
                            .background(c.yellow.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Groups, null, tint = c.yellow, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(club.title, style = MaterialTheme.typography.headlineSmall, color = c.textBright)
                        Text("by ${club.creatorName}", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                        Spacer(Modifier.height(6.dp))
                        LiftClubStatusChip(club.status)
                    }
                }
                KntDivider()
                Text(club.description, style = MaterialTheme.typography.bodyMedium, color = c.textMuted)
            }

            Spacer(Modifier.height(16.dp))
            SectionHeader(title = "Route & Schedule")
            KntCard {
                InfoRow(Icons.Rounded.LocationOn, "Pickup Area",  club.pickupArea)
                InfoRow(Icons.Rounded.Flag,        "Drop Area",    club.dropArea)
                InfoRow(Icons.Rounded.Schedule,    "Departs",      club.departureTime.toString())
                if (club.returnTime != null)
                    InfoRow(Icons.Rounded.Schedule, "Returns",     club.returnTime.toString())
                InfoRow(Icons.Rounded.CalendarViewWeek, "Days",   club.daysOfWeek.joinToString(" · "))
            }

            Spacer(Modifier.height(16.dp))
            SectionHeader(title = "Capacity")
            KntCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("${club.subscriberCount} / ${club.maxPassengers}", style = MaterialTheme.typography.displaySmall, color = c.yellow)
                        Text("Subscribers", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                    }
                    // Progress bar
                    Box(
                        Modifier.width(140.dp).height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(c.surface3),
                    ) {
                        val progress = (club.subscriberCount.toFloat() / club.maxPassengers).coerceIn(0f, 1f)
                        Box(
                            Modifier.fillMaxHeight()
                                .fillMaxWidth(progress)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (progress >= 1f) StatusGreen else c.yellow)
                        )
                    }
                }
                if (club.status == LiftClubStatus.OPEN) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "${club.maxPassengers - club.subscriberCount} spots remaining",
                        style = MaterialTheme.typography.bodySmall, color = c.textMuted,
                    )
                }
            }

            // Quote section
            if (club.quotedAmount != null) {
                Spacer(Modifier.height(16.dp))
                SectionHeader(title = "Quote")
                Surface(
                    shape  = RoundedCornerShape(16.dp),
                    color  = c.yellow.copy(alpha = 0.08f),
                    border = BorderStroke(1.5.dp, c.yellow.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "R${String.format("%.2f", club.quotedAmount)} / ${club.paymentCycle?.name?.lowercase() ?: ""}",
                            style = MaterialTheme.typography.displaySmall.copy(color = c.yellow),
                        )
                        Text("per person", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Payment options: Monthly · Weekly · Fortnightly",
                            style = MaterialTheme.typography.bodySmall, color = c.textMuted,
                        )
                        if (subscribed && club.status == LiftClubStatus.QUOTE_SENT) {
                            Spacer(Modifier.height(16.dp))
                            KntPrimaryButton(
                                text    = "Review & Approve Quote",
                                onClick = { onQuoteReview(club.id) },
                                icon    = Icons.Rounded.CheckCircle,
                            )
                        }
                    }
                }
            }

            // Driver info
            if (club.driverName != null) {
                Spacer(Modifier.height(16.dp))
                SectionHeader(title = "Driver & Vehicle")
                KntCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(44.dp).clip(CircleShape).background(c.blue.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.Person, null, tint = c.blue, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(club.driverName, style = MaterialTheme.typography.titleSmall, color = c.textBright)
                            Text("K&T Transport Driver", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                        }
                    }
                    if (club.vehicleInfo != null) {
                        KntDivider()
                        InfoRow(Icons.Rounded.DirectionsCar, "Vehicle", club.vehicleInfo)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // CTA
            if (!subscribed && club.status == LiftClubStatus.OPEN) {
                KntPrimaryButton(
                    text    = "Subscribe to Lift Club",
                    onClick = { showConfirm = true },
                    icon    = Icons.Rounded.PersonAdd,
                )
            } else if (subscribed) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = StatusGreen.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, StatusGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CheckCircle, null, tint = StatusGreen, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("You're subscribed to this lift club", style = MaterialTheme.typography.bodyMedium, color = StatusGreen)
                    }
                }
            } else if (club.status == LiftClubStatus.QUOTA_MET) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = c.yellow.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, c.yellow.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Groups, null, tint = c.yellow, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Quota met — driver confirmation pending", style = MaterialTheme.typography.bodyMedium, color = c.yellow)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
