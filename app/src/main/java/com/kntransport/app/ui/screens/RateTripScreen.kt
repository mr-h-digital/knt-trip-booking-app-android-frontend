package com.kntransport.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kntransport.app.data.*
import com.kntransport.app.network.ApiResult
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*
import com.kntransport.app.viewmodel.TripViewModel

@Composable
fun RateTripScreen(
    tripId    : String,
    onDone    : () -> Unit,
    viewModel : TripViewModel = viewModel(),
) {
    val c         = LocalAppColors.current
    val rateState by viewModel.rateState.collectAsState()
    val tripState by viewModel.selectedTrip.collectAsState()
    val tripDto   = (tripState as? ApiResult.Success)?.data

    LaunchedEffect(tripId) { viewModel.loadTrip(tripId) }

    var rating      by remember { mutableIntStateOf(0) }
    var hoveredStar by remember { mutableIntStateOf(0) }
    var comment     by remember { mutableStateOf("") }
    var submitted   by remember { mutableStateOf(false) }

    LaunchedEffect(rateState) {
        when (rateState) {
            is ApiResult.Success -> { submitted = true; viewModel.resetRateState() }
            is ApiResult.Error   -> viewModel.resetRateState()
            else -> {}
        }
    }

    val ratingLabel = when (rating) {
        1 -> "Poor"
        2 -> "Below average"
        3 -> "Average"
        4 -> "Good"
        5 -> "Excellent!"
        else -> "Tap a star to rate"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bgDeep),
    ) {
        // Background glow
        Box(
            Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    listOf(KntBlue.copy(0.08f), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(500f, 400f),
                    radius = 900f,
                )
            )
        )

        if (submitted) {
            // ── Thank you state ───────────────────────────────────────────
            Column(
                modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                val checkScale by animateFloatAsState(
                    targetValue   = 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                    label         = "checkScale",
                )
                Box(
                    Modifier.size(80.dp).clip(CircleShape)
                        .background(StatusGreen.copy(0.15f)),
                    Alignment.Center,
                ) {
                    Icon(Icons.Rounded.CheckCircle, null, tint = StatusGreen, modifier = Modifier.size(44.dp).scale(checkScale))
                }
                Spacer(Modifier.height(20.dp))
                Text("Thank you!", style = MaterialTheme.typography.displaySmall.copy(color = c.textBright))
                Spacer(Modifier.height(8.dp))
                Text("Your rating has been submitted.", style = MaterialTheme.typography.bodyMedium, color = c.textMuted)
                Spacer(Modifier.height(32.dp))
                KntPrimaryButton(text = "Done", onClick = onDone, modifier = Modifier.padding(horizontal = 48.dp))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(c.headerStart, c.headerEnd)))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onDone) {
                            Icon(Icons.Rounded.ArrowBackIosNew, null, tint = c.headerText, modifier = Modifier.size(20.dp))
                        }
                        Text("Rate Your Trip", style = MaterialTheme.typography.titleLarge, color = c.headerText)
                    }
                    Box(Modifier.align(Alignment.BottomEnd).width(40.dp).height(2.dp).background(c.yellow))
                }

                Spacer(Modifier.height(32.dp))

                // Driver avatar + name
                if (tripDto?.driverName != null) {
                    Box(
                        Modifier.size(72.dp).clip(CircleShape)
                            .background(Brush.linearGradient(listOf(c.blue, c.yellow.copy(0.6f)))),
                        Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.Person, null, tint = c.bgDeep, modifier = Modifier.size(38.dp))
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(tripDto!!.driverName!!, style = MaterialTheme.typography.headlineSmall, color = c.textBright)
                    Text("K&T Transport Driver", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "To: ${tripDto!!.dropAddress}",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textDim,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp),
                    )
                } else {
                    Box(
                        Modifier.size(72.dp).clip(CircleShape).background(c.surface1),
                        Alignment.Center,
                    ) {
                        Icon(Icons.Rounded.DirectionsBus, null, tint = c.blue, modifier = Modifier.size(38.dp))
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Rate your trip", style = MaterialTheme.typography.headlineSmall, color = c.textBright)
                }

                Spacer(Modifier.height(32.dp))

                // ── Stars ──────────────────────────────────────────────────
                Text(
                    ratingLabel,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = when (rating) {
                            5    -> KntYellow
                            4    -> StatusGreen
                            3    -> KntBlueBright
                            2    -> KntOrange
                            1    -> StatusRed
                            else -> c.textDim
                        }
                    ),
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..5).forEach { star ->
                        val filled   = star <= (if (hoveredStar > 0) hoveredStar else rating)
                        val starScale by animateFloatAsState(
                            targetValue   = if (filled) 1.25f else 1f,
                            animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium),
                            label         = "star$star",
                        )
                        Icon(
                            imageVector = if (filled) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                            contentDescription = "$star stars",
                            tint     = if (filled) KntYellow else c.textDim,
                            modifier = Modifier
                                .size(48.dp)
                                .scale(starScale)
                                .clickable {
                                    rating = star
                                    hoveredStar = 0
                                },
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                // ── Comment field ──────────────────────────────────────────
                Column(Modifier.padding(horizontal = 24.dp).fillMaxWidth()) {
                    Text(
                        "Leave a comment (optional)",
                        style = MaterialTheme.typography.labelMedium,
                        color = c.textMuted,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    OutlinedTextField(
                        value         = comment,
                        onValueChange = { if (it.length <= 200) comment = it },
                        placeholder   = { Text("How was your experience?", style = MaterialTheme.typography.bodySmall, color = c.textDim) },
                        singleLine    = false,
                        maxLines      = 4,
                        modifier      = Modifier.fillMaxWidth().height(110.dp),
                        shape         = RoundedCornerShape(12.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor      = c.blue,
                            unfocusedBorderColor    = c.borderColor,
                            focusedTextColor        = c.textBright,
                            unfocusedTextColor      = c.textBright,
                            cursorColor             = c.blue,
                            focusedContainerColor   = c.surface2,
                            unfocusedContainerColor = c.surface2,
                        ),
                    )
                    Text(
                        "${comment.length}/200",
                        style    = MaterialTheme.typography.labelSmall,
                        color    = c.textDim,
                        modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
                    )
                }

                Spacer(Modifier.height(28.dp))

                // ── Submit ─────────────────────────────────────────────────
                Column(Modifier.padding(horizontal = 24.dp).fillMaxWidth()) {
                    Button(
                        onClick  = { if (rating > 0) viewModel.rateTrip(tripId, rating, comment) },
                        enabled  = rating > 0,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor         = c.blue,
                            contentColor           = Color.White,
                            disabledContainerColor = c.surface2,
                            disabledContentColor   = c.textDim,
                        ),
                    ) {
                        Icon(Icons.Rounded.Star, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Submit Rating",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                        Text("Skip", style = MaterialTheme.typography.labelLarge, color = c.textDim)
                    }
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}
