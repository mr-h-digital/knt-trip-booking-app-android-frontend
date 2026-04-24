package com.kntransport.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kntransport.app.data.*
import com.kntransport.app.network.ApiResult
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*
import com.kntransport.app.viewmodel.TripViewModel

@Composable
fun QuoteReviewScreen(
    quoteId  : String,
    type     : String,
    onBack   : () -> Unit,
    onDone   : () -> Unit,
    viewModel: TripViewModel = viewModel(),
) {
    val c          = LocalAppColors.current
    val quoteState by viewModel.quoteState.collectAsState()

    val isTrip = type == "TRIP"
    // Amounts shown from SampleData while we still use it for the trip/club detail
    val trip   = if (isTrip) SampleData.myTrips.firstOrNull { it.id == quoteId } else null
    val club   = if (!isTrip) SampleData.liftClubs.firstOrNull { it.id == quoteId } else null
    val amount = trip?.quotedAmount ?: club?.quotedAmount ?: 0.0

    var selectedCycle     by remember { mutableStateOf(PaymentCycle.MONTHLY) }
    var localDecision     by remember { mutableStateOf<Boolean?>(null) }
    var showDeclineDialog by remember { mutableStateOf(false) }
    var errorMessage      by remember { mutableStateOf<String?>(null) }

    val isLoading = quoteState is ApiResult.Loading

    LaunchedEffect(quoteState) {
        when (val s = quoteState) {
            is ApiResult.Success -> {
                viewModel.resetQuoteState()
                // Navigate to result screen based on localDecision
            }
            is ApiResult.Error -> { errorMessage = s.message; viewModel.resetQuoteState() }
            else -> {}
        }
    }

    val snackbarState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        errorMessage?.let { snackbarState.showSnackbar(it); errorMessage = null }
    }

    if (showDeclineDialog) {
        AlertDialog(
            onDismissRequest  = { showDeclineDialog = false },
            containerColor    = c.surface2,
            titleContentColor = c.textBright,
            textContentColor  = c.textMuted,
            title = { Text("Decline Quote?") },
            text  = { Text("Declining this quote will cancel the booking. K&T Transport will be notified.") },
            confirmButton = {
                Button(onClick = {
                    showDeclineDialog = false
                    localDecision = false
                    viewModel.respondToQuote(quoteId, accepted = false)
                }, colors = ButtonDefaults.buttonColors(containerColor = StatusRed)) {
                    Text("Decline")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeclineDialog = false }) { Text("Back", color = c.textMuted) }
            },
        )
    }

    // Show result screens once API responds (or immediately if already decided)
    val showResult = quoteState is ApiResult.Success || (localDecision != null && quoteState == null)

    KntScaffold(title = "Review Quote", onBack = onBack, snackbarHost = { SnackbarHost(snackbarState) }) { pv ->
        if (showResult) {
            when (localDecision) {
                true  -> QuoteAcceptedScreen(onDone = onDone)
                false -> QuoteDeclinedScreen(onDone = onDone)
                null  -> QuoteAcceptedScreen(onDone = onDone)
            }
            return@KntScaffold
        }

        when (localDecision) {
            true -> { QuoteAcceptedScreen(onDone = onDone); return@KntScaffold }
            false -> { QuoteDeclinedScreen(onDone = onDone); return@KntScaffold }
            null -> Column(
                Modifier.fillMaxSize().padding(pv).verticalScroll(rememberScrollState()).padding(16.dp)
            ) {
                Spacer(Modifier.height(12.dp))

                // Quote amount hero
                Surface(
                    shape    = RoundedCornerShape(20.dp),
                    color    = c.surface1,
                    border   = BorderStroke(1.5.dp, c.yellow.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.RequestQuote, null, tint = c.yellow, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "R${String.format("%.2f", amount)}",
                            style = MaterialTheme.typography.displayMedium.copy(color = c.yellow),
                        )
                        Text(
                            if (isTrip) "one-way trip" else "per person / ${club?.paymentCycle?.name?.lowercase() ?: ""}",
                            style = MaterialTheme.typography.bodySmall, color = c.textMuted,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            if (isTrip) "Payment upfront upon acceptance"
                            else "Choose your preferred payment cycle below",
                            style = MaterialTheme.typography.bodySmall, color = c.textMuted,
                        )
                    }
                }

                // Lift club payment cycle selector
                if (!isTrip) {
                    Spacer(Modifier.height(20.dp))
                    SectionHeader(title = "Payment Cycle")
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        PaymentCycle.entries.forEach { cycle ->
                            PaymentCycleChip(
                                cycle    = cycle,
                                selected = cycle == selectedCycle,
                                onClick  = { selectedCycle = cycle },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                // What's included
                Spacer(Modifier.height(20.dp))
                SectionHeader(title = "What's Included")
                KntCard {
                    listOf(
                        Icons.Rounded.DirectionsCar to "Door-to-door transport",
                        Icons.Rounded.Person to "Professional K&T driver",
                        Icons.Rounded.Security to "Safe & insured vehicle",
                        Icons.Rounded.Support to "24/7 support via WhatsApp",
                    ).forEach { (icon, text) ->
                        Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(icon, null, tint = StatusGreen, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(text, style = MaterialTheme.typography.bodyMedium, color = c.textBright)
                        }
                    }
                }

                // Trip / club summary
                Spacer(Modifier.height(20.dp))
                SectionHeader(title = "Booking Summary")
                KntCard {
                    if (isTrip && trip != null) {
                        InfoRow(Icons.Rounded.LocationOn, "Pickup", trip.pickupAddress)
                        InfoRow(Icons.Rounded.Flag, "Drop-off", trip.dropAddress)
                        InfoRow(Icons.Rounded.Schedule, "Date & Time", "${trip.date} · ${trip.time}")
                        InfoRow(Icons.Rounded.Person, "Passengers", trip.passengers.toString())
                    } else if (club != null) {
                        InfoRow(Icons.Rounded.LocationOn, "Pickup Area", club.pickupArea)
                        InfoRow(Icons.Rounded.Flag, "Drop Area", club.dropArea)
                        InfoRow(Icons.Rounded.Schedule, "Departs", club.departureTime.toString())
                        InfoRow(Icons.Rounded.CalendarViewWeek, "Days", club.daysOfWeek.joinToString(", "))
                        InfoRow(Icons.Rounded.People, "Passengers", "${club.subscriberCount} commuters")
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Accept / Decline
                KntPrimaryButton(
                    text    = if (isLoading) "Processing…" else "Accept Quote — R${String.format("%.2f", amount)}",
                    onClick = {
                        localDecision = true
                        viewModel.respondToQuote(
                            quoteId,
                            accepted     = true,
                            paymentCycle = if (!isTrip) selectedCycle.name else null,
                        )
                    },
                    enabled = !isLoading,
                    icon    = Icons.Rounded.CheckCircle,
                )
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick  = { showDeclineDialog = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    border   = BorderStroke(1.dp, StatusRed.copy(alpha = 0.6f)),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                ) {
                    Icon(Icons.Rounded.Cancel, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Decline Quote", style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp))
                }

                Spacer(Modifier.height(32.dp))
            } // null branch Column
        } // when(localDecision)
    } // KntScaffold
}

@Composable
private fun PaymentCycleChip(cycle: PaymentCycle, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    val label = when (cycle) {
        PaymentCycle.MONTHLY     -> "Monthly"
        PaymentCycle.WEEKLY      -> "Weekly"
        PaymentCycle.FORTNIGHTLY -> "Fortnightly"
    }
    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = if (selected) c.blue else c.surface2,
        border   = BorderStroke(1.dp, if (selected) c.blue else c.borderColor),
        modifier = modifier.clickable { onClick() },
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else c.textMuted,
            modifier = Modifier.padding(vertical = 12.dp).wrapContentWidth(Alignment.CenterHorizontally),
        )
    }
}

@Composable
private fun QuoteAcceptedScreen(onDone: () -> Unit) {
    val c = LocalAppColors.current
    Box(Modifier.fillMaxSize().background(c.bgDeep), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Surface(shape = CircleShape, color = StatusGreen.copy(alpha = 0.15f)) {
                Icon(Icons.Rounded.CheckCircle, null, tint = StatusGreen,
                    modifier = Modifier.size(80.dp).padding(18.dp))
            }
            Text("Booking Confirmed!", style = MaterialTheme.typography.headlineMedium, color = c.textBright)
            Text(
                "Your quote has been accepted. K&T Transport will be in touch with driver and vehicle details shortly.",
                style = MaterialTheme.typography.bodyMedium, color = c.textMuted,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(8.dp))
            KntPrimaryButton(text = "Back to Home", onClick = onDone, icon = Icons.Rounded.Home)
        }
    }
}

@Composable
private fun QuoteDeclinedScreen(onDone: () -> Unit) {
    val c = LocalAppColors.current
    Box(Modifier.fillMaxSize().background(c.bgDeep), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Surface(shape = CircleShape, color = StatusRed.copy(alpha = 0.12f)) {
                Icon(Icons.Rounded.Cancel, null, tint = StatusRed,
                    modifier = Modifier.size(80.dp).padding(18.dp))
            }
            Text("Quote Declined", style = MaterialTheme.typography.headlineMedium, color = c.textBright)
            Text(
                "You've declined the quote. K&T Transport has been notified.",
                style = MaterialTheme.typography.bodyMedium, color = c.textMuted,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(8.dp))
            KntPrimaryButton(text = "Back to Home", onClick = onDone, icon = Icons.Rounded.Home)
        }
    }
}
