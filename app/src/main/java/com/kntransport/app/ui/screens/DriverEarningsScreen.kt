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
import androidx.compose.ui.draw.clip
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kntransport.app.R
import com.kntransport.app.data.*
import com.kntransport.app.network.ApiResult
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*
import com.kntransport.app.viewmodel.DriverViewModel
import java.time.format.DateTimeFormatter

@Composable
fun DriverEarningsScreen(
    onBack    : () -> Unit,
    viewModel : DriverViewModel = viewModel(),
) {
    val c             = LocalAppColors.current
    val earningsState by viewModel.earnings.collectAsState()
    val tripsState    by viewModel.trips.collectAsState()
    val periods       = listOf("Summary")
    var selected      by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.loadEarnings()
        viewModel.loadTrips()
    }

    val dto = (earningsState as? ApiResult.Success)?.data
    val completedTrips = (tripsState as? ApiResult.Success)?.data?.filter { it.status == "COMPLETED" } ?: emptyList()
    val dateFmt = DateTimeFormatter.ofPattern("EEE d MMM")

    KntScaffold(title = "Earnings", onBack = onBack) { pv ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pv)
                .verticalScroll(rememberScrollState()),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            ) {
                HeroBgImage(resId = R.drawable.hero_bg_6, modifier = Modifier.fillMaxSize(), darkOverlay = 0.52f)
                Column(Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 12.dp)) {
                    Text(
                        "Track your income",
                        style = MaterialTheme.typography.labelMedium.copy(color = KntYellow, letterSpacing = 0.5.sp),
                    )
                }
            }

            Column(Modifier.padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(20.dp))

            if (earningsState is ApiResult.Loading) {
                Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // Revenue hero card
                Surface(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                    Box(
                        Modifier.background(Brush.linearGradient(listOf(KntDark, Color(0xFF0D3A6A))))
                            .border(BorderStroke(1.dp, KntYellow.copy(0.3f)), RoundedCornerShape(18.dp))
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Payments, null, tint = KntYellow, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Total Earnings", style = MaterialTheme.typography.labelMedium, color = KntMuted)
                            }
                            Spacer(Modifier.height(6.dp))
                            GradientText(
                                text   = "R${String.format("%.2f", dto?.totalEarnings ?: 0.0)}",
                                style  = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                                colors = listOf(KntYellow, KntOrange),
                            )
                            Text("${dto?.completedTrips ?: 0} trips completed", style = MaterialTheme.typography.bodySmall, color = KntMuted)
                            Spacer(Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                                Column {
                                    Text("Confirmed", style = MaterialTheme.typography.labelSmall, color = KntMuted)
                                    Text("${dto?.confirmedTrips ?: 0}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = StatusGreen)
                                }
                                Column {
                                    Text("In Progress", style = MaterialTheme.typography.labelSmall, color = KntMuted)
                                    Text("${dto?.inProgressTrips ?: 0}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = KntOrange)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    EarningsChip("${dto?.completedTrips ?: 0}", "Completed", c.blue, Modifier.weight(1f))
                    EarningsChip(
                        "R${String.format("%.0f", dto?.averageEarningsPerTrip ?: 0.0)}",
                        "Avg / Trip", c.yellow, Modifier.weight(1f),
                    )
                    EarningsChip("${dto?.confirmedTrips ?: 0}", "Confirmed", KntOrange, Modifier.weight(1f))
                }
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
                    EarningsTripDtoRow(trip = trip)
                    Spacer(Modifier.height(10.dp))
                }
            }

            Spacer(Modifier.height(32.dp))
            } // close inner padding Column
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
private fun EarningsTripDtoRow(trip: com.kntransport.app.network.TripBookingDto) {
    val c = LocalAppColors.current
    val fmt = DateTimeFormatter.ofPattern("EEE d MMM")
    val dateStr = runCatching { java.time.LocalDate.parse(trip.date).format(fmt) }.getOrElse { trip.date }
    KntCard(onClick = {}) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(StatusGreen.copy(0.1f)),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.CheckCircle, null, tint = StatusGreen, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(trip.commuterName ?: "Commuter", style = MaterialTheme.typography.titleSmall, color = c.textBright)
                Text(trip.dropAddress, style = MaterialTheme.typography.bodySmall, color = c.textMuted, maxLines = 1)
                Text(dateStr, style = MaterialTheme.typography.labelSmall, color = c.textDim)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("R${String.format("%.2f", trip.quotedAmount ?: 0.0)}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = KntYellow)
                Surface(shape = RoundedCornerShape(6.dp), color = StatusGreen.copy(0.12f)) {
                    Text("Paid", style = MaterialTheme.typography.labelSmall, color = StatusGreen,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp))
                }
            }
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
