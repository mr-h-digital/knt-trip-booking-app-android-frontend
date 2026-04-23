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
import com.kntransport.app.R
import com.kntransport.app.data.*
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*
import java.time.format.DateTimeFormatter

@Composable
fun MyTripsScreen(
    onBack      : () -> Unit,
    onTripDetail: (String) -> Unit,
) {
    val c = LocalAppColors.current
    val tabs = listOf("All", "Active", "Completed")
    var selectedTab by remember { mutableIntStateOf(0) }

    val filtered = when (selectedTab) {
        1 -> SampleData.myTrips.filter { it.status in listOf(TripStatus.PENDING_QUOTE, TripStatus.QUOTE_SENT, TripStatus.CONFIRMED, TripStatus.IN_PROGRESS) }
        2 -> SampleData.myTrips.filter { it.status == TripStatus.COMPLETED }
        else -> SampleData.myTrips
    }

    KntScaffold(title = "My Trips", onBack = onBack) { pv ->
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

            if (filtered.isEmpty()) {
                TripEmptyState(onBook = {})
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    filtered.forEachIndexed { idx, trip ->
                        StaggeredItem(index = idx) {
                            TripDetailCard(trip = trip, onClick = { onTripDetail(trip.id) })
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
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
