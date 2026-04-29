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
import com.kntransport.app.network.LiftClubDto
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*
import com.kntransport.app.viewmodel.LiftClubViewModel

@Composable
fun LiftClubsScreen(
    onBack      : () -> Unit,
    onClubDetail: (String) -> Unit,
    onCreateClub: () -> Unit,
    viewModel   : LiftClubViewModel = viewModel(),
) {
    val c                 = LocalAppColors.current
    val clubsState        by viewModel.clubs.collectAsState()
    val subscriptionsState by viewModel.mySubscriptions.collectAsState()
    val myClubsState      by viewModel.myClubs.collectAsState()
    val tabs              = listOf("Browse", "My Subscriptions", "My Requests")
    var selectedTab       by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) { viewModel.loadLiftClubs() }
    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            1 -> viewModel.loadMySubscriptions()
            2 -> viewModel.loadMyClubs()
        }
    }

    val allClubs   = (clubsState        as? ApiResult.Success)?.data ?: emptyList()
    val subscribed = (subscriptionsState as? ApiResult.Success)?.data ?: emptyList()
    val myRequests = (myClubsState      as? ApiResult.Success)?.data ?: emptyList()

    KntScaffold(
        title  = "Lift Clubs",
        onBack = onBack,
        actions = {
            IconButton(onClick = onCreateClub) {
                Icon(Icons.Rounded.Add, "Create Lift Club", tint = LocalAppColors.current.yellow)
            }
        },
    ) { pv ->
        Column(Modifier.fillMaxSize().padding(pv)) {
            // Hero banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            ) {
                HeroBgImage(resId = R.drawable.hero_bg_8, modifier = Modifier.fillMaxSize(), darkOverlay = 0.48f)
                Column(Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 12.dp)) {
                    Text("Shared transport, real savings", style = MaterialTheme.typography.labelMedium.copy(color = KntYellow, letterSpacing = 0.5.sp))
                }
            }

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor   = c.surface1,
                contentColor     = c.blue,
                indicator        = { tps ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tps[selectedTab]),
                        color = c.yellow,
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

            val activeState = when (selectedTab) {
                1    -> subscriptionsState
                2    -> myClubsState
                else -> clubsState
            }
            val list = when (selectedTab) {
                1    -> subscribed
                2    -> myRequests
                else -> allClubs
            }
            val onRetry: () -> Unit = when (selectedTab) {
                1    -> {{ viewModel.loadMySubscriptions() }}
                2    -> {{ viewModel.loadMyClubs() }}
                else -> {{ viewModel.loadLiftClubs() }}
            }

            if (activeState is ApiResult.Loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (activeState is ApiResult.Error) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ErrorState(message = (activeState as ApiResult.Error).message, onRetry = onRetry)
                }
            } else if (list.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    when (selectedTab) {
                        1 -> LiftClubEmptyState(
                            onBrowse     = { selectedTab = 0 },
                            onCreateClub = onCreateClub,
                            title        = "No subscriptions yet",
                            subtitle     = "You haven't joined any lift clubs. Browse available clubs and subscribe to one that suits your route.",
                            browseLabel  = "Browse Lift Clubs",
                            createLabel  = "Create a Lift Club",
                        )
                        2 -> LiftClubEmptyState(
                            onBrowse     = onCreateClub,
                            title        = "No lift club requests yet",
                            subtitle     = "Create your first lift club request and K&T will match passengers on your route.",
                            browseLabel  = "Create a Lift Club",
                        )
                        else -> LiftClubEmptyState(
                            onBrowse     = { selectedTab = 0 },
                            onCreateClub = onCreateClub,
                            title        = "No lift clubs available",
                            subtitle     = "There are no active lift clubs yet. Be the first to create one for your route!",
                            browseLabel  = "Refresh",
                            createLabel  = "Create a Lift Club",
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (selectedTab == 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = c.yellow.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, c.yellow.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Lightbulb, null, tint = c.yellow, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Subscribe to a lift club or create your own. Once the quota is met, K&T will confirm and send a quote.",
                                    style = MaterialTheme.typography.bodySmall, color = c.textMuted,
                                )
                            }
                        }
                    }

                    list.forEachIndexed { idx, club ->
                        StaggeredItem(index = idx) {
                            LiftClubDtoCard(
                                club         = club,
                                isSubscribed = false,
                                onClick      = { onClubDetail(club.id) },
                            )
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun LiftClubDtoCard(club: LiftClubDto, isSubscribed: Boolean, onClick: () -> Unit) {
    val c = LocalAppColors.current
    KntCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.Top) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(c.yellow.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Groups, null, tint = c.yellow, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(club.title, style = MaterialTheme.typography.titleSmall, color = c.textBright,
                        modifier = Modifier.weight(1f), maxLines = 1)
                    LiftClubStatusDtoChip(club.status)
                }
                Spacer(Modifier.height(4.dp))
                Text(club.description, style = MaterialTheme.typography.bodySmall, color = c.textMuted, maxLines = 2)
            }
        }
        KntDivider()
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            LiftClubMeta(Icons.Rounded.Schedule, club.departureTime, c.blue)
            LiftClubMeta(Icons.Rounded.People, "${club.subscriberCount}/${club.maxPassengers}", c.textMuted)
            LiftClubMeta(Icons.Rounded.CalendarViewWeek, club.daysOfWeek.joinToString(", "), c.textMuted)
        }
        if (club.quotedAmount != null) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Payments, null, tint = c.yellow, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("R${String.format("%.2f", club.quotedAmount)} ${club.paymentCycle?.lowercase() ?: ""}",
                    style = MaterialTheme.typography.labelMedium, color = c.yellow)
            }
        }
        if (isSubscribed) {
            Spacer(Modifier.height(8.dp))
            Surface(shape = RoundedCornerShape(8.dp), color = StatusGreen.copy(alpha = 0.12f)) {
                Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.CheckCircle, null, tint = StatusGreen, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("You're subscribed", style = MaterialTheme.typography.labelSmall, color = StatusGreen)
                }
            }
        }
    }
}

