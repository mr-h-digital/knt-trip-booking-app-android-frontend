package com.kntransport.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kntransport.app.R
import com.kntransport.app.data.*
import com.kntransport.app.network.ApiResult
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*
import com.kntransport.app.viewmodel.UserViewModel
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    onBookTrip      : () -> Unit,
    onMyTrips       : () -> Unit,
    onLiftClubs     : () -> Unit,
    onLiftClubDetail: (String) -> Unit,
    onNotifications : () -> Unit,
    onProfile       : () -> Unit,
    onTripDetail    : (String) -> Unit,
    userViewModel   : UserViewModel = viewModel(),
    tripViewModel   : com.kntransport.app.viewmodel.TripViewModel = viewModel(),
    liftClubViewModel: com.kntransport.app.viewmodel.LiftClubViewModel = viewModel(),
    notifViewModel  : com.kntransport.app.viewmodel.NotificationViewModel = viewModel(),
) {
    val c            = LocalAppColors.current
    val profileState by userViewModel.profile.collectAsState()
    val tripsState   by tripViewModel.trips.collectAsState()
    val clubsState   by liftClubViewModel.clubs.collectAsState()
    val notifState   by notifViewModel.notifications.collectAsState()

    LaunchedEffect(Unit) {
        userViewModel.loadProfile()
        tripViewModel.loadTrips()
        liftClubViewModel.loadLiftClubs()
        notifViewModel.loadNotifications()
    }

    val displayName  = (profileState as? ApiResult.Success)?.data?.name ?: ""
    val displayRole  = (profileState as? ApiResult.Success)?.data?.role ?: ""
    val recentTrips  = (tripsState   as? ApiResult.Success)?.data?.take(2) ?: emptyList()
    val recentClubs  = (clubsState   as? ApiResult.Success)?.data?.take(2) ?: emptyList()
    val unreadCount  = (notifState   as? ApiResult.Success)?.data?.count { !it.read } ?: 0

    val tripsLoading = tripsState  is ApiResult.Loading
    val clubsLoading = clubsState  is ApiResult.Loading

    var selectedTab by remember { mutableStateOf(KntNavTab.HOME) }
    val scrollState = rememberScrollState()

    // Parallax: header collapses from full → compact as user scrolls
    val scrollPx = scrollState.value.toFloat()
    val headerHeightDp = 80.dp
    val collapseThresholdPx = 200f
    val collapseProgress = (scrollPx / collapseThresholdPx).coerceIn(0f, 1f)

    // Pulsing notification badge animation
    val pulseAnim = rememberInfiniteTransition(label = "badge")
    val pulseScale by pulseAnim.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.25f,
        animationSpec = infiniteRepeatable(
            animation  = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    val isDark = LocalIsDarkTheme.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(c.bgGradientTop, c.bgGradientMid, c.bgGradientBottom),
                    start  = Offset(0f, 0f),
                    end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                )
            )
    ) {

    // Decorative orbs in light mode — subtle blue & yellow ambient glows
    if (!isDark) {
        Box(
            Modifier.size(320.dp).offset((-60).dp, 80.dp)
                .background(Brush.radialGradient(listOf(KntBlue.copy(0.10f), Color.Transparent)))
        )
        Box(
            Modifier.fillMaxWidth().height(260.dp).align(Alignment.BottomCenter)
                .background(Brush.radialGradient(
                    listOf(KntYellow.copy(0.09f), Color.Transparent),
                    center = Offset(Float.POSITIVE_INFINITY / 2, Float.POSITIVE_INFINITY),
                    radius = 500f,
                ))
        )
    }

    val avatarOverlap = 40.dp

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                // ── Hero photo layer ──────────────────────────────────────
                val heroHeight = lerp(240.dp, 72.dp, collapseProgress)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(heroHeight)
                        .clip(RoundedCornerShape(bottomStart = lerp(28.dp, 0.dp, collapseProgress), bottomEnd = lerp(28.dp, 0.dp, collapseProgress)))
                ) {
                    HeroBgImage(
                        resId       = R.drawable.hero_bg_6,
                        modifier    = Modifier.fillMaxSize(),
                        darkOverlay = lerpF(0.38f, 0.72f, collapseProgress),
                    )
                    // Blue radial glow top-left
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.radialGradient(
                                listOf(c.blue.copy(0.22f), Color.Transparent),
                                center = Offset(0f, 0f), radius = 500f,
                            )
                        )
                    )
                    // Bottom scrim so text is always readable
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(0.55f)),
                                startY = 80f,
                            )
                        )
                    )
                    // Greeting — centred, fades out on collapse
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = avatarOverlap + 16.dp)
                            .graphicsLayer(alpha = 1f - collapseProgress * 2f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            "Good morning,",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color        = KntMuted,
                                letterSpacing = 0.5.sp,
                            ),
                        )
                        GradientText(
                            text   = displayName.split(" ").first(),
                            style  = MaterialTheme.typography.headlineMedium,
                            colors = listOf(KntWhite, KntYellow),
                        )
                        if (displayRole.isNotBlank()) {
                            Text(
                                displayRole.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall.copy(color = KntMuted),
                            )
                        }
                    }
                }

                // ── Top bar icons (always visible) ────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val logoSize = lerp(40.dp, 32.dp, collapseProgress)
                    KntLogoBadge(size = logoSize)
                    Spacer(Modifier.width(8.dp))
                    // Compact title on scroll
                    AnimatedVisibility(
                        visible = collapseProgress >= 0.6f,
                        enter   = fadeIn(tween(150)),
                        exit    = fadeOut(tween(150)),
                    ) {
                        Text(
                            "K&T Transport",
                            style    = MaterialTheme.typography.titleLarge,
                            color    = c.headerText,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (collapseProgress < 0.6f) Spacer(Modifier.weight(1f))
                    // Notification bell
                    Box {
                        IconButton(onClick = onNotifications) {
                            Icon(Icons.Rounded.Notifications, "Notifications",
                                tint = KntWhite, modifier = Modifier.size(24.dp))
                        }
                        if (unreadCount > 0) {
                            Surface(
                                shape    = CircleShape,
                                color    = c.orange,
                                modifier = Modifier
                                    .size(16.dp)
                                    .align(Alignment.TopEnd)
                                    .offset((-2).dp, 2.dp)
                                    .scale(pulseScale),
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
                }

                // ── Avatar — centred bottom overlap ───────────────────────
                val avatarSize = lerp(72.dp, 0.dp, collapseProgress)
                if (avatarSize > 8.dp) {
                    UserAvatar(
                        name      = displayName,
                        avatarUri = null,
                        size      = avatarSize,
                        onClick   = onProfile,
                        modifier  = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = avatarSize / 2),
                    )
                }

                // Yellow accent stripe (visible when collapsed)
                if (collapseProgress > 0.4f) {
                    Row(
                        Modifier.align(Alignment.BottomEnd)
                            .graphicsLayer(alpha = (collapseProgress - 0.4f) * 2f)
                    ) {
                        Box(Modifier.width(40.dp).height(2.dp).background(c.yellow))
                        Box(Modifier.width(4.dp).height(2.dp).background(c.orange))
                    }
                }
            }
        },
        bottomBar = {
            KntBottomNav(
                selected = selectedTab,
                onSelect = { tab ->
                    selectedTab = tab
                    when (tab) {
                        KntNavTab.HOME       -> {}
                        KntNavTab.TRIPS      -> onMyTrips()
                        KntNavTab.LIFT_CLUBS -> onLiftClubs()
                        KntNavTab.PROFILE    -> onProfile()
                    }
                },
            )
        },
    ) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv)
                .verticalScroll(scrollState)
        ) {
            // Space for the centred avatar that overlaps the fold
            Spacer(Modifier.height(avatarOverlap + 8.dp))

            // ── Quick action cards ────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                QuickActionCard(
                    icon    = Icons.Rounded.DirectionsBus,
                    label   = "Book a Trip",
                    sublabel= "One-way, one-time",
                    tint    = c.blue,
                    onClick = onBookTrip,
                    modifier = Modifier.weight(1f),
                )
                QuickActionCard(
                    icon    = Icons.Rounded.Groups,
                    label   = "Lift Clubs",
                    sublabel= "Subscribe or create",
                    tint    = c.yellow,
                    onClick = onLiftClubs,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Personal stats strip ──────────────────────────────────────
            val allTrips       = (tripsState as? ApiResult.Success)?.data ?: emptyList()
            val tripsTaken     = allTrips.size
            val tripsCompleted = allTrips.count { it.status == "COMPLETED" }
            val amountSpent    = allTrips
                .filter { it.status == "COMPLETED" }
                .mapNotNull { it.quotedAmount }
                .sum()

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatChip(tripsTaken.toString(),     "Trips Booked", c.blue,   Modifier.weight(1f))
                StatChip(tripsCompleted.toString(), "Completed",    c.yellow, Modifier.weight(1f))
                StatChip(
                    value    = "R${String.format("%.0f", amountSpent)}",
                    label    = "Spent",
                    color    = c.orange,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Recent trips ──────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.width(3.dp).height(18.dp).clip(RoundedCornerShape(2.dp)).background(c.yellow))
                Spacer(Modifier.width(8.dp))
                Text("Recent Trips", style = MaterialTheme.typography.titleMedium, color = c.textBright, modifier = Modifier.weight(1f))
                TextButton(onClick = onMyTrips) {
                    Text("See All", style = MaterialTheme.typography.labelMedium, color = c.blue)
                }
            }
            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (tripsLoading) {
                    repeat(2) { TripCardShimmer() }
                } else if (recentTrips.isEmpty()) {
                    TripEmptyState(onBookTrip)
                } else {
                    recentTrips.forEachIndexed { idx, trip ->
                        StaggeredItem(index = idx) {
                            TripDtoCard(trip = trip, onClick = { onTripDetail(trip.id) })
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Lift clubs snapshot ───────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.width(3.dp).height(18.dp).clip(RoundedCornerShape(2.dp)).background(c.yellow))
                Spacer(Modifier.width(8.dp))
                Text("Available Lift Clubs", style = MaterialTheme.typography.titleMedium, color = c.textBright, modifier = Modifier.weight(1f))
                TextButton(onClick = onLiftClubs) {
                    Text("Browse All", style = MaterialTheme.typography.labelMedium, color = c.blue)
                }
            }
            Column(modifier = Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (clubsLoading) {
                    repeat(2) { TripCardShimmer() }
                } else if (recentClubs.isEmpty()) {
                    LiftClubEmptyState(onLiftClubs)
                } else {
                    recentClubs.forEachIndexed { idx, club ->
                        StaggeredItem(index = idx + 2) {
                            LiftClubDtoCard(club = club, isSubscribed = false, onClick = { onLiftClubDetail(club.id) })
                        }
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
    } // close outer gradient Box
}

private fun lerp(a: Dp, b: Dp, t: Float): Dp = a + (b - a) * t
private fun lerpF(a: Float, b: Float, t: Float): Float = a + (b - a) * t

// ── Empty states with Canvas bus/group illustrations ─────────────────────────

@Composable
fun TripEmptyState(onBook: () -> Unit) {
    val c = LocalAppColors.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Canvas(modifier = Modifier.size(80.dp)) {
            // Bus body
            drawRoundRect(
                color       = KntBlue.copy(alpha = 0.18f),
                size        = androidx.compose.ui.geometry.Size(size.width * 0.88f, size.height * 0.55f),
                topLeft     = Offset(size.width * 0.06f, size.height * 0.28f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f),
            )
            drawRoundRect(
                color       = KntBlue.copy(alpha = 0.55f),
                size        = androidx.compose.ui.geometry.Size(size.width * 0.88f, size.height * 0.55f),
                topLeft     = Offset(size.width * 0.06f, size.height * 0.28f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f),
                style       = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5f),
            )
            // Windows
            listOf(0.15f, 0.38f, 0.61f).forEach { x ->
                drawRoundRect(
                    color    = KntBlueBright.copy(alpha = 0.45f),
                    size     = androidx.compose.ui.geometry.Size(size.width * 0.16f, size.height * 0.18f),
                    topLeft  = Offset(size.width * x, size.height * 0.34f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f),
                )
            }
            // Wheels
            listOf(0.22f, 0.72f).forEach { x ->
                drawCircle(color = KntDark, radius = size.width * 0.08f, center = Offset(size.width * x, size.height * 0.84f))
                drawCircle(color = KntMuted.copy(alpha = 0.5f), radius = size.width * 0.04f, center = Offset(size.width * x, size.height * 0.84f))
            }
            // Yellow stripe
            drawLine(
                color       = KntYellow.copy(alpha = 0.8f),
                start       = Offset(size.width * 0.06f, size.height * 0.50f),
                end         = Offset(size.width * 0.94f, size.height * 0.50f),
                strokeWidth = 3f,
            )
        }
        Text("No trips yet", style = MaterialTheme.typography.titleSmall, color = c.textBright)
        Text("Book your first trip with K&T Transport", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
        Spacer(Modifier.height(4.dp))
        KntPrimaryButton(
            text     = "Book a Trip",
            onClick  = onBook,
            icon     = Icons.Rounded.DirectionsBus,
            modifier = Modifier.fillMaxWidth(0.7f),
        )
    }
}

@Composable
fun LiftClubEmptyState(onBrowse: () -> Unit) {
    val c = LocalAppColors.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Canvas(modifier = Modifier.size(80.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            // Three person silhouettes
            listOf(-0.28f, 0f, 0.28f).forEachIndexed { i, dx ->
                val alpha = if (i == 1) 0.9f else 0.55f
                val tint  = if (i == 1) KntYellow else KntBlue
                drawCircle(tint.copy(alpha = alpha), radius = size.width * 0.10f,
                    center = Offset(cx + dx * size.width, cy - size.height * 0.14f))
                drawRoundRect(
                    color        = tint.copy(alpha = alpha),
                    size         = androidx.compose.ui.geometry.Size(size.width * 0.18f, size.height * 0.24f),
                    topLeft      = Offset(cx + dx * size.width - size.width * 0.09f, cy + size.height * 0.02f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f),
                )
            }
            // Connecting arc
            drawArc(
                color      = KntYellow.copy(alpha = 0.3f),
                startAngle = 200f,
                sweepAngle = 140f,
                useCenter  = false,
                topLeft    = Offset(cx - size.width * 0.35f, cy - size.height * 0.35f),
                size       = androidx.compose.ui.geometry.Size(size.width * 0.70f, size.height * 0.70f),
                style      = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f),
            )
        }
        Text("No lift clubs", style = MaterialTheme.typography.titleSmall, color = c.textBright)
        Text("Browse or create a lift club today", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
        Spacer(Modifier.height(4.dp))
        KntSecondaryButton(
            text     = "Browse Lift Clubs",
            onClick  = onBrowse,
            icon     = Icons.Rounded.Groups,
            modifier = Modifier.fillMaxWidth(0.7f),
        )
    }
}

// ── Quick action card ─────────────────────────────────────────────────────────

@Composable
private fun QuickActionCard(
    icon    : androidx.compose.ui.graphics.vector.ImageVector,
    label   : String,
    sublabel: String,
    tint    : Color,
    onClick : () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c     = LocalAppColors.current
    val isDark = LocalIsDarkTheme.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label         = "qaScale",
    )

    // Light mode: rich colour-tinted gradient card; dark mode: subtle glass
    val cardBg = if (!isDark)
        Brush.linearGradient(
            listOf(Color.White, tint.copy(0.08f), Color.White.copy(0.92f)),
            start = Offset(0f, 0f),
            end   = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
        )
    else
        Brush.linearGradient(listOf(c.surface1.copy(0.95f), c.surface1.copy(0.75f)))

    Column(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(
                BorderStroke(
                    if (!isDark) 1.5.dp else 1.dp,
                    Brush.linearGradient(listOf(tint.copy(if (!isDark) 0.45f else 0.30f), Color.White.copy(0.08f))),
                ),
                RoundedCornerShape(18.dp),
            )
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (!isDark) tint.copy(alpha = 0.14f)
                    else         tint.copy(alpha = 0.15f)
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(label,    style = MaterialTheme.typography.titleMedium, color = c.textBright)
        Text(sublabel, style = MaterialTheme.typography.bodySmall,   color = c.textMuted)
    }
}

