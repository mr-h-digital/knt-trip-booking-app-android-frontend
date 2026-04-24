package com.kntransport.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.draw.clip
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kntransport.app.R
import com.kntransport.app.data.*
import com.kntransport.app.network.ApiResult
import com.kntransport.app.network.TripBookingDto
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*
import com.kntransport.app.viewmodel.DriverViewModel
import java.time.LocalDate

@Composable
fun DriverTripsScreen(
    onBack      : () -> Unit,
    onTripDetail: (String) -> Unit,
    viewModel   : DriverViewModel = viewModel(),
) {
    val c          = LocalAppColors.current
    val tripsState by viewModel.trips.collectAsState()
    val tabs       = listOf("Today", "Upcoming", "Completed")
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) { viewModel.loadTrips() }

    val today    = LocalDate.now()
    val allTrips = (tripsState as? ApiResult.Success)?.data ?: emptyList()
    val filtered = when (selectedTab) {
        0 -> allTrips.filter {
            runCatching { LocalDate.parse(it.date) }.getOrNull() == today &&
            it.status !in listOf("COMPLETED", "CANCELLED")
        }
        1 -> allTrips.filter {
            runCatching { LocalDate.parse(it.date) }.getOrNull()?.isAfter(today) == true &&
            it.status !in listOf("COMPLETED", "CANCELLED")
        }
        else -> allTrips.filter { it.status == "COMPLETED" }
    }

    KntScaffold(title = "My Trips", onBack = onBack) { pv ->
        Column(Modifier.fillMaxSize().padding(pv)) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            ) {
                HeroBgImage(resId = R.drawable.hero_bg_7, modifier = Modifier.fillMaxSize(), darkOverlay = 0.52f)
                Column(Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 12.dp)) {
                    Text(
                        "Your assigned trips",
                        style = MaterialTheme.typography.labelMedium.copy(color = KntYellow, letterSpacing = 0.5.sp),
                    )
                }
            }

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
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Rounded.DirectionsBus, null, tint = c.textDim, modifier = Modifier.size(52.dp))
                                Text(
                                    when (selectedTab) { 0 -> "No trips for today"; 1 -> "No upcoming trips"; else -> "No completed trips" },
                                    style = MaterialTheme.typography.bodyMedium, color = c.textMuted,
                                )
                            }
                        }
                    } else {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            filtered.forEachIndexed { idx, trip ->
                                StaggeredItem(index = idx) {
                                    DriverTripDtoCard(trip = trip, onClick = { onTripDetail(trip.id) })
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
fun DriverTripDtoCard(trip: TripBookingDto, onClick: () -> Unit) {
    val c   = LocalAppColors.current
    val fmt = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
    val time = runCatching { java.time.LocalTime.parse(trip.time).format(fmt) }.getOrElse { trip.time }
    KntCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(10.dp), color = c.blue.copy(0.12f), modifier = Modifier.size(44.dp)) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(time, style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold), color = c.blue)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(trip.commuterName ?: "Commuter", style = MaterialTheme.typography.titleSmall, color = c.textBright)
                Text(trip.pickupAddress, style = MaterialTheme.typography.bodySmall, color = c.textMuted, maxLines = 1)
                Text(trip.dropAddress,   style = MaterialTheme.typography.bodySmall, color = c.textMuted, maxLines = 1)
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                TripStatusDtoChip(trip.status)
                if (trip.quotedAmount != null) {
                    Text("R${String.format("%.0f", trip.quotedAmount)}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold), color = c.yellow)
                }
            }
        }
    }
}