@Composable
fun LiftClubStatusDtoChip(status: String) {
    val (label, bg, fg) = when (status.uppercase()) {
        "OPEN"       -> Triple("Open",       androidx.compose.ui.graphics.Color(0xFF0D3A1A), StatusGreen)
        "QUOTA_MET"  -> Triple("Quota Met",  androidx.compose.ui.graphics.Color(0xFF3A2A00), KntYellow)
        "QUOTE_SENT" -> Triple("Quote Sent", androidx.compose.ui.graphics.Color(0xFF3A2800), KntOrange)
        "ACTIVE"     -> Triple("Active",     androidx.compose.ui.graphics.Color(0xFF0D2040), KntBlueBright)
        "COMPLETED"  -> Triple("Completed",  androidx.compose.ui.graphics.Color(0xFF1A2A1A), androidx.compose.ui.graphics.Color(0xFF7BC47B))
        else         -> Triple("Cancelled",  androidx.compose.ui.graphics.Color(0xFF3A1A1A), StatusRed)
    }
    Surface(shape = RoundedCornerShape(20.dp), color = bg) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}

@Composable
fun LiftClubCard(club: LiftClub, isSubscribed: Boolean, onClick: () -> Unit) {
    val c = LocalAppColors.current
    KntCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                    .background(c.yellow.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Groups, null, tint = c.yellow, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(club.title, style = MaterialTheme.typography.titleSmall, color = c.textBright,
                        modifier = Modifier.weight(1f), maxLines = 1)
                    LiftClubStatusChip(club.status)
                }
                Spacer(Modifier.height(4.dp))
                Text(club.description, style = MaterialTheme.typography.bodySmall, color = c.textMuted, maxLines = 2)
            }
        }

        KntDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            LiftClubMeta(Icons.Rounded.Schedule, club.departureTime.toString(), c.blue)
            LiftClubMeta(Icons.Rounded.People, "${club.subscriberCount}/${club.maxPassengers}", c.textMuted)
            LiftClubMeta(Icons.Rounded.CalendarViewWeek, club.daysOfWeek.joinToString(", "), c.textMuted)
        }

        if (club.quotedAmount != null) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Payments, null, tint = c.yellow, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    "R${String.format("%.2f", club.quotedAmount)} ${club.paymentCycle?.name?.lowercase() ?: ""}",
                    style = MaterialTheme.typography.labelMedium, color = c.yellow,
                )
            }
        }

        if (isSubscribed) {
            Spacer(Modifier.height(8.dp))
            Surface(shape = RoundedCornerShape(8.dp), color = StatusGreen.copy(alpha = 0.12f)) {
                Row(
                    Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.CheckCircle, null, tint = StatusGreen, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("You're subscribed", style = MaterialTheme.typography.labelSmall, color = StatusGreen)
                }
            }
        }
    }
}

@Composable
private fun LiftClubMeta(
    icon : androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    tint : androidx.compose.ui.graphics.Color,
) {
    val c = LocalAppColors.current
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(13.dp))
        Text(value, style = MaterialTheme.typography.bodySmall, color = c.textMuted, maxLines = 1)
    }
}
