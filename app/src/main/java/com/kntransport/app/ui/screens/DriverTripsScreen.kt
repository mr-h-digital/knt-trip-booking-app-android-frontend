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
import com.kntransport.app.data.*
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*
import java.time.LocalDate

@Composable
fun DriverTripsScreen(
    onBack      : () -> Unit,
    onTripDetail: (String) -> Unit,
) {
    val c    = LocalAppColors.current
    val tabs = listOf("Today", "Upcoming", "Completed")
    var selectedTab by remember { mutableIntStateOf(0) }

    val filtered = when (selectedTab) {
        0 -> SampleData.driverTrips.filter {
            it.date == LocalDate.now() &&
            it.status !in listOf(TripStatus.COMPLETED, TripStatus.CANCELLED)
        }
        1 -> SampleData.driverTrips.filter {
            it.date.isAfter(LocalDate.now()) &&
            it.status !in listOf(TripStatus.COMPLETED, TripStatus.CANCELLED)
        }
        else -> SampleData.driverTrips.filter { it.status == TripStatus.COMPLETED }
    }

    KntScaffold(title = "My Trips", onBack = onBack) { pv ->
        Column(Modifier.fillMaxSize().padding(pv)) {

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

            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(Icons.Rounded.DirectionsBus, null, tint = c.textDim, modifier = Modifier.size(52.dp))
                        Text(
                            when (selectedTab) {
                                0    -> "No trips scheduled for today"
                                1    -> "No upcoming trips"
                                else -> "No completed trips yet"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = c.textMuted,
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    filtered.forEachIndexed { idx, trip ->
                        StaggeredItem(index = idx) {
                            DriverTripCard(trip = trip, onClick = { onTripDetail(trip.id) })
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}