@Composable
private fun StatChip(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    // Animated count-up — supports leading prefix (e.g. "R250") and trailing suffix (e.g. "3+")
    val prefix      = value.takeWhile { !it.isDigit() }
    val remainder   = value.dropWhile { !it.isDigit() }
    val numericPart = remainder.takeWhile { it.isDigit() }
    val suffix      = remainder.dropWhile { it.isDigit() }
    val target      = numericPart.toIntOrNull() ?: 0
    val animatedVal by animateIntAsState(
        targetValue   = target,
        animationSpec = tween(durationMillis = 1200, easing = EaseOutCubic),
        label         = "statCounter",
    )
    val displayVal = if (numericPart.isNotEmpty()) "$prefix$animatedVal$suffix" else value

    val isDark = LocalIsDarkTheme.current
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (!isDark)
                    Brush.linearGradient(listOf(Color.White, color.copy(0.06f)))
                else
                    Brush.linearGradient(listOf(c.surface1.copy(0.95f), c.surface1.copy(0.75f)))
            )
            .border(
                1.dp,
                Brush.linearGradient(listOf(color.copy(if (!isDark) 0.35f else 0.25f), Color.White.copy(0.06f))),
                RoundedCornerShape(12.dp),
            )
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        GradientText(
            text   = displayVal,
            style  = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
            colors = listOf(color, color.copy(0.7f)),
        )
        Text(label, style = MaterialTheme.typography.bodySmall, color = c.textMuted, maxLines = 1)
    }
}

