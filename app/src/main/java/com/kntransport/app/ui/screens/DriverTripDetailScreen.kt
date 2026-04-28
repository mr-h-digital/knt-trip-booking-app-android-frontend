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

            // ── Earnings ──────────────────────────────────────────────────
            if (dto.quotedAmount != null) {
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
                            Text("Trip Earnings", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                            GradientText(
                                text   = "R${String.format("%.2f", dto.quotedAmount)}",
                                style  = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                colors = listOf(KntYellow, KntOrange),
                            )
                        }
                        Surface(shape = RoundedCornerShape(8.dp), color = StatusGreen.copy(0.12f)) {
                            Text("On completion", style = MaterialTheme.typography.labelSmall, color = StatusGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }
            }

            // ── Action buttons ────────────────────────────────────────────
            Spacer(Modifier.height(28.dp))

            when (currentStatus) {
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
