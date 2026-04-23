package com.kntransport.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.kntransport.app.R
import com.kntransport.app.data.*
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.components.DriverNavTab
import com.kntransport.app.ui.components.NavTabItem
import com.kntransport.app.ui.theme.*
import java.time.format.DateTimeFormatter

@Composable
fun DriverDashboardScreen(
    onBack        : () -> Unit,
    onMyTrips     : () -> Unit,
    onEarnings    : () -> Unit,
    onProfile     : () -> Unit,
    onNotifications: () -> Unit,
    onTripDetail  : (String) -> Unit,
) {
    val c            = LocalAppColors.current
    val driver       = SampleData.currentUser
    val allTrips     = SampleData.driverTrips
    val todayTrips   = allTrips.filter { it.date == java.time.LocalDate.now() }
    val activeTrip   = todayTrips.firstOrNull { it.status == TripStatus.IN_PROGRESS }
    val nextTrip     = todayTrips.firstOrNull { it.status == TripStatus.CONFIRMED || it.status == TripStatus.QUOTE_ACCEPTED }
    val unreadCount  = SampleData.notifications.count { !it.read }
    var selectedTab  by remember { mutableIntStateOf(0) }

    val driverTabs = DriverNavTab.entries.map { NavTabItem(it.label, it.icon) }

    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseAnim.animateFloat(
        initialValue  = 1f, targetValue = 1.25f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse",
    )

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.linearGradient(
                listOf(c.bgGradientTop, c.bgGradientMid, c.bgGradientBottom),
                start = Offset(0f, 0f), end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
            )
        )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                RoleBottomNav(
                    tabs     = driverTabs,
                    selected = selectedTab,
                    onSelect = { idx ->
                        selectedTab = idx
                        when (idx) {
                            1 -> onMyTrips()
                            2 -> onEarnings()
                            3 -> onProfile()
                            else -> {}
                        }
                    },
                )
            },
            topBar = {
                Box(Modifier.fillMaxWidth().wrapContentHeight()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                    ) {
                        HeroBgImage(resId = R.drawable.hero_bg_2, modifier = Modifier.fillMaxSize(), darkOverlay = 0.55f)
                        Box(Modifier.fillMaxSize().background(
                            Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.5f)))
                        ))
                        Column(
                            Modifier.align(Alignment.BottomStart)
                                .padding(start = 16.dp, bottom = 16.dp)
                        ) {
                            Text(greeting(), style = MaterialTheme.typography.bodySmall, color = KntMuted)
                            GradientText(
                                text   = driver.name,
                                style  = MaterialTheme.typography.headlineSmall,
                                colors = listOf(KntWhite, KntYellow),
                            )
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = KntYellow.copy(0.15f),
                            ) {
                                Row(
                                    Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(Icons.Rounded.LocalShipping, null, tint = KntYellow, modifier = Modifier.size(12.dp))
                                    Text("Driver", style = MaterialTheme.typography.labelSmall, color = KntYellow)
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().statusBarsPadding()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        KntLogoBadge(size = 36.dp)
                        Spacer(Modifier.weight(1f))
                        Box {
                            IconButton(onClick = onNotifications) {
                                Icon(Icons.Rounded.Notifications, null, tint = KntWhite, modifier = Modifier.size(24.dp))
                            }
                            if (unreadCount > 0) {
                                Surface(
                                    shape    = CircleShape,
                                    color    = c.orange,
                                    modifier = Modifier.size(16.dp).align(Alignment.TopEnd)
                                        .offset((-2).dp, 2.dp).scale(pulseScale),
                                ) {
                                    Text(
                                        unreadCount.toString(),
                                        style    = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                        color    = Color.White,
                                        modifier = Modifier.wrapContentSize(Alignment.Center),
                                    )
                                }
                            }
                        }
                        IconButton(onClick = onProfile) {
                            UserAvatar(name = driver.name, avatarUri = driver.avatarUri, size = 32.dp)
                        }
                    }
                }
            },
        ) { pv ->
            Column(
                modifier = Modifier.fillMaxSize().padding(pv)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            ) {
                Spacer(Modifier.height(16.dp))

                // ── Today's stats strip ───────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    DriverStatChip(
                        value = todayTrips.size.toString(),
                        label = "Today's Trips",
                        tint  = c.blue,
                        modifier = Modifier.weight(1f),
                    )
                    DriverStatChip(
                        value = "R${todayTrips.mapNotNull { it.quotedAmount }.sum().let { "%.0f".format(it) }}",
                        label = "Today's Earnings",
                        tint  = StatusGreen,
                        modifier = Modifier.weight(1f),
                    )
                    DriverStatChip(
                        value = allTrips.count { it.status == TripStatus.COMPLETED }.toString(),
                        label = "Total Done",
                        tint  = c.yellow,
                        modifier = Modifier.weight(1f),
                    )
                }

                // ── Active trip banner ────────────────────────────────────
                if (activeTrip != null) {
                    Spacer(Modifier.height(20.dp))
                    Surface(
                        shape    = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        onClick  = { onTripDetail(activeTrip.id) },
                    ) {
                        Box(
                            Modifier.background(
                                Brush.linearGradient(listOf(KntBlue.copy(0.9f), KntBlueBright.copy(0.7f)))
                            ).border(BorderStroke(1.dp, KntWhite.copy(0.2f)), RoundedCornerShape(16.dp))
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier.size(10.dp).clip(CircleShape).background(StatusGreen),
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("TRIP IN PROGRESS", style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 1.sp, fontWeight = FontWeight.Bold,
                                    ), color = KntWhite)
                                    Spacer(Modifier.weight(1f))
                                    Icon(Icons.Rounded.ChevronRight, null, tint = KntWhite.copy(0.7f), modifier = Modifier.size(18.dp))
                                }
                                Spacer(Modifier.height(10.dp))
                                Text(activeTrip.commuterName, style = MaterialTheme.typography.titleMedium, color = KntWhite)
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Rounded.LocationOn, null, tint = KntYellow, modifier = Modifier.size(14.dp))
                                    Text(activeTrip.dropAddress, style = MaterialTheme.typography.bodySmall, color = KntWhite.copy(0.85f))
                                }
                            }
                        }
                    }
                }

                // ── Next trip ─────────────────────────────────────────────
                if (nextTrip != null) {
                    Spacer(Modifier.height(20.dp))
                    SectionHeader(title = "Next Trip")
                    Spacer(Modifier.height(10.dp))
                    DriverTripCard(trip = nextTrip, onClick = { onTripDetail(nextTrip.id) })
                }

                // ── Quick actions ─────────────────────────────────────────
                Spacer(Modifier.height(24.dp))
                SectionHeader(title = "Quick Actions")
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    DriverActionCard(
                        icon     = Icons.Rounded.DirectionsBus,
                        label    = "My Trips",
                        sublabel = "View all assigned trips",
                        tint     = c.blue,
                        onClick  = onMyTrips,
                        modifier = Modifier.weight(1f),
                    )
                    DriverActionCard(
                        icon     = Icons.Rounded.Payments,
                        label    = "Earnings",
                        sublabel = "Track your income",
                        tint     = StatusGreen,
                        onClick  = onEarnings,
                        modifier = Modifier.weight(1f),
                    )
                }

                // ── Today's schedule ──────────────────────────────────────
                if (todayTrips.isNotEmpty()) {
                    Spacer(Modifier.height(24.dp))
                    SectionHeader(title = "Today's Schedule", action = "See All", onAction = onMyTrips)
                    Spacer(Modifier.height(10.dp))
                    val fmt = DateTimeFormatter.ofPattern("HH:mm")
                    todayTrips.forEachIndexed { idx, trip ->
                        StaggeredItem(index = idx) {
                            DriverTripCard(trip = trip, onClick = { onTripDetail(trip.id) })
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun DriverStatChip(value: String, label: String, tint: Color, modifier: Modifier = Modifier) {
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
                style  = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                colors = listOf(tint, tint.copy(0.7f)),
            )
            Text(label, style = MaterialTheme.typography.labelSmall, color = c.textMuted, maxLines = 1)
        }
    }
}

