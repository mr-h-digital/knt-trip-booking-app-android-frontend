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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kntransport.app.R
import com.kntransport.app.network.ApiResult
import com.kntransport.app.network.TripBookingDto
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*
import com.kntransport.app.viewmodel.AdminViewModel

@Composable
fun AdminTripsScreen(
    onBack       : () -> Unit,
    onTripDetail : (String) -> Unit = {},
    viewModel    : AdminViewModel = viewModel(),
) {
    val c          = LocalAppColors.current
    val tripsState by viewModel.allTrips.collectAsState()
    val tabs       = listOf("All", "Pending", "Active", "Completed")
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) { viewModel.loadAllTrips() }

    val allTrips = (tripsState as? ApiResult.Success)?.data ?: emptyList()
    val filtered = when (selectedTab) {
        1 -> allTrips.filter { it.status in listOf("PENDING_QUOTE", "QUOTE_SENT", "QUOTE_ACCEPTED") }
        2 -> allTrips.filter { it.status in listOf("CONFIRMED", "IN_PROGRESS") }
        3 -> allTrips.filter { it.status in listOf("COMPLETED", "CANCELLED") }
        else -> allTrips
    }

    KntScaffold(title = "All Trips", onBack = onBack) { pv ->
        Column(Modifier.fillMaxSize().padding(pv)) {
            Box(
                modifier = Modifier.fillMaxWidth().height(110.dp)
                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            ) {
                HeroBgImage(resId = R.drawable.hero_bg_7, modifier = Modifier.fillMaxSize(), darkOverlay = 0.52f)
                Column(Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 12.dp)) {
                    Text("All trip bookings", style = MaterialTheme.typography.labelMedium.copy(color = KntYellow, letterSpacing = 0.5.sp))
                }
            }

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor   = c.surface1,
                contentColor     = c.blue,
                indicator = { tps ->
                    TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tps[selectedTab]), color = c.yellow)
                },
                divider = {},
            ) {
                tabs.forEachIndexed { i, label ->
                    Tab(selected = i == selectedTab, onClick = { selectedTab = i },
                        text = { Text(label, style = MaterialTheme.typography.labelMedium) },
                        selectedContentColor = c.yellow, unselectedContentColor = c.textMuted)
                }
            }

            when (val state = tripsState) {
                is ApiResult.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                is ApiResult.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ErrorState(message = state.message, onRetry = { viewModel.loadAllTrips() })
                }
                is ApiResult.Success -> {
                    if (filtered.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Rounded.DirectionsBus, null, tint = c.textDim, modifier = Modifier.size(52.dp))
                                Text("No trips found", style = MaterialTheme.typography.bodyMedium, color = c.textMuted)
                            }
                        }
                    } else {
                        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            filtered.forEachIndexed { idx, trip ->
                                StaggeredItem(index = idx) {
                                    AdminTripCard(trip = trip, onClick = { onTripDetail(trip.id) })
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
private fun AdminTripCard(trip: TripBookingDto, onClick: () -> Unit = {}) {
    val c = LocalAppColors.current
    KntCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(trip.commuterName ?: "Commuter", style = MaterialTheme.typography.titleSmall, color = c.textBright)
                Text("${trip.pickupAddress} → ${trip.dropAddress}",
                    style = MaterialTheme.typography.bodySmall, color = c.textMuted, maxLines = 1)
                Text("${trip.date} · ${trip.time}", style = MaterialTheme.typography.labelSmall, color = c.textDim)
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                TripStatusDtoChip(trip.status)
                if (trip.quotedAmount != null) {
                    Text("R${String.format("%.2f", trip.quotedAmount)}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = c.yellow)
                }
            }
        }
        if (trip.driverName != null) {
            KntDivider()
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Person, null, tint = c.blue, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(trip.driverName, style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                if (trip.vehiclePlate != null) {
                    Spacer(Modifier.width(12.dp))
                    Icon(Icons.Rounded.DirectionsBus, null, tint = c.blue, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(trip.vehiclePlate, style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                }
            }
        }
    }
}
