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
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val heroRes    : Int,
    val icon       : androidx.compose.ui.graphics.vector.ImageVector,
    val iconTint   : Color,
    val title      : String,
    val subtitle   : String,
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

    Box(Modifier.fillMaxSize().background(KntBlack)) {
        // Pager
        HorizontalPager(
            state    = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            val p = PAGES[page]
            Box(Modifier.fillMaxSize()) {
                HeroBgImage(resId = p.heroRes, modifier = Modifier.fillMaxSize(), darkOverlay = 0.72f)

                // Blue gradient overlay bottom half for text readability
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, KntBlack.copy(0.95f)),
                            startY = 400f,
                        )
                    )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 32.dp)
                        .padding(bottom = 160.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Icon badge
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
                        style    = MaterialTheme.typography.bodyMedium.copy(color = KntMuted),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .padding(bottom = 48.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Page dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

            Spacer(Modifier.height(24.dp))

            // Action button
            Button(
                onClick = {
                    if (isLast) onFinished()
                    else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = KntBlue, contentColor = Color.White),
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

            // Skip
            if (!isLast) {
                TextButton(onClick = onFinished) {
                    Text("Skip", style = MaterialTheme.typography.labelLarge, color = KntMuted)
                }
            }
        }
    }
}
