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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.kntransport.app.data.*
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*
import java.time.format.DateTimeFormatter

private data class EarningsPeriod(
    val total      : String,
    val paid       : String,
    val pending    : String,
    val tripCount  : Int,
)

private val weekEarnings  = EarningsPeriod("R645",    "R465",    "R180",   4)
private val monthEarnings = EarningsPeriod("R3,240",  "R2,700",  "R540",   18)
private val yearEarnings  = EarningsPeriod("R28,500", "R25,800", "R2,700", 152)

@Composable
fun DriverEarningsScreen(onBack: () -> Unit) {
    val c       = LocalAppColors.current
    val periods = listOf("This Week", "This Month", "This Year")
    var selected by remember { mutableIntStateOf(1) }

    val period = when (selected) {
        0    -> weekEarnings
        2    -> yearEarnings
        else -> monthEarnings
    }

    val completedTrips = SampleData.driverTrips.filter { it.status == TripStatus.COMPLETED }
    val dateFmt = DateTimeFormatter.ofPattern("EEE d MMM")

    KntScaffold(title = "Earnings", onBack = onBack) { pv ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pv)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            // Period selector
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                periods.forEachIndexed { i, label ->
                    val sel = i == selected
                    Surface(
                        onClick = { selected = i },
                        shape   = RoundedCornerShape(20.dp),
                        color   = if (sel) c.blue else c.surface2,
                        border  = BorderStroke(1.dp, if (sel) c.blue else c.borderColor),
                    ) {
                        Text(
                            label,
                            style    = MaterialTheme.typography.labelMedium,
                            color    = if (sel) Color.White else c.textMuted,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Revenue hero card
            Surface(
                shape    = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    Modifier.background(
                        Brush.linearGradient(listOf(KntDark, Color(0xFF0D3A6A)))
                    ).border(BorderStroke(1.dp, KntYellow.copy(0.3f)), RoundedCornerShape(18.dp))
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Payments, null, tint = KntYellow, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Total Earnings", style = MaterialTheme.typography.labelMedium, color = KntMuted)
                        }
                        Spacer(Modifier.height(6.dp))
                        GradientText(
                            text   = period.total,
                            style  = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                            colors = listOf(KntYellow, KntOrange),
                        )
                        Text("${period.tripCount} trips · ${periods[selected]}", style = MaterialTheme.typography.bodySmall, color = KntMuted)
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            Column {
                                Text("Paid", style = MaterialTheme.typography.labelSmall, color = KntMuted)
                                Text(period.paid, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = StatusGreen)
                            }
                            Column {
                                Text("Pending", style = MaterialTheme.typography.labelSmall, color = KntMuted)
                                Text(period.pending, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = KntOrange)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Quick stat row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                EarningsChip("${period.tripCount}", "Trips",         c.blue,   Modifier.weight(1f))
                EarningsChip(
                    "R${(period.total.replace("[^0-9]".toRegex(), "").toIntOrNull()?.div(period.tripCount.coerceAtLeast(1)) ?: 0)}",
                    "Avg / Trip", c.yellow, Modifier.weight(1f),
                )
                EarningsChip("4.3 ★", "Rating", KntOrange, Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))
            SectionHeader(title = "Recent Completed Trips")
            Spacer(Modifier.height(12.dp))

            if (completedTrips.isEmpty()) {
                Box(
                    Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("No completed trips yet", style = MaterialTheme.typography.bodyMedium, color = c.textMuted)
                }
            } else {
                completedTrips.forEach { trip ->
                    EarningsTripRow(trip = trip, dateFmt = dateFmt)
                    Spacer(Modifier.height(10.dp))
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun EarningsChip(value: String, label: String, tint: Color, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = c.surface2,
        border   = BorderStroke(1.dp, tint.copy(0.25f)),
        modifier = modifier,
    ) {
        Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            GradientText(
                text   = value,
                style  = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                colors = listOf(tint, tint.copy(0.7f)),
            )
            Text(label, style = MaterialTheme.typography.labelSmall, color = c.textMuted)
        }
    }
}

@Composable
private fun EarningsTripRow(trip: TripBooking, dateFmt: DateTimeFormatter) {
    val c = LocalAppColors.current
    KntCard(onClick = {}) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                    .background(StatusGreen.copy(0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.CheckCircle, null, tint = StatusGreen, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(trip.commuterName, style = MaterialTheme.typography.titleSmall, color = c.textBright)
                Text(trip.dropAddress,  style = MaterialTheme.typography.bodySmall,  color = c.textMuted, maxLines = 1)
                Text(trip.date.format(dateFmt), style = MaterialTheme.typography.labelSmall, color = c.textDim)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "R${String.format("%.2f", trip.quotedAmount ?: 0.0)}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = KntYellow,
                )
                Surface(shape = RoundedCornerShape(6.dp), color = StatusGreen.copy(0.12f)) {
                    Text("Paid", style = MaterialTheme.typography.labelSmall, color = StatusGreen,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp))
                }
            }
        }
    }
}
