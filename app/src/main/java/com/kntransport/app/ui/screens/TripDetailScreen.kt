package com.kntransport.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.*
import com.kntransport.app.data.*
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// Ordered pipeline steps
private val PIPELINE = listOf(
    TripStatus.PENDING_QUOTE,
    TripStatus.QUOTE_SENT,
    TripStatus.QUOTE_ACCEPTED,
    TripStatus.CONFIRMED,
    TripStatus.IN_PROGRESS,
    TripStatus.COMPLETED,
)

private val PIPELINE_LABELS = listOf(
    "Pending",
    "Quoted",
    "Accepted",
    "Confirmed",
    "En Route",
    "Done",
)

@Composable
fun TripDetailScreen(
    tripId       : String,
    onBack       : () -> Unit,
    onQuoteReview: (String) -> Unit,
    onRateTrip   : (String) -> Unit = {},
) {
    val c    = LocalAppColors.current
    val trip = SampleData.myTrips.firstOrNull { it.id == tripId } ?: run {
        Box(Modifier.fillMaxSize().background(c.bgDeep), Alignment.Center) {
            Text("Trip not found", color = c.textMuted)
        }
        return
    }
    val fmt = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")

    // Live countdown for confirmed/in-progress trips
    var countdownText by remember { mutableStateOf("") }
    LaunchedEffect(trip.id) {
        if (trip.status in listOf(TripStatus.CONFIRMED, TripStatus.IN_PROGRESS)) {
            val pickupDateTime = LocalDateTime.of(trip.date, trip.time)
            while (true) {
                val now  = LocalDateTime.now()
                val diff = java.time.Duration.between(now, pickupDateTime)
                countdownText = if (diff.isNegative) {
                    if (trip.status == TripStatus.IN_PROGRESS) "En Route" else "Departed"
                } else {
                    val h = diff.toHours()
                    val m = diff.toMinutesPart()
                    val s = diff.toSecondsPart()
                    if (h > 0) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
                }
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    KntScaffold(title = "Trip Details", onBack = onBack) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Countdown banner ──────────────────────────────────────────
            if (countdownText.isNotEmpty()) {
                val isEnRoute = trip.status == TripStatus.IN_PROGRESS
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                if (isEnRoute) listOf(KntBlue.copy(0.25f), KntBlueBright.copy(0.10f))
                                else           listOf(StatusGreen.copy(0.18f), KntBlue.copy(0.08f))
                            )
                        )
                        .border(BorderStroke(1.dp,
                            if (isEnRoute) KntBlue.copy(0.4f) else StatusGreen.copy(0.35f)
                        ), RoundedCornerShape(14.dp))
                        .padding(16.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            if (isEnRoute) "Driver is on the way" else "Pickup in",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isEnRoute) KntBlueBright else StatusGreen,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            countdownText,
                            style = MaterialTheme.typography.displaySmall.copy(
                                brush = Brush.linearGradient(
                                    if (isEnRoute) listOf(KntBlueBright, KntBlue)
                                    else           listOf(StatusGreen, KntBlueBright)
                                )
                            ),
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Status pipeline ───────────────────────────────────────────
            if (trip.status != TripStatus.CANCELLED) {
                TripStatusPipeline(currentStatus = trip.status)
                Spacer(Modifier.height(16.dp))
            }

            // ── Trip summary card ─────────────────────────────────────────
            KntCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Trip #${trip.id.uppercase()}", style = MaterialTheme.typography.labelMedium, color = c.textMuted)
                        Spacer(Modifier.height(2.dp))
                        Text(trip.date.format(fmt), style = MaterialTheme.typography.titleMedium, color = c.textBright)
                    }
                    TripStatusChip(trip.status)
                }
                KntDivider()
                InfoRow(Icons.Rounded.LocationOn, "Pickup",     trip.pickupAddress)
                InfoRow(Icons.Rounded.Flag,       "Drop-off",   trip.dropAddress)
                InfoRow(Icons.Rounded.Schedule,   "Time",       trip.time.toString())
                InfoRow(Icons.Rounded.Person,     "Passengers", trip.passengers.toString())
            }

            // ── Quote card ────────────────────────────────────────────────
            if (trip.quotedAmount != null && trip.status == TripStatus.QUOTE_SENT) {
                Spacer(Modifier.height(16.dp))
                Surface(
                    shape    = RoundedCornerShape(16.dp),
                    color    = c.yellow.copy(alpha = 0.08f),
                    border   = BorderStroke(1.5.dp, c.yellow.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.RequestQuote, null, tint = c.yellow, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Quote Received", style = MaterialTheme.typography.titleMedium, color = c.yellow)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("R${String.format("%.2f", trip.quotedAmount)}", style = MaterialTheme.typography.displaySmall.copy(color = c.yellow))
                        Text("one-way trip", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                        Spacer(Modifier.height(16.dp))
                        Text("Payment is due upfront once you accept this quote.", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                        Spacer(Modifier.height(16.dp))
                        KntPrimaryButton(text = "Review & Accept Quote", onClick = { onQuoteReview(trip.id) }, icon = Icons.Rounded.CheckCircle)
                    }
                }
            }

            // ── Driver & vehicle info ─────────────────────────────────────
            if (trip.driverName != null) {
                Spacer(Modifier.height(16.dp))
                SectionHeader(title = "Your Driver")
                KntCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(48.dp).clip(CircleShape).background(c.blue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            val initials = trip.driverName.split(" ")
                                .mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
                            if (initials.isNotEmpty()) {
                                Text(initials, style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold), color = c.blue)
                            } else {
                                Icon(Icons.Rounded.Person, null, tint = c.blue, modifier = Modifier.size(26.dp))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(trip.driverName, style = MaterialTheme.typography.titleMedium, color = c.textBright)
                            Text("K&T Transport Driver", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                        }
                    }
                    // ── Vehicle card (Uber-style) ─────────────────────────
                    if (trip.vehicleInfo != null || trip.vehiclePlate != null) {
                        KntDivider()
                        val vehicle = SampleData.driverTrips.find { it.id == trip.id }
                        val vehicleDisplay = trip.vehicleInfo ?: ""
                        val plateDisplay   = trip.vehiclePlate ?: ""
                        Surface(
                            shape  = RoundedCornerShape(10.dp),
                            color  = c.surface1,
                            border = BorderStroke(1.dp, c.borderColor),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                                        .background(c.yellow.copy(0.12f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(Icons.Rounded.DirectionsBus, null, tint = c.yellow, modifier = Modifier.size(20.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(vehicleDisplay, style = MaterialTheme.typography.titleSmall, color = c.textBright)
                                    Text("Assigned vehicle", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                                }
                                if (plateDisplay.isNotEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = c.blue.copy(0.12f),
                                        border = BorderStroke(1.dp, c.blue.copy(0.3f)),
                                    ) {
                                        Text(
                                            plateDisplay,
                                            style    = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                                            color    = c.blue,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Receipt ───────────────────────────────────────────────────
            if (trip.status == TripStatus.COMPLETED && trip.quotedAmount != null) {
                Spacer(Modifier.height(16.dp))
                SectionHeader(title = "Receipt")
                KntCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Receipt, null, tint = StatusGreen, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Trip Completed", style = MaterialTheme.typography.titleSmall, color = StatusGreen)
                        Spacer(Modifier.weight(1f))
                        Text("R${String.format("%.2f", trip.quotedAmount)}", style = MaterialTheme.typography.titleMedium, color = c.yellow)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Thank you for travelling with K&T Transport.", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                }
                Spacer(Modifier.height(16.dp))
                KntSecondaryButton(
                    text    = "Rate This Trip",
                    onClick = { onRateTrip(trip.id) },
                    icon    = Icons.Rounded.Star,
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TripStatusPipeline(currentStatus: TripStatus) {
    val c          = LocalAppColors.current
    val activeIdx  = PIPELINE.indexOf(currentStatus).coerceAtLeast(0)

    // Glow pulse on the active step
    val glowAnim = rememberInfiniteTransition(label = "glow")
    val glowAlpha by glowAnim.animateFloat(
        initialValue  = 0.35f,
        targetValue   = 0.85f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "glowAlpha",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(listOf(c.surface1.copy(0.93f), c.surface1.copy(0.75f)))
            )
            .border(
                BorderStroke(1.dp, Brush.linearGradient(listOf(Color.White.copy(0.12f), c.blue.copy(0.18f)))),
                RoundedCornerShape(16.dp),
            )
            .padding(16.dp),
    ) {
        Text("Journey Progress", style = MaterialTheme.typography.labelMedium, color = c.textMuted)
        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PIPELINE.forEachIndexed { idx, _ ->
                val isDone   = idx < activeIdx
                val isActive = idx == activeIdx

                // Step dot
                Box(
                    modifier = Modifier
                        .size(if (isActive) 14.dp else 10.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isActive -> c.blue.copy(alpha = glowAlpha)
                                isDone   -> StatusGreen
                                else     -> c.surface2
                            }
                        )
                        .then(
                            if (isActive) Modifier.border(2.dp, c.blue.copy(0.6f), CircleShape) else Modifier
                        ),
                )

                // Connecting line (not after last)
                if (idx < PIPELINE.lastIndex) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(
                                if (isDone)
                                    Brush.horizontalGradient(listOf(StatusGreen, StatusGreen.copy(0.6f)))
                                else
                                    Brush.horizontalGradient(listOf(c.surface2, c.surface2))
                            )
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Labels row
        Row(modifier = Modifier.fillMaxWidth()) {
            PIPELINE_LABELS.forEachIndexed { idx, label ->
                val isActive = idx == activeIdx
                val isDone   = idx < activeIdx
                Text(
                    text  = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = when {
                            isActive -> c.blue
                            isDone   -> StatusGreen
                            else     -> c.textDim
                        },
                    ),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                )
            }
        }
    }
}