@Composable
fun DriverTripCard(trip: TripBooking, onClick: () -> Unit) {
    val c   = LocalAppColors.current
    val fmt = DateTimeFormatter.ofPattern("HH:mm")
    KntCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = c.blue.copy(0.12f),
                modifier = Modifier.size(44.dp),
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        trip.time.format(fmt),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = c.blue,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(trip.commuterName, style = MaterialTheme.typography.titleSmall, color = c.textBright)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Rounded.TripOrigin, null, tint = c.textDim, modifier = Modifier.size(11.dp))
                    Text(trip.pickupAddress, style = MaterialTheme.typography.bodySmall, color = c.textMuted, maxLines = 1)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Rounded.LocationOn, null, tint = c.blue, modifier = Modifier.size(11.dp))
                    Text(trip.dropAddress, style = MaterialTheme.typography.bodySmall, color = c.textMuted, maxLines = 1)
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                TripStatusChip(trip.status)
                if (trip.quotedAmount != null) {
                    Text(
                        "R${String.format("%.0f", trip.quotedAmount)}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = c.yellow,
                    )
                }
            }
        }
    }
}

@Composable
private fun DriverActionCard(
    icon    : androidx.compose.ui.graphics.vector.ImageVector,
    label   : String,
    sublabel: String,
    tint    : Color,
    onClick : () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = LocalAppColors.current
    Surface(
        onClick  = onClick,
        shape    = RoundedCornerShape(16.dp),
        color    = c.surface2,
        border   = BorderStroke(1.dp, tint.copy(0.25f)),
        modifier = modifier,
    ) {
        Column(Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                    .background(tint.copy(0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(label,    style = MaterialTheme.typography.titleSmall, color = c.textBright)
            Text(sublabel, style = MaterialTheme.typography.bodySmall,  color = c.textMuted)
        }
    }
}

private fun greeting(): String {
    val hour = java.time.LocalTime.now().hour
    return when {
        hour < 12 -> "Good morning,"
        hour < 17 -> "Good afternoon,"
        else      -> "Good evening,"
    }
}

private fun androidx.compose.ui.Modifier.scale(scale: Float) =
    this.then(Modifier.graphicsLayer(scaleX = scale, scaleY = scale))
