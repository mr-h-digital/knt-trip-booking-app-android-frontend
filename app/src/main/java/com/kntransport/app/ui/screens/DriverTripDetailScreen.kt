package com.kntransport.app.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.kntransport.app.R
import com.kntransport.app.data.*
import com.kntransport.app.network.ApiResult
import com.kntransport.app.network.QuoteDto
import com.kntransport.app.network.TripBookingDto
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*
import com.kntransport.app.viewmodel.DriverViewModel
import java.time.format.DateTimeFormatter

@Composable
fun DriverTripDetailScreen(
    tripId    : String,
    onBack    : () -> Unit,
    viewModel : DriverViewModel = viewModel(),
) {
    val c          = LocalAppColors.current
    val tripState by viewModel.selectedTrip.collectAsState()
    val actionState by viewModel.tripActionState.collectAsState()

    LaunchedEffect(tripId) { viewModel.loadTrip(tripId) }

    if (tripState is ApiResult.Loading || tripState == null) {
        KntScaffold(title = "Trip Detail", onBack = onBack) { pv ->
            Box(Modifier.fillMaxSize().padding(pv), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        }
        return
    }
    if (tripState is ApiResult.Error) {
        KntScaffold(title = "Trip Detail", onBack = onBack) { pv ->
            Box(Modifier.fillMaxSize().padding(pv), contentAlignment = Alignment.Center) {
                ErrorState(message = (tripState as ApiResult.Error).message, onRetry = { viewModel.loadTrip(tripId) })
            }
        }
        return
    }

    val dto = (tripState as ApiResult.Success<TripBookingDto>).data

    var currentStatus      by remember(dto.id) { mutableStateOf(dto.status) }
    var showConfirmDialog  by remember { mutableStateOf(false) }
    var showCancelSheet    by remember { mutableStateOf(false) }
    var pendingStatus      by remember { mutableStateOf<String?>(null) }
    var errorMessage       by remember { mutableStateOf<String?>(null) }

    // Quote flow state — seed from myQuote if backend returns it
    val quoteState       by viewModel.quoteState.collectAsState()
    val cancelQuoteState by viewModel.cancelQuoteState.collectAsState()
    var showQuoteSheet   by remember { mutableStateOf(false) }
    var editingQuote     by remember { mutableStateOf<QuoteDto?>(null) }
    var activeQuoteId     by remember(dto.id) { mutableStateOf<String?>(dto.myQuote?.id) }
    var activeQuoteAmount by remember(dto.id) { mutableStateOf<Double?>(dto.myQuote?.amount ?: dto.quotedAmount) }
    var activeQuoteNote   by remember(dto.id) { mutableStateOf(dto.myQuote?.driverNote ?: "") }
    var quoteSuccessMsg   by remember { mutableStateOf<String?>(null) }

    // Track the quote this driver sent (stored locally after creation)
    LaunchedEffect(quoteState) {
        when (val s = quoteState) {
            is ApiResult.Success -> {
                val wasEdit       = editingQuote != null
                activeQuoteId     = s.data.id
                activeQuoteAmount = s.data.amount
                activeQuoteNote   = s.data.driverNote
                currentStatus     = "QUOTE_SENT"
                showQuoteSheet    = false
                editingQuote      = null
                quoteSuccessMsg   = if (wasEdit) "Quote updated successfully" else "Quote submitted successfully"
                viewModel.resetQuoteState()
            }
            is ApiResult.Error -> { errorMessage = s.message; viewModel.resetQuoteState() }
            else -> {}
        }
    }
    LaunchedEffect(cancelQuoteState) {
        when (val s = cancelQuoteState) {
            is ApiResult.Success -> {
                activeQuoteId     = null
                activeQuoteAmount = null
                activeQuoteNote   = ""
                currentStatus     = "PENDING_QUOTE"
                viewModel.resetCancelQuoteState()
            }
            is ApiResult.Error -> { errorMessage = s.message; viewModel.resetCancelQuoteState() }
            else -> {}
        }
    }

    val isSharingLocation by viewModel.isSharingLocation.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Permission launcher for fine location
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) viewModel.startSharingLocation() }

    // Wire GPS → ViewModel whenever location sharing is active and trip is IN_PROGRESS
    @SuppressLint("MissingPermission")
    fun startGpsUpdates() {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5_000L).build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { loc ->
                    viewModel.broadcastLocation(dto.id, loc.latitude, loc.longitude)
                }
            }
        }
        fusedClient.requestLocationUpdates(request, callback, context.mainLooper)
    }

    LaunchedEffect(isSharingLocation, currentStatus) {
        if (isSharingLocation && currentStatus == "IN_PROGRESS") {
            val hasPerm = ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPerm) startGpsUpdates()
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopSharingLocation() }
    }

    LaunchedEffect(actionState) {
        when (val s = actionState) {
            is ApiResult.Success -> { currentStatus = s.data.status; viewModel.resetTripActionState() }
            is ApiResult.Error   -> { errorMessage = s.message;      viewModel.resetTripActionState() }
            else -> {}
        }
    }
    val snackbarState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        errorMessage?.let { snackbarState.showSnackbar(it); errorMessage = null }
    }
    LaunchedEffect(quoteSuccessMsg) {
        quoteSuccessMsg?.let { snackbarState.showSnackbar(it); quoteSuccessMsg = null }
    }


    if (showCancelSheet) {
        CancelTripSheet(
            reasons   = DRIVER_CANCEL_REASONS,
            onDismiss = { showCancelSheet = false },
            onConfirm = { reason, note ->
                viewModel.cancelTrip(tripId, reason, note)
                showCancelSheet = false
            },
        )
    }

    if (showQuoteSheet) {
        DriverQuoteSheet(
            existingQuote = editingQuote,
            onDismiss     = { showQuoteSheet = false; editingQuote = null },
            onSubmit      = { amount, note ->
                val qId = editingQuote?.id
                if (qId != null) viewModel.editQuote(qId, amount, note)
                else             viewModel.createQuote(tripId, amount, note)
            },
            isLoading = quoteState is ApiResult.Loading,
        )
    }

    if (showConfirmDialog && pendingStatus != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor   = c.surface2,
            title = {
                Text(
                    when (pendingStatus) {
                        "IN_PROGRESS" -> "Start Trip?"
                        "COMPLETED"   -> "Complete Trip?"
                        else          -> "Update Status?"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = c.textBright,
                )
            },
            text = {
                Text(
                    when (pendingStatus) {
                        "IN_PROGRESS" -> "Confirm that you have picked up ${dto.commuterName} and the trip has started."
                        "COMPLETED"   -> "Confirm that you have safely dropped off ${dto.commuterName} and the trip is complete."
                        else          -> "Update the trip status?"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textMuted,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingStatus?.let { viewModel.updateTripStatus(tripId, it) }
                        showConfirmDialog = false
                        pendingStatus    = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (pendingStatus == "COMPLETED") StatusGreen else c.blue
                    ),
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel", color = c.textMuted)
                }
            },
        )
    }

    KntScaffold(title = "Trip Detail", onBack = onBack, snackbarHost = { SnackbarHost(snackbarState) }) { pv ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pv)
                .verticalScroll(rememberScrollState()),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            ) {
                HeroBgImage(resId = R.drawable.hero_bg_4, modifier = Modifier.fillMaxSize(), darkOverlay = 0.52f)
                Column(Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 12.dp)) {
                    Text(
                        dto.commuterName ?: "Commuter",
                        style = MaterialTheme.typography.titleMedium.copy(color = KntWhite),
                    )
                    Text(
                        "${dto.pickupAddress}  →  ${dto.dropAddress}",
                        style = MaterialTheme.typography.bodySmall.copy(color = KntYellow),
                        maxLines = 1,
                    )
                }
            }

            Column(Modifier.padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(16.dp))

            // ── Status banner ─────────────────────────────────────────────
            val (bannerColor, bannerLabel) = when (currentStatus) {
                "IN_PROGRESS"    -> KntBlue to "Trip In Progress"
                "CONFIRMED",
                "QUOTE_ACCEPTED" -> KntYellow to "Confirmed — Ready to Start"
                "COMPLETED"      -> StatusGreen to "Trip Completed"
                "CANCELLED"      -> StatusRed to "Trip Cancelled"
                else             -> KntMuted to currentStatus.replace("_", " ")
            }
            Surface(
                shape    = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                color    = bannerColor.copy(0.12f),
                border   = BorderStroke(1.dp, bannerColor.copy(0.4f)),
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(bannerColor))
                    Spacer(Modifier.width(10.dp))
                    Text(bannerLabel, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = bannerColor)
                }
            }

            // ── Location sharing toggle (only visible while trip is active) ──
            if (currentStatus == "IN_PROGRESS") {
                Spacer(Modifier.height(12.dp))
                DriverLocationSharingBadge(
                    isSharing = isSharingLocation,
                    onToggle  = {
                        if (!isSharingLocation) {
                            val hasPerm = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPerm) viewModel.startSharingLocation()
                            else permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        } else {
                            viewModel.stopSharingLocation()
                        }
                    },
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── Commuter info ─────────────────────────────────────────────
            SectionHeader(title = "Commuter")
            Spacer(Modifier.height(10.dp))
            Surface(
                shape  = RoundedCornerShape(14.dp),
                color  = c.surface2,
                border = BorderStroke(1.dp, c.borderColor),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape)
                            .background(Brush.linearGradient(listOf(KntBlue.copy(0.7f), KntBlueBright.copy(0.5f)))),
                        contentAlignment = Alignment.Center,
                    ) {
                        val initials = (dto.commuterName ?: "?").split(" ")
                            .mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
                        Text(initials, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(dto.commuterName ?: "Commuter", style = MaterialTheme.typography.titleSmall, color = c.textBright)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Rounded.Person, null, tint = c.textDim, modifier = Modifier.size(12.dp))
                            Text("${dto.passengers} passenger${if (dto.passengers > 1) "s" else ""}", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                        }
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Rounded.Phone, null, tint = c.blue, modifier = Modifier.size(22.dp))
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Rounded.Message, null, tint = c.yellow, modifier = Modifier.size(22.dp))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Route ─────────────────────────────────────────────────────
            SectionHeader(title = "Route")
            Spacer(Modifier.height(10.dp))
            Surface(
                shape  = RoundedCornerShape(14.dp),
                color  = c.surface2,
                border = BorderStroke(1.dp, c.borderColor),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.size(10.dp).clip(CircleShape).background(c.blue))
                            Box(Modifier.width(2.dp).height(24.dp).background(c.borderColor))
                            Box(Modifier.size(10.dp).clip(CircleShape).background(KntYellow))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                            Column {
                                Text("Pickup", style = MaterialTheme.typography.labelSmall, color = c.textDim)
                                Text(dto.pickupAddress, style = MaterialTheme.typography.bodyMedium, color = c.textBright)
                            }
                            Column {
                                Text("Drop-off", style = MaterialTheme.typography.labelSmall, color = c.textDim)
                                Text(dto.dropAddress, style = MaterialTheme.typography.bodyMedium, color = c.textBright)
                            }
                        }
                    }
                    KntDivider()
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        InfoRow(Icons.Rounded.CalendarMonth, "Date", dto.date)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        InfoRow(Icons.Rounded.Schedule, "Time", dto.time)
                    }
                    if (dto.notes.isNotBlank()) {
                        Row {
                            InfoRow(Icons.Rounded.Notes, "Notes", dto.notes)
                        }
                    }
                }
            }

            // ── Payment method badge ──────────────────────────────────────
            if (dto.paymentMethod != null) {
                Spacer(Modifier.height(12.dp))
                val isCash = dto.paymentMethod == "CASH"
                Surface(
                    shape  = RoundedCornerShape(12.dp),
                    color  = if (isCash) StatusGreen.copy(0.10f) else c.blue.copy(0.10f),
                    border = BorderStroke(1.dp, if (isCash) StatusGreen.copy(0.35f) else c.blue.copy(0.35f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            if (isCash) Icons.Rounded.Payments else Icons.Rounded.CreditCard,
                            null,
                            tint     = if (isCash) StatusGreen else c.blue,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            if (isCash) "Commuter paying cash on pickup" else "Commuter paying by card",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isCash) StatusGreen else c.blue,
                        )
                    }
                }
            }

            // ── Earnings / quoted amount ───────────────────────────────────
            val displayAmount = activeQuoteAmount ?: dto.quotedAmount
            if (displayAmount != null) {
                val isPending = currentStatus == "QUOTE_SENT" && activeQuoteId != null
                Spacer(Modifier.height(20.dp))
                Surface(
                    shape  = RoundedCornerShape(14.dp),
                    color  = c.surface2,
                    border = BorderStroke(1.dp, KntYellow.copy(0.3f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Payments, null, tint = KntYellow, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (isPending) "Your Quote" else "Trip Earnings",
                                style = MaterialTheme.typography.bodySmall,
                                color = c.textMuted,
                            )
                            GradientText(
                                text   = "R${String.format("%.2f", displayAmount)}",
                                style  = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                colors = listOf(KntYellow, KntOrange),
                            )
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = if (isPending) c.yellow.copy(0.12f) else StatusGreen.copy(0.12f)) {
                            Text(
                                if (isPending) "Awaiting response" else "On completion",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isPending) c.yellow else StatusGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }

            // ── Action buttons ────────────────────────────────────────────
            Spacer(Modifier.height(28.dp))

            when (currentStatus) {
                "PENDING_QUOTE" -> {
                    KntPrimaryButton(
                        text    = "Quote This Trip",
                        onClick = { editingQuote = null; showQuoteSheet = true },
                        icon    = Icons.Rounded.RequestQuote,
                    )
                }
                "QUOTE_SENT" -> {
                    if (activeQuoteId != null) {
                        // This driver sent the quote
                        KntPrimaryButton(
                            text    = "Edit Quote",
                            onClick = {
                                editingQuote = QuoteDto(
                                    id            = activeQuoteId!!,
                                    referenceId   = dto.id,
                                    referenceType = "TRIP",
                                    amount        = activeQuoteAmount ?: 0.0,
                                    driverNote    = activeQuoteNote,
                                )
                                showQuoteSheet = true
                            },
                            icon = Icons.Rounded.Edit,
                        )
                        Spacer(Modifier.height(8.dp))
                        KntSecondaryButton(
                            text    = "Cancel Quote",
                            onClick = { viewModel.cancelQuote(activeQuoteId!!) },
                            icon    = Icons.Rounded.Cancel,
                        )
                    } else {
                        // Another driver's quote is active — just show info
                        Surface(
                            shape  = RoundedCornerShape(14.dp),
                            color  = c.yellow.copy(0.10f),
                            border = BorderStroke(1.dp, c.yellow.copy(0.3f)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.HourglassTop, null, tint = c.yellow, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Text("Quote sent — awaiting commuter response",
                                    style = MaterialTheme.typography.bodySmall, color = c.yellow)
                            }
                        }
                    }
                }
                "CONFIRMED", "QUOTE_ACCEPTED" -> {
                    KntPrimaryButton(
                        text    = "Start Trip",
                        onClick = { pendingStatus = "IN_PROGRESS"; showConfirmDialog = true },
                        icon    = Icons.Rounded.PlayArrow,
                    )
                    Spacer(Modifier.height(8.dp))
                    KntSecondaryButton(
                        text    = "Cancel Trip",
                        onClick = { showCancelSheet = true },
                        icon    = Icons.Rounded.Cancel,
                    )
                }
                "IN_PROGRESS" -> {
                    KntPrimaryButton(
                        text    = "Complete Trip",
                        onClick = { pendingStatus = "COMPLETED"; showConfirmDialog = true },
                        icon    = Icons.Rounded.CheckCircle,
                    )
                }
                "COMPLETED" -> {
                    Surface(
                        shape    = RoundedCornerShape(14.dp),
                        color    = StatusGreen.copy(0.1f),
                        border   = BorderStroke(1.dp, StatusGreen.copy(0.3f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CheckCircle, null, tint = StatusGreen, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Trip completed", style = MaterialTheme.typography.titleSmall, color = StatusGreen)
                        }
                    }
                }
                "CANCELLED" -> {
                    Surface(
                        shape    = RoundedCornerShape(14.dp),
                        color    = StatusRed.copy(0.1f),
                        border   = BorderStroke(1.dp, StatusRed.copy(0.3f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Cancel, null, tint = StatusRed, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Trip cancelled", style = MaterialTheme.typography.titleSmall, color = StatusRed)
                        }
                    }
                }
                else -> {}
            }

            Spacer(Modifier.height(32.dp))
            } // close inner padding Column
        }
    }
}