@Composable
fun TripSummaryCard(trip: TripBooking, onClick: () -> Unit) {
    val c   = LocalAppColors.current
    val fmt = DateTimeFormatter.ofPattern("EEE d MMM")
    KntCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                    .background(c.blue.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.DirectionsBus, null, tint = c.blue, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(trip.dropAddress, style = MaterialTheme.typography.titleSmall, color = c.textBright, maxLines = 1)
                Text("${trip.date.format(fmt)} · ${trip.time}", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
            }
            TripStatusChip(trip.status)
        }
        if (trip.quotedAmount != null) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Payments, null, tint = c.yellow, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("R${String.format("%.2f", trip.quotedAmount)}", style = MaterialTheme.typography.labelMedium, color = c.yellow)
            }
        }
    }
}

@Composable
fun LiftClubSummaryCard(club: LiftClub, onClick: () -> Unit) {
    val c = LocalAppColors.current
    KntCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                    .background(c.yellow.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Groups, null, tint = c.yellow, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(club.title, style = MaterialTheme.typography.titleSmall, color = c.textBright, maxLines = 1)
                Text("${club.pickupArea} → ${club.dropArea}", style = MaterialTheme.typography.bodySmall, color = c.textMuted, maxLines = 1)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Schedule, null, tint = c.textDim, modifier = Modifier.size(12.dp))
                    Text(club.departureTime.toString(), style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                    Icon(Icons.Rounded.Person, null, tint = c.textDim, modifier = Modifier.size(12.dp))
                    Text("${club.subscriberCount}/${club.maxPassengers}", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                }
            }
            LiftClubStatusChip(club.status)
        }
    }
}
