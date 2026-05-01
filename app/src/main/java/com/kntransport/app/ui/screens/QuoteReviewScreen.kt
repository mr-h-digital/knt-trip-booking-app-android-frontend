package com.kntransport.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kntransport.app.data.*
import com.kntransport.app.network.ApiResult
import com.kntransport.app.network.QuoteDto
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*
import com.kntransport.app.viewmodel.TripViewModel

// ── Step enum ────────────────────────────────────────────────────────────────

private enum class ReviewStep { REVIEW, PAYMENT_METHOD, RESULT_ACCEPTED, RESULT_DECLINED }
private enum class PayMethod   { CASH, CARD }

// ── Main screen ───────────────────────────────────────────────────────────────

@Composable
fun QuoteReviewScreen(
    quoteId  : String,
    type     : String,
    onBack   : () -> Unit,
    onDone   : () -> Unit,
    viewModel: TripViewModel = viewModel(),
) {
    val c           = LocalAppColors.current
    val quoteState  by viewModel.quoteState.collectAsState()
    val quoteDetail by viewModel.selectedQuote.collectAsState()

    val isTrip = type == "TRIP"

    LaunchedEffect(quoteId) { viewModel.loadQuote(quoteId) }

    var step           by remember { mutableStateOf(ReviewStep.REVIEW) }
    var selectedMethod by remember { mutableStateOf<PayMethod?>(null) }
    var selectedCycle  by remember { mutableStateOf(PaymentCycle.MONTHLY) }
    var showDecline    by remember { mutableStateOf(false) }
    var errorMessage   by remember { mutableStateOf<String?>(null) }

    val isLoading = quoteState is ApiResult.Loading

    LaunchedEffect(quoteState) {
        when (val s = quoteState) {
            is ApiResult.Success -> {
                viewModel.resetQuoteState()
                step = ReviewStep.RESULT_ACCEPTED
            }
            is ApiResult.Error -> { errorMessage = s.message; viewModel.resetQuoteState() }
            else -> {}
        }
    }

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        errorMessage?.let { snackbar.showSnackbar(it); errorMessage = null }
    }

    // Decline dialog
    if (showDecline) {
        AlertDialog(
            onDismissRequest  = { showDecline = false },
            containerColor    = c.surface2,
            titleContentColor = c.textBright,
            textContentColor  = c.textMuted,
            title = { Text("Decline Quote?") },
            text  = { Text("Declining this quote will not cancel your booking. Other driver quotes will still be available.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDecline = false
                        viewModel.respondToQuote(quoteId, accepted = false)
                        step = ReviewStep.RESULT_DECLINED
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
                ) { Text("Decline") }
            },
            dismissButton = {
                TextButton(onClick = { showDecline = false }) { Text("Back", color = c.textMuted) }
            },
        )
    }

    KntScaffold(
        title  = when (step) {
            ReviewStep.REVIEW         -> "Review Quote"
            ReviewStep.PAYMENT_METHOD -> "How Would You Like to Pay?"
            else                      -> "Booking"
        },
        onBack = if (step == ReviewStep.PAYMENT_METHOD) {{ step = ReviewStep.REVIEW }} else onBack,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { pv ->
        when (step) {
            ReviewStep.RESULT_ACCEPTED -> QuoteAcceptedScreen(
                payMethod = selectedMethod,
                onDone    = onDone,
            )
            ReviewStep.RESULT_DECLINED -> QuoteDeclinedScreen(onDone = onDone)

            ReviewStep.PAYMENT_METHOD  -> PaymentMethodScreen(
                amount         = (quoteDetail as? ApiResult.Success<QuoteDto>)?.data?.amount ?: 0.0,
                selectedMethod = selectedMethod,
                onSelect       = { selectedMethod = it },
                isLoading      = isLoading,
                onConfirm      = {
                    viewModel.respondToQuote(
                        quoteId,
                        accepted      = true,
                        paymentCycle  = if (!isTrip) selectedCycle.name else null,
                        paymentMethod = selectedMethod?.name,
                    )
                },
                pv = pv,
            )

            ReviewStep.REVIEW -> {
                // Loading / error for quote detail
                if (quoteDetail == null || quoteDetail is ApiResult.Loading) {
                    Box(Modifier.fillMaxSize().padding(pv), Alignment.Center) {
                        CircularProgressIndicator(color = c.blue)
                    }
                    return@KntScaffold
                }
                if (quoteDetail is ApiResult.Error) {
                    Box(Modifier.fillMaxSize().padding(pv), Alignment.Center) {
                        ErrorState(
                            message = (quoteDetail as ApiResult.Error).message,
                            onRetry = { viewModel.loadQuote(quoteId) },
                        )
                    }
                    return@KntScaffold
                }

                val dto = (quoteDetail as ApiResult.Success<QuoteDto>).data

                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(pv)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                ) {
                    Spacer(Modifier.height(12.dp))

                    // Quote amount hero
                    Surface(
                        shape    = RoundedCornerShape(20.dp),
                        color    = c.surface1,
                        border   = BorderStroke(1.5.dp, c.yellow.copy(0.4f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.RequestQuote, null, tint = c.yellow, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "R${String.format("%.2f", dto.amount)}",
                                style = MaterialTheme.typography.displayMedium.copy(color = c.yellow),
                            )
                            Text(
                                if (isTrip) "one-way trip" else "per person",
                                style = MaterialTheme.typography.bodySmall, color = c.textMuted,
                            )
                            if (dto.driverName != null) {
                                Spacer(Modifier.height(10.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Box(
                                        Modifier.size(28.dp).clip(CircleShape).background(c.blue.copy(0.15f)),
                                        Alignment.Center,
                                    ) {
                                        Text(
                                            dto.driverName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = c.blue,
                                        )
                                    }
                                    Text(dto.driverName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = c.textBright)
                                }
                            }
                            if (dto.driverNote.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text("\"${dto.driverNote}\"", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (isTrip) "Choose your payment method on the next step"
                                else "Choose your preferred payment cycle below",
                                style = MaterialTheme.typography.bodySmall, color = c.textMuted,
                            )
                        }
                    }

                    // Lift club payment cycle
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
                            Icons.Rounded.Person        to "Professional K&T driver",
                            Icons.Rounded.Security      to "Safe & insured vehicle",
                            Icons.Rounded.Support       to "24/7 support via WhatsApp",
                        ).forEach { (icon, text) ->
                            Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(icon, null, tint = StatusGreen, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(text, style = MaterialTheme.typography.bodyMedium, color = c.textBright)
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Accept → go to payment method step
                    KntPrimaryButton(
                        text    = "Accept Quote — R${String.format("%.2f", dto.amount)}",
                        onClick = { step = ReviewStep.PAYMENT_METHOD },
                        icon    = Icons.Rounded.CheckCircle,
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick  = { showDecline = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(14.dp),
                        border   = BorderStroke(1.dp, StatusRed.copy(0.6f)),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                    ) {
                        Icon(Icons.Rounded.Cancel, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Decline Quote", style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp))
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

// ── Payment method selection ──────────────────────────────────────────────────

@Composable
private fun PaymentMethodScreen(
    amount         : Double,
    selectedMethod : PayMethod?,
    onSelect       : (PayMethod) -> Unit,
    isLoading      : Boolean,
    onConfirm      : () -> Unit,
    pv             : androidx.compose.foundation.layout.PaddingValues,
) {
    val c = LocalAppColors.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(pv)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Spacer(Modifier.height(8.dp))

        Text(
            "How would you like to pay R${String.format("%.2f", amount)}?",
            style     = MaterialTheme.typography.titleMedium,
            color     = c.textBright,
            textAlign = TextAlign.Center,
            modifier  = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(4.dp))

        // Cash option
        PayMethodCard(
            icon       = Icons.Rounded.Payments,
            iconTint   = StatusGreen,
            title      = "Cash",
            subtitle   = "Pay the driver in cash on the day of your trip.",
            badge      = "Pay on pickup",
            badgeColor = StatusGreen,
            selected   = selectedMethod == PayMethod.CASH,
            onClick    = { onSelect(PayMethod.CASH) },
        )

        // Card / EFT option
        PayMethodCard(
            icon       = Icons.Rounded.CreditCard,
            iconTint   = KntBlue,
            title      = "Card / EFT",
            subtitle   = "Pay securely online via card, instant EFT, or SnapScan.",
            badge      = "Coming soon",
            badgeColor = KntMuted,
            selected   = selectedMethod == PayMethod.CARD,
            onClick    = { onSelect(PayMethod.CARD) },
            disabled   = true,
            disabledNote = "Online payments are being set up. Cash is available now.",
        )

        // Security note
        Surface(
            shape  = RoundedCornerShape(12.dp),
            color  = c.blue.copy(0.06f),
            border = BorderStroke(1.dp, c.blue.copy(0.2f)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Info, null, tint = c.blue, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Your booking will be confirmed immediately after selecting a payment method.",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textMuted,
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Confirm button
        Button(
            onClick  = onConfirm,
            enabled  = selectedMethod != null && !isLoading,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(
                containerColor         = StatusGreen,
                contentColor           = Color.White,
                disabledContainerColor = c.surface2,
                disabledContentColor   = c.textDim,
            ),
            elevation = ButtonDefaults.buttonElevation(4.dp),
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Rounded.CheckCircle, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    when (selectedMethod) {
                        PayMethod.CASH -> "Confirm — Pay Cash on Pickup"
                        PayMethod.CARD -> "Confirm — Pay by Card"
                        null           -> "Select a payment method"
                    },
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                )
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun PayMethodCard(
    icon         : ImageVector,
    iconTint     : androidx.compose.ui.graphics.Color,
    title        : String,
    subtitle     : String,
    badge        : String,
    badgeColor   : androidx.compose.ui.graphics.Color,
    selected     : Boolean,
    onClick      : () -> Unit,
    disabled     : Boolean = false,
    disabledNote : String? = null,
) {
    val c          = LocalAppColors.current
    val borderColor = when {
        disabled  -> c.borderColor
        selected  -> iconTint.copy(0.6f)
        else      -> c.borderColor
    }
    val bgColor = when {
        disabled -> c.surface2.copy(0.4f)
        selected -> iconTint.copy(0.08f)
        else     -> c.surface2
    }

    Surface(
        shape    = RoundedCornerShape(16.dp),
        color    = bgColor,
        border   = BorderStroke(if (selected) 2.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!disabled) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(14.dp))
                        .background(iconTint.copy(if (disabled) 0.08f else 0.15f)),
                    Alignment.Center,
                ) {
                    Icon(
                        icon, null,
                        tint     = if (disabled) iconTint.copy(0.4f) else iconTint,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color      = if (disabled) c.textDim else c.textBright,
                        ),
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (disabled) c.textDim else c.textMuted,
                        ),
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(0.12f),
                ) {
                    Text(
                        badge,
                        style    = MaterialTheme.typography.labelSmall.copy(
                            color      = badgeColor,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
            // Selected indicator
            if (selected && !disabled) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Rounded.CheckCircle, null, tint = iconTint, modifier = Modifier.size(16.dp))
                    Text(
                        "Selected",
                        style = MaterialTheme.typography.labelSmall.copy(color = iconTint, fontWeight = FontWeight.Bold),
                    )
                }
            }
            // Disabled note
            if (disabled && disabledNote != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Rounded.Schedule, null, tint = c.textDim, modifier = Modifier.size(14.dp))
                    Text(disabledNote, style = MaterialTheme.typography.labelSmall, color = c.textDim)
                }
            }
        }
    }
}

// ── Result screens ────────────────────────────────────────────────────────────

@Composable
private fun QuoteAcceptedScreen(payMethod: PayMethod?, onDone: () -> Unit) {
    val c = LocalAppColors.current
    Box(Modifier.fillMaxSize().background(c.bgDeep), Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Surface(shape = CircleShape, color = StatusGreen.copy(0.15f)) {
                Icon(
                    Icons.Rounded.CheckCircle, null,
                    tint     = StatusGreen,
                    modifier = Modifier.size(80.dp).padding(18.dp),
                )
            }
            Text("Booking Confirmed!", style = MaterialTheme.typography.headlineMedium, color = c.textBright)
            Text(
                "The driver will be notified and your trip is now confirmed.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = c.textMuted,
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(horizontal = 16.dp),
            )
            // Payment reminder
            if (payMethod == PayMethod.CASH) {
                Spacer(Modifier.height(4.dp))
                Surface(
                    shape  = RoundedCornerShape(12.dp),
                    color  = StatusGreen.copy(0.10f),
                    border = BorderStroke(1.dp, StatusGreen.copy(0.3f)),
                ) {
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Rounded.Payments, null, tint = StatusGreen, modifier = Modifier.size(18.dp))
                        Text(
                            "Remember to have R${""} cash ready for your driver on pickup.",
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusGreen,
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            KntPrimaryButton(text = "Back to Home", onClick = onDone, icon = Icons.Rounded.Home)
        }
    }
}

@Composable
private fun QuoteDeclinedScreen(onDone: () -> Unit) {
    val c = LocalAppColors.current
    Box(Modifier.fillMaxSize().background(c.bgDeep), Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Surface(shape = CircleShape, color = StatusRed.copy(0.12f)) {
                Icon(Icons.Rounded.Cancel, null, tint = StatusRed, modifier = Modifier.size(80.dp).padding(18.dp))
            }
            Text("Quote Declined", style = MaterialTheme.typography.headlineMedium, color = c.textBright)
            Text(
                "You've declined this quote. You can still review other driver quotes on your trip.",
                style     = MaterialTheme.typography.bodyMedium,
                color     = c.textMuted,
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(8.dp))
            KntPrimaryButton(text = "Back to Trip", onClick = onDone, icon = Icons.Rounded.ArrowBack)
        }
    }
}

// ── Payment cycle chip (lift clubs) ──────────────────────────────────────────

@Composable
private fun PaymentCycleChip(cycle: PaymentCycle, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = if (selected) c.blue else c.surface2,
        border   = BorderStroke(1.dp, if (selected) c.blue else c.borderColor),
        modifier = modifier.clickable { onClick() },
    ) {
        Text(
            when (cycle) {
                PaymentCycle.MONTHLY     -> "Monthly"
                PaymentCycle.WEEKLY      -> "Weekly"
                PaymentCycle.FORTNIGHTLY -> "Fortnightly"
            },
            style    = MaterialTheme.typography.labelMedium,
            color    = if (selected) Color.White else c.textMuted,
            modifier = Modifier.padding(vertical = 12.dp).wrapContentWidth(Alignment.CenterHorizontally),
        )
    }
}
