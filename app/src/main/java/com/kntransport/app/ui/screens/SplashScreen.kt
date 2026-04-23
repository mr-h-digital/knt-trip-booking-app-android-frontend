package com.kntransport.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.*
import com.kntransport.app.R
import com.kntransport.app.ui.components.HeroBgImage
import com.kntransport.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    var tagVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100); visible = true
        delay(400); tagVisible = true
        delay(1800); onFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        // Hero photo full-bleed
        HeroBgImage(
            resId       = R.drawable.hero_bg,
            modifier    = Modifier.fillMaxSize(),
            darkOverlay = 0.62f,
        )

        // Blue radial glow over the photo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(KntBlue.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(0.5f, 0.45f),
                        radius = 800f,
                    )
                )
        )

        // Decorative vertical stripe
        Box(Modifier.align(Alignment.CenterEnd).width(5.dp).fillMaxHeight(0.6f)
            .background(KntBlue))
        Box(Modifier.align(Alignment.CenterEnd).offset(x = (-14).dp).width(2.dp).fillMaxHeight(0.4f)
            .background(KntYellow))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Main logo
            AnimatedVisibility(
                visible = visible,
                enter   = fadeIn(tween(600)) + slideInVertically(tween(600, easing = EaseOutCubic)) { it / 3 },
            ) {
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = "K&T Transport",
                    modifier = Modifier.width(260.dp),
                )
            }

            AnimatedVisibility(
                visible = tagVisible,
                enter   = fadeIn(tween(500)) + slideInVertically(tween(500)) { 20 },
            ) {
                Text(
                    text  = "Moving Communities Forward",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        letterSpacing = 1.sp,
                        color = KntMuted,
                    ),
                )
            }
        }

        // Loading indicator
        AnimatedVisibility(
            visible  = tagVisible,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 60.dp),
            enter    = fadeIn(tween(400)),
        ) {
            LinearProgressIndicator(
                modifier  = Modifier.width(120.dp).height(2.dp).clip(RoundedCornerShape(1.dp)),
                color     = KntBlue,
                trackColor= KntBlue.copy(alpha = 0.2f),
            )
        }

        // Developer credit — bottom of splash
        AnimatedVisibility(
            visible  = tagVisible,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp),
            enter    = fadeIn(tween(600, 300)),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "by",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color        = KntMuted.copy(alpha = 0.5f),
                        letterSpacing = 1.sp,
                    ),
                )
                Spacer(Modifier.height(4.dp))
                Image(
                    painter            = painterResource(R.drawable.mrh_digital_logo),
                    contentDescription = "Mr H Digital",
                    modifier           = Modifier.width(90.dp),
                    alpha              = 0.65f,
                )
            }
        }
    }
}
