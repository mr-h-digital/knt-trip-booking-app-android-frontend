package com.kntransport.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kntransport.app.R
import com.kntransport.app.data.*
import com.kntransport.app.network.ApiResult
import com.kntransport.app.network.TripBookingDto
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*
import com.kntransport.app.viewmodel.TripViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun MyTripsScreen(
    onBack      : () -> Unit,
    onTripDetail: (String) -> Unit,
    onBookTrip  : () -> Unit = {},
    viewModel   : TripViewModel = viewModel(),
) {
    val c          = LocalAppColors.current
    val tripsState by viewModel.trips.collectAsState()
    val tabs       = listOf("All", "Active", "Completed")
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) { viewModel.loadTrips() }

    val allTrips = (tripsState as? ApiResult.Success)?.data ?: emptyList()
    val filtered = when (selectedTab) {
        1 -> allTrips.filter { it.status in listOf("PENDING_QUOTE", "QUOTE_SENT", "CONFIRMED", "IN_PROGRESS") }
        2 -> allTrips.filter { it.status == "COMPLETED" }
        else -> allTrips
    }

    KntScaffold(
        title   = "My Trips",
        onBack  = onBack,
        actions = {
            IconButton(onClick = onBookTrip) {
                Icon(Icons.Rounded.Add, contentDescription = "Book a Trip", tint = KntWhite)
            }
        },
    ) { pv ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pv)
        ) {
            // Hero banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            ) {
                HeroBgImage(resId = R.drawable.hero_bg_7, modifier = Modifier.fillMaxSize(), darkOverlay = 0.50f)
                Column(Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 12.dp)) {
                    Text("Your journeys, all in one place", style = MaterialTheme.typography.labelMedium.copy(color = KntYellow, letterSpacing = 0.5.sp))
                }
            }

            // Filter tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor   = c.surface1,
                contentColor     = c.blue,
                indicator        = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color    = c.yellow,
                    )
                },
                divider = {},
            ) {
                tabs.forEachIndexed { i, label ->
                    Tab(
                        selected = i == selectedTab,
                        onClick  = { selectedTab = i },
                        text     = { Text(label, style = MaterialTheme.typography.labelMedium) },
                        selectedContentColor   = c.yellow,
                        unselectedContentColor = c.textMuted,
                    )
                }
            }

            when (val state = tripsState) {
                is ApiResult.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                is ApiResult.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ErrorState(message = state.message, onRetry = { viewModel.loadTrips() })
                }
                is ApiResult.Success -> {
                    if (filtered.isEmpty()) {
                        TripEmptyState(onBook = onBookTrip)
                    } else {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            filtered.forEachIndexed { idx, trip ->
                                StaggeredItem(index = idx) {
                                    TripDtoCard(trip = trip, onClick = { onTripDetail(trip.id) })
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
fun TripDtoCard(trip: TripBookingDto, onClick: () -> Unit) {
    val c   = LocalAppColors.current
    val fmt = DateTimeFormatter.ofPattern("EEE d MMM yyyy")
    val date = try { LocalDate.parse(trip.date) } catch (_: Exception) { null }
    KntCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("To: ${trip.dropAddress}", style = MaterialTheme.typography.titleSmall, color = c.textBright, maxLines = 1)
                Spacer(Modifier.height(2.dp))
                Text(
                    "${date?.format(fmt) ?: trip.date} at ${trip.time}",
                    style = MaterialTheme.typography.bodySmall, color = c.textMuted,
                )
            }
            Spacer(Modifier.width(8.dp))
            TripStatusDtoChip(trip.status)
        }
        KntDivider()
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoRow(Icons.Rounded.LocationOn, "From", trip.pickupAddress)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoRow(Icons.Rounded.Person, "Pax", trip.passengers.toString())
        }
        if (trip.quotedAmount != null) {
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Rounded.Payments, null, tint = c.yellow, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Quoted: R${String.format("%.2f", trip.quotedAmount)}", style = MaterialTheme.typography.labelMedium, color = c.yellow)
                if (trip.status == "QUOTE_SENT") {
                    Spacer(Modifier.weight(1f))
                    Surface(shape = RoundedCornerShape(8.dp), color = c.yellow.copy(alpha = 0.15f)) {
                        Text("Tap to review", style = MaterialTheme.typography.labelSmall, color = c.yellow,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                }
            }
        }
        if (trip.driverName != null) {
            KntDivider()
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Person, null, tint = c.blue, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(trip.driverName, style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                if (trip.vehicleInfo != null) {
                    Spacer(Modifier.width(12.dp))
                    Icon(Icons.Rounded.DirectionsCar, null, tint = c.blue, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(trip.vehicleInfo, style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                }
            }
        }
    }
}

@Composable
fun TripStatusDtoChip(status: String) {
    val (label, bg, fg) = when (status.uppercase()) {
        "PENDING_QUOTE"  -> Triple("Awaiting Quote", androidx.compose.ui.graphics.Color(0xFF1A3A5C), KntMuted)
        "QUOTE_SENT"     -> Triple("Quote Ready",    androidx.compose.ui.graphics.Color(0xFF3A2A00), KntYellow)
        "QUOTE_ACCEPTED" -> Triple("Accepted",       androidx.compose.ui.graphics.Color(0xFF1A3A1A), StatusGreen)
        "CONFIRMED"      -> Triple("Confirmed",      androidx.compose.ui.graphics.Color(0xFF1A3A1A), StatusGreen)
        "IN_PROGRESS"    -> Triple("On the Way",     androidx.compose.ui.graphics.Color(0xFF0D2040), KntBlueBright)
        "COMPLETED"      -> Triple("Completed",      androidx.compose.ui.graphics.Color(0xFF1A2A1A), androidx.compose.ui.graphics.Color(0xFF7BC47B))
        else             -> Triple("Cancelled",      androidx.compose.ui.graphics.Color(0xFF3A1A1A), StatusRed)
    }
    Surface(shape = RoundedCornerShape(20.dp), color = bg) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}

@Composable
fun TripDetailCard(trip: TripBooking, onClick: () -> Unit) {
    val c   = LocalAppColors.current
    val fmt = DateTimeFormatter.ofPattern("EEE d MMM yyyy")
    KntCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "To: ${trip.dropAddress}",
                    style = MaterialTheme.typography.titleSmall,
                    color = c.textBright,
                    maxLines = 1,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${trip.date.format(fmt)} at ${trip.time}",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textMuted,
                )
            }
            Spacer(Modifier.width(8.dp))
            TripStatusChip(trip.status)
        }

        KntDivider()

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoRow(Icons.Rounded.LocationOn, "From", trip.pickupAddress)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoRow(Icons.Rounded.Person, "Pax", trip.passengers.toString())
        }

        if (trip.quotedAmount != null) {
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Rounded.Payments, null, tint = c.yellow, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    "Quoted: R${String.format("%.2f", trip.quotedAmount)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = c.yellow,
                )
                if (trip.status == TripStatus.QUOTE_SENT) {
                    Spacer(Modifier.weight(1f))
                    Surface(shape = RoundedCornerShape(8.dp), color = c.yellow.copy(alpha = 0.15f)) {
                        Text(
                            "Tap to review",
                            style = MaterialTheme.typography.labelSmall,
                            color = c.yellow,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                }
            }
        }

        if (trip.driverName != null) {
            KntDivider()
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Person, null, tint = c.blue, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(trip.driverName, style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                Spacer(Modifier.width(12.dp))
                Icon(Icons.Rounded.DirectionsCar, null, tint = c.blue, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(trip.vehicleInfo ?: "", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
            }
        }
    }
}

@Composable
fun EmptyState(icon: androidx.compose.ui.graphics.vector.ImageVector, message: String) {
    val c = LocalAppColors.current
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, null, tint = c.textDim, modifier = Modifier.size(56.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = c.textMuted)
        }
    }
}
