package com.kntransport.app.ui.screens

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kntransport.app.R
import com.kntransport.app.network.AnalyticsDto
import com.kntransport.app.network.ApiResult
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.components.AdminNavTab
import com.kntransport.app.ui.components.NavTabItem
import com.kntransport.app.ui.theme.*
import com.kntransport.app.viewmodel.AdminViewModel
import com.kntransport.app.viewmodel.UserViewModel

@Composable
fun AdminDashboardScreen(
    onBack        : () -> Unit,
    onUsers       : () -> Unit,
    onTrips       : () -> Unit,
    onAnalytics   : () -> Unit,
    onFinancials  : () -> Unit,
    onFleet        : () -> Unit = {},
    onProfile      : () -> Unit = {},
    viewModel      : AdminViewModel = viewModel(),
    userViewModel  : UserViewModel = viewModel(),
) {
    val c              = LocalAppColors.current
    val profileState  by userViewModel.profile.collectAsState()
    var selectedTab    by remember { mutableIntStateOf(0) }
    val adminTabs      = AdminNavTab.entries.map { NavTabItem(it.label, it.icon) }
    val analyticsState by viewModel.analytics.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAnalytics()
        userViewModel.loadProfile()
    }

    val displayName = (profileState as? ApiResult.Success)?.data?.name
        ?: ""

    val analytics = (analyticsState as? ApiResult.Success<AnalyticsDto>)?.data

    KntScaffold(
        title     = "Admin Dashboard",
        bottomBar = {
            RoleBottomNav(
                tabs     = adminTabs,
                selected = selectedTab,
                onSelect = { idx ->
                    selectedTab = idx
                    when (idx) {
                        1 -> onUsers()
                        2 -> onFleet()
                        3 -> onAnalytics()
                        4 -> onProfile()
                        else -> {}
                    }
                },
            )
        },
    ) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv)
                .verticalScroll(rememberScrollState()),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            ) {
                HeroBgImage(resId = R.drawable.hero_bg, modifier = Modifier.fillMaxSize(), darkOverlay = 0.55f)
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.5f)))
                    )
                )
                Column(
                    Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 16.dp)
                ) {
                    Text(
                        "Welcome back,",
                        style = MaterialTheme.typography.bodySmall.copy(color = KntMuted),
                    )
                    GradientText(
                        text   = displayName,
                        style  = MaterialTheme.typography.headlineSmall,
                        colors = listOf(KntWhite, KntYellow),
                    )
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = KntOrange.copy(alpha = 0.20f),
                    ) {
                        Row(
                            Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Icon(Icons.Rounded.AdminPanelSettings, null, tint = KntOrange, modifier = Modifier.size(13.dp))
                            Text("Administrator", style = MaterialTheme.typography.labelSmall, color = KntOrange)
                        }
                    }
                }
            }

            Column(Modifier.padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(24.dp))
            SectionHeader(title = "Overview")
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AdminStatCard(analytics?.totalUsers?.toString() ?: "—",  "Total Users",    Icons.Rounded.People,        c.blue,      Modifier.weight(1f))
                AdminStatCard(analytics?.confirmedTrips?.toString() ?: "—", "Active Trips", Icons.Rounded.DirectionsBus, c.yellow,    Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AdminStatCard(analytics?.pendingQuoteTrips?.toString() ?: "—", "Pending Quotes", Icons.Rounded.RequestQuote, KntOrange,   Modifier.weight(1f))
                AdminStatCard(
                    value      = analytics?.let { "R${String.format("%.0f", it.totalRevenue)}" } ?: "—",
                    label      = "Total Revenue",
                    icon       = Icons.Rounded.Payments,
                    tint       = StatusGreen,
                    modifier   = Modifier.weight(1f),
                    isMonetary = true,
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AdminStatCard(
                    value    = analytics?.totalLiftClubs?.toString() ?: "—",
                    label    = "Lift Clubs",
                    icon     = Icons.Rounded.Groups,
                    tint     = KntBlueBright,
                    modifier = Modifier.weight(1f),
                )
                AdminStatCard(
                    value    = analytics?.totalDrivers?.toString() ?: "—",
                    label    = "Drivers",
                    icon     = Icons.Rounded.Person,
                    tint     = c.yellow,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(24.dp))
            SectionHeader(title = "Manage")
            Spacer(Modifier.height(12.dp))

            AdminNavCard(
                icon     = Icons.Rounded.People,
                title    = "Users",
                subtitle = "Manage commuters, drivers & admins",
                tint     = c.blue,
                onClick  = onUsers,
            )
            Spacer(Modifier.height(10.dp))
            AdminNavCard(
                icon     = Icons.Rounded.DirectionsBus,
                title    = "All Trips",
                subtitle = "View and manage all trip bookings",
                tint     = c.yellow,
                onClick  = onTrips,
            )
            Spacer(Modifier.height(10.dp))
            AdminNavCard(
                icon     = Icons.Rounded.BarChart,
                title    = "Analytics",
                subtitle = "Routes, ratings and trip trends",
                tint     = KntOrange,
                onClick  = onAnalytics,
            )
            Spacer(Modifier.height(10.dp))
            AdminNavCard(
                icon     = Icons.Rounded.AccountBalance,
                title    = "Financials",
                subtitle = "Revenue, transactions & export reports",
                tint     = StatusGreen,
                onClick  = onFinancials,
            )
            Spacer(Modifier.height(10.dp))
            AdminNavCard(
                icon     = Icons.Rounded.DirectionsBus,
                title    = "Fleet",
                subtitle = "Manage vehicles & driver assignments",
                tint     = KntBlueBright,
                onClick  = onFleet,
            )

            Spacer(Modifier.height(32.dp))
            } // close inner padding Column
        }
    }
}

@Composable
private fun AdminStatCard(
    value      : String,
    label      : String,
    icon       : ImageVector,
    tint       : Color,
    modifier   : Modifier = Modifier,
    isMonetary : Boolean = false,
) {
    val c       = LocalAppColors.current
    val numeric = value.filter { it.isDigit() }
    val target  = numeric.toIntOrNull() ?: 0
    val animated by animateIntAsState(
        targetValue   = target,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label         = "stat",
    )
    val display = if (!isMonetary && numeric.isNotEmpty()) animated.toString() else value

    Surface(
        shape    = RoundedCornerShape(14.dp),
        color    = c.surface2,
        border   = BorderStroke(1.dp, tint.copy(alpha = 0.25f)),
        modifier = modifier,
    ) {
        Column(Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(10.dp))
            GradientText(
                text   = display,
                style  = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                colors = listOf(tint, tint.copy(0.7f)),
            )
            Text(label, style = MaterialTheme.typography.bodySmall, color = c.textMuted)
        }
    }
}

@Composable
private fun AdminNavCard(
    icon    : ImageVector,
    title   : String,
    subtitle: String,
    tint    : Color,
    onClick : () -> Unit,
) {
    val c = LocalAppColors.current
    KntCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title,    style = MaterialTheme.typography.titleSmall, color = c.textBright)
                Text(subtitle, style = MaterialTheme.typography.bodySmall,  color = c.textMuted)
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = c.textDim, modifier = Modifier.size(20.dp))
        }
    }
}