// ── Quote bottom sheet ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DriverQuoteSheet(
    existingQuote : com.kntransport.app.network.QuoteDto?,
    onDismiss     : () -> Unit,
    onSubmit      : (amount: Double, note: String) -> Unit,
    isLoading     : Boolean,
) {
    val c       = LocalAppColors.current
    val isEdit  = existingQuote != null
    var amount  by remember(existingQuote) { mutableStateOf(existingQuote?.amount?.let { "%.2f".format(it) } ?: "") }
    var note    by remember(existingQuote) { mutableStateOf(existingQuote?.driverNote ?: "") }
    val isValid = amount.toDoubleOrNull()?.let { it > 0 } == true

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = c.surface1,
        dragHandle = {
            Box(Modifier.padding(vertical = 10.dp).width(36.dp).height(4.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(2.dp)).background(c.borderColor))
        },
    ) {
        Column(
            Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(38.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                        .background(c.yellow.copy(0.14f)),
                    Alignment.Center,
                ) {
                    Icon(Icons.Rounded.RequestQuote, null, tint = c.yellow, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    if (isEdit) "Edit Quote" else "Submit a Quote",
                    style = MaterialTheme.typography.titleMedium,
                    color = c.textBright,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Enter the price you want to charge for this trip.",
                style = MaterialTheme.typography.bodySmall,
                color = c.textMuted,
            )
            Spacer(Modifier.height(20.dp))

            KntTextField(
                value         = amount,
                onValueChange = { amount = it },
                label         = "Quote Amount (R)",
                leadingIcon   = Icons.Rounded.Payments,
                keyboardType  = androidx.compose.ui.text.input.KeyboardType.Decimal,
            )
            Spacer(Modifier.height(12.dp))
            KntTextField(
                value         = note,
                onValueChange = { note = it },
                label         = "Note to commuter (optional)",
                leadingIcon   = Icons.Rounded.Notes,
                singleLine    = false,
                maxLines      = 3,
            )
            Spacer(Modifier.height(20.dp))

            Button(
                onClick  = { amount.toDoubleOrNull()?.let { onSubmit(it, note.trim()) } },
                enabled  = isValid && !isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = c.yellow, contentColor = Color.Black),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.Black)
                } else {
                    Icon(Icons.Rounded.Send, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isEdit) "Update Quote" else "Send Quote",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge, color = c.textMuted)
            }
        }
    }
}
