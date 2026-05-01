package com.kntransport.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.kntransport.app.R
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues

private data class OnboardingPage(
    val heroRes  : Int,
    val icon     : androidx.compose.ui.graphics.vector.ImageVector,
    val iconTint : Color,
    val title    : String,
    val subtitle : String,
)

private val PAGES = listOf(
    OnboardingPage(
        heroRes  = R.drawable.hero_bg,
        icon     = Icons.Rounded.DirectionsBus,
        iconTint = KntBlue,
        title    = "Book Your Ride",
        subtitle = "Request a one-way trip from your door. K&T Transport serves Beacon Valley and Mitchell's Plain.",
    ),
    OnboardingPage(
        heroRes  = R.drawable.hero_bg_8,
        icon     = Icons.Rounded.Groups,
        iconTint = KntYellow,
        title    = "Join a Lift Club",
        subtitle = "Share the journey with your community. Subscribe to a lift club and split the cost.",
    ),
    OnboardingPage(
        heroRes  = R.drawable.hero_bg_6,
        icon     = Icons.Rounded.CheckCircle,
        iconTint = StatusGreen,
        title    = "Get a Quote & Go",
        subtitle = "Receive a transparent quote, accept it, and your driver is confirmed. Moving communities forward.",
    ),
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pagerState = rememberPagerState { PAGES.size }
    val scope      = rememberCoroutineScope()
    val isLast     = pagerState.currentPage == PAGES.lastIndex
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    // Show swipe hint arrow on first page only, fades after 2 seconds
    var showSwipeHint by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(2200)
        showSwipeHint = false
    }
    // Also hide it as soon as the user swipes
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage > 0) showSwipeHint = false
    }

    // Controls height: dots row(8) + gap(8) + counter(18) + gap(24) + button(54) + gap(10) + skip(40)
    val controlsHeight    = 8.dp + 8.dp + 18.dp + 24.dp + 54.dp + 10.dp + 40.dp
    val controlsBottomPad = 48.dp + navBarPadding
    val contentClearance  = controlsHeight + controlsBottomPad + 16.dp

    Box(Modifier.fillMaxSize().background(KntBlack)) {

        // ── Pager (behind controls) ───────────────────────────────────────────
        HorizontalPager(
            state    = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            val p = PAGES[page]
            Box(Modifier.fillMaxSize()) {
                HeroBgImage(resId = p.heroRes, modifier = Modifier.fillMaxSize(), darkOverlay = 0.72f)

                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, KntBlack.copy(0.95f)),
                            startY = 400f,
                        )
                    )
                )

                // Page content — pinned above controls
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .padding(bottom = contentClearance),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        Modifier.size(64.dp).clip(RoundedCornerShape(18.dp))
                            .background(p.iconTint.copy(0.18f))
                            .border(1.5.dp, p.iconTint.copy(0.4f), RoundedCornerShape(18.dp)),
                        Alignment.Center,
                    ) {
                        Icon(p.icon, null, tint = p.iconTint, modifier = Modifier.size(32.dp))
                    }
                    Spacer(Modifier.height(20.dp))
                    GradientText(
                        text   = p.title,
                        style  = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                        colors = listOf(KntWhite, p.iconTint),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        p.subtitle,
                        style     = MaterialTheme.typography.bodyMedium.copy(color = KntMuted),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        // ── Swipe hint — animated chevrons, first page only ───────────────────
        AnimatedVisibility(
            visible = showSwipeHint,
            enter   = fadeIn(tween(400)),
            exit    = fadeOut(tween(600)),
            modifier = Modifier.align(Alignment.Center).offset(y = 60.dp),
        ) {
            SwipeHint()
        }

        // ── Bottom controls ───────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .padding(bottom = controlsBottomPad),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Page dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                repeat(PAGES.size) { idx ->
                    val isSelected = idx == pagerState.currentPage
                    val width by animateDpAsState(
                        targetValue   = if (isSelected) 24.dp else 8.dp,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label         = "dot",
                    )
                    Box(
                        Modifier.height(8.dp).width(width)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) KntYellow else KntMuted.copy(0.4f))
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Page counter e.g. "1 of 3"
            Text(
                "${pagerState.currentPage + 1} of ${PAGES.size}",
                style = MaterialTheme.typography.labelSmall.copy(color = KntMuted.copy(0.7f)),
            )

            Spacer(Modifier.height(24.dp))

            // Next / Get Started button
            Button(
                onClick = {
                    if (isLast) onFinished()
                    else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = if (isLast) StatusGreen else KntBlue,
                    contentColor   = Color.White,
                ),
            ) {
                Text(
                    if (isLast) "Get Started" else "Next",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    if (isLast) Icons.Rounded.RocketLaunch else Icons.Rounded.ArrowForward,
                    null, modifier = Modifier.size(18.dp),
                )
            }

            Spacer(Modifier.height(10.dp))

            // Skip — always visible so the user always has an out
            TextButton(
                onClick  = onFinished,
                modifier = Modifier.height(40.dp),
            ) {
                Text(
                    if (isLast) "Sign In instead" else "Skip",
                    style = MaterialTheme.typography.labelLarge,
                    color = KntMuted,
                )
            }
        }
    }
}

@Composable
private fun SwipeHint() {
    val infiniteTransition = rememberInfiniteTransition(label = "swipeHint")
    val offset by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 12f,
        animationSpec = infiniteRepeatable(
            animation  = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "swipeOffset",
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue  = 0.4f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "swipeAlpha",
    )

    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier              = Modifier.offset(x = offset.dp),
    ) {
        // Two trailing chevrons — classic swipe-right affordance
        Icon(
            Icons.Rounded.ChevronRight, null,
            tint     = KntWhite.copy(alpha = alpha * 0.5f),
            modifier = Modifier.size(20.dp),
        )
        Icon(
            Icons.Rounded.ChevronRight, null,
            tint     = KntWhite.copy(alpha = alpha),
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(2.dp))
        Text(
            "Swipe",
            style = MaterialTheme.typography.labelSmall.copy(
                color = KntWhite.copy(alpha = alpha),
            ),
        )
    }
}
