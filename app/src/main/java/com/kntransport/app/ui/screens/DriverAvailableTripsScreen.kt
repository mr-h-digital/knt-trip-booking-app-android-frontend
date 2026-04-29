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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kntransport.app.R
import com.kntransport.app.network.ApiResult
import com.kntransport.app.network.TripBookingDto
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*
import com.kntransport.app.viewmodel.DriverViewModel

@Composable
fun DriverAvailableTripsScreen(
    onBack      : () -> Unit,
    onTripDetail: (String) -> Unit,
    viewModel   : DriverViewModel = viewModel(),
) {
    val c             = LocalAppColors.current
    val availableState by viewModel.availableTrips.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadAvailableTrips() }

    KntScaffold(
        title   = "Available Trips",
        onBack  = onBack,
        actions = {
            IconButton(onClick = { viewModel.loadAvailableTrips() }) {
                Icon(Icons.Rounded.Refresh, null, tint = c.yellow, modifier = Modifier.size(20.dp))
            }
        },
    ) { pv ->
        Column(Modifier.fillMaxSize().padding(pv)) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            ) {
                HeroBgImage(resId = R.drawable.hero_bg_4, modifier = Modifier.fillMaxSize(), darkOverlay = 0.50f)
                Column(Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 12.dp)) {
                    Text(
                        "Open requests you can quote",
                        style = MaterialTheme.typography.labelMedium.copy(color = KntYellow, letterSpacing = 0.5.sp),
                    )
                }
            }

            when (val state = availableState) {
                is ApiResult.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = c.blue)
                }
                is ApiResult.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ErrorState(message = state.message, onRetry = { viewModel.loadAvailableTrips() })
                }
                is ApiResult.Success -> {
                    val trips = state.data
                    if (trips.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Icon(Icons.Rounded.EventAvailable, null, tint = c.textDim, modifier = Modifier.size(52.dp))
                                Text("No open trips right now", style = MaterialTheme.typography.titleSmall, color = c.textBright)
                                Text("Check back later for new requests", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                                Spacer(Modifier.height(4.dp))
                                KntSecondaryButton(
                                    text     = "Refresh",
                                    onClick  = { viewModel.loadAvailableTrips() },
                                    icon     = Icons.Rounded.Refresh,
                                    modifier = Modifier.fillMaxWidth(0.5f),
                                )
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = c.blue.copy(0.08f),
                                border = BorderStroke(1.dp, c.blue.copy(0.25f)),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Info, null, tint = c.blue, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Tap any trip to view details and submit a quote.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = c.textMuted,
                                    )
                                }
                            }

                            trips.forEachIndexed { idx, trip ->
                                StaggeredItem(index = idx) {
                                    AvailableTripCard(trip = trip, onClick = { onTripDetail(trip.id) })
                                }
                            }
                            Spacer(Modifier.height(32.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AvailableTripCard(trip: TripBookingDto, onClick: () -> Unit) {
    val c = LocalAppColors.current
    KntCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Date badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = c.yellow.copy(0.12f),
                border = BorderStroke(1.dp, c.yellow.copy(0.3f)),
                modifier = Modifier.size(52.dp),
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val parts = trip.date.split("-")
                        Text(
                            if (parts.size == 3) parts[2] else trip.date,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold, color = c.yellow,
                            ),
                        )
                        Text(
                            if (parts.size == 3) monthShort(parts[1].toIntOrNull() ?: 0) else "",
                            style = MaterialTheme.typography.labelSmall.copy(color = c.textMuted),
                        )
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Rounded.TripOrigin, null, tint = c.blue, modifier = Modifier.size(11.dp))
                    Text(trip.pickupAddress, style = MaterialTheme.typography.bodySmall, color = c.textBright, maxLines = 1)
                }
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Rounded.LocationOn, null, tint = c.yellow, modifier = Modifier.size(11.dp))
                    Text(trip.dropAddress, style = MaterialTheme.typography.bodySmall, color = c.textMuted, maxLines = 1)
                }
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Rounded.Schedule, null, tint = c.textDim, modifier = Modifier.size(11.dp))
                        Text(trip.time, style = MaterialTheme.typography.labelSmall, color = c.textMuted)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Rounded.Person, null, tint = c.textDim, modifier = Modifier.size(11.dp))
                        Text("${trip.passengers} pax", style = MaterialTheme.typography.labelSmall, color = c.textMuted)
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = StatusGreen.copy(0.10f),
                border = BorderStroke(1.dp, StatusGreen.copy(0.3f)),
            ) {
                Text(
                    "Quote",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = StatusGreen),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }
}

private fun monthShort(month: Int) = when (month) {
    1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"
    5 -> "May"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Aug"
    9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; 12 -> "Dec"
    else -> ""
}
