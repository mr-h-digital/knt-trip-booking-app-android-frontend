package com.kntransport.app.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kntransport.app.R
import com.kntransport.app.network.ApiResult
import com.kntransport.app.network.TripBookingDto
import com.kntransport.app.network.UserDto
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*
import com.kntransport.app.viewmodel.AdminViewModel

@Composable
fun AdminTripDetailScreen(
    tripId    : String,
    onBack    : () -> Unit,
    viewModel : AdminViewModel = viewModel(),
) {
    val c           = LocalAppColors.current
    val tripState  by viewModel.selectedTrip.collectAsState()
    val actionState by viewModel.tripActionState.collectAsState()
    val usersState  by viewModel.users.collectAsState()

    LaunchedEffect(tripId) {
        viewModel.loadTrip(tripId)
        viewModel.loadUsers()
    }

    var currentStatus  by remember { mutableStateOf("") }
    var showCancel     by remember { mutableStateOf(false) }
    var showQuoteSheet by remember { mutableStateOf(false) }
    var showDriverSheet by remember { mutableStateOf(false) }
    var errorMessage   by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage)   { errorMessage?.let   { snackbar.showSnackbar(it); errorMessage   = null } }
    LaunchedEffect(successMessage) { successMessage?.let { snackbar.showSnackbar(it); successMessage = null } }

    LaunchedEffect(actionState) {
        when (val s = actionState) {
            is ApiResult.Success -> {
                currentStatus  = s.data.status
                successMessage = "Trip updated"
                viewModel.resetTripActionState()
                viewModel.loadTrip(tripId)
            }
            is ApiResult.Error -> { errorMessage = s.message; viewModel.resetTripActionState() }
            else -> {}
        }
    }

    // Loading / error guards
    if (tripState == null || tripState is ApiResult.Loading) {
        KntScaffold(title = "Trip Detail", onBack = onBack) { pv ->
            Box(Modifier.fillMaxSize().padding(pv), Alignment.Center) { CircularProgressIndicator() }
        }
        return
    }
    if (tripState is ApiResult.Error) {
        KntScaffold(title = "Trip Detail", onBack = onBack) { pv ->
            Box(Modifier.fillMaxSize().padding(pv), Alignment.Center) {
                ErrorState(message = (tripState as ApiResult.Error).message, onRetry = { viewModel.loadTrip(tripId) })
            }
        }
        return
    }

    val dto = (tripState as ApiResult.Success<TripBookingDto>).data
    if (currentStatus.isEmpty()) currentStatus = dto.status

    val drivers = (usersState as? ApiResult.Success)?.data?.filter { it.role == "DRIVER" } ?: emptyList()

    // Cancel sheet
    if (showCancel) {
        CancelTripSheet(
            reasons   = listOf("Admin override", "Commuter request", "Driver unavailable", "Safety concern", "Other"),
            onDismiss = { showCancel = false },
            onConfirm = { reason, note ->
                viewModel.cancelTrip(tripId, reason, note)
                showCancel = false
            },
        )
    }

    // Quote update sheet
    if (showQuoteSheet) {
        AdminQuoteSheet(
            current   = dto.quotedAmount,
            onDismiss = { showQuoteSheet = false },
            onConfirm = { amount ->
                viewModel.updateQuote(tripId, amount)
                showQuoteSheet = false
            },
        )
    }

    // Driver assignment sheet
    if (showDriverSheet) {
        DriverPickerSheet(
            drivers    = drivers,
            isLoading  = usersState is ApiResult.Loading,
            currentId  = dto.driverId,
            onSelect   = { driver ->
                viewModel.assignDriver(tripId, driver.id)
                showDriverSheet = false
            },
            onUnassign = null,
            onDismiss  = { showDriverSheet = false },
        )
    }

    KntScaffold(title = "Trip Detail", onBack = onBack, snackbarHost = { SnackbarHost(snackbar) }) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv)
                .verticalScroll(rememberScrollState()),
        ) {
            // Hero
            Box(
                modifier = Modifier.fillMaxWidth().height(110.dp)
                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            ) {
                HeroBgImage(resId = R.drawable.hero_bg_7, modifier = Modifier.fillMaxSize(), darkOverlay = 0.52f)
                Column(Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 12.dp)) {
                    Text(dto.commuterName ?: "Commuter", style = MaterialTheme.typography.titleMedium.copy(color = KntWhite))
                    Text("${dto.pickupAddress}  →  ${dto.dropAddress}",
                        style = MaterialTheme.typography.bodySmall.copy(color = KntYellow), maxLines = 1)
                }
            }

            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // Status banner
                val (bannerColor, bannerLabel) = statusBanner(currentStatus)
                Surface(
                    shape  = RoundedCornerShape(14.dp),
                    color  = bannerColor.copy(0.12f),
                    border = BorderStroke(1.dp, bannerColor.copy(0.4f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(bannerColor))
                        Spacer(Modifier.width(10.dp))
                        Text(bannerLabel,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = bannerColor)
                        Spacer(Modifier.weight(1f))
                        TripStatusDtoChip(currentStatus)
                    }
                }

                // Trip info
                SectionHeader(title = "Trip Details")
                Surface(
                    shape  = RoundedCornerShape(14.dp),
                    color  = c.surface2,
                    border = BorderStroke(1.dp, c.borderColor),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        InfoRow(Icons.Rounded.LocationOn,   "Pickup",     dto.pickupAddress)
                        InfoRow(Icons.Rounded.Flag,         "Drop-off",   dto.dropAddress)
                        InfoRow(Icons.Rounded.CalendarMonth,"Date",       dto.date)
                        InfoRow(Icons.Rounded.Schedule,     "Time",       dto.time)
                        InfoRow(Icons.Rounded.Person,       "Passengers", dto.passengers.toString())
                        if (dto.notes.isNotBlank()) InfoRow(Icons.Rounded.Notes, "Notes", dto.notes)
                    }
                }

                // Commuter info
                SectionHeader(title = "Commuter")
                Surface(
                    shape  = RoundedCornerShape(14.dp),
                    color  = c.surface2,
                    border = BorderStroke(1.dp, c.borderColor),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(42.dp).clip(CircleShape)
                                .background(Brush.linearGradient(listOf(KntBlue.copy(0.6f), KntBlueBright.copy(0.4f)))),
                            Alignment.Center,
                        ) {
                            Text(
                                (dto.commuterName ?: "?").split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(dto.commuterName ?: "—", style = MaterialTheme.typography.titleSmall, color = c.textBright)
                            if (dto.commuterPhone != null)
                                Text(dto.commuterPhone, style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                        }
                    }
                }

                // Quote card
                SectionHeader(title = "Quote")
                Surface(
                    shape  = RoundedCornerShape(14.dp),
                    color  = c.surface2,
                    border = BorderStroke(1.dp, c.yellow.copy(0.3f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Payments, null, tint = c.yellow, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Quoted Amount", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                            Text(
                                if (dto.quotedAmount != null) "R${String.format("%.2f", dto.quotedAmount)}" else "Not quoted yet",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (dto.quotedAmount != null) c.yellow else c.textDim,
                                ),
                            )
                        }
                        if (currentStatus !in listOf("COMPLETED", "CANCELLED")) {
                            IconButton(onClick = { showQuoteSheet = true }) {
                                Icon(Icons.Rounded.Edit, null, tint = c.blue, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                // Driver card
                SectionHeader(title = "Assigned Driver")
                Surface(
                    shape  = RoundedCornerShape(14.dp),
                    color  = c.surface2,
                    border = BorderStroke(1.dp, c.borderColor),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Person, null, tint = c.blue, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(dto.driverName ?: "No driver assigned",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (dto.driverName != null) c.textBright else c.textDim)
                            if (dto.vehiclePlate != null)
                                Text("${dto.vehicleInfo ?: ""} · ${dto.vehiclePlate}",
                                    style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                        }
                        if (currentStatus !in listOf("COMPLETED", "CANCELLED", "IN_PROGRESS")) {
                            IconButton(onClick = { showDriverSheet = true }) {
                                Icon(Icons.Rounded.Edit, null, tint = c.blue, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                // Admin actions
                if (currentStatus !in listOf("COMPLETED", "CANCELLED")) {
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = { showCancel = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = StatusRed.copy(0.12f),
                            contentColor   = StatusRed,
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                    ) {
                        Icon(Icons.Rounded.Cancel, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Cancel Trip", style = MaterialTheme.typography.labelLarge)
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

private fun statusBanner(status: String): Pair<androidx.compose.ui.graphics.Color, String> = when (status) {
    "IN_PROGRESS"    -> KntBlue    to "Trip In Progress"
    "CONFIRMED",
    "QUOTE_ACCEPTED" -> KntYellow  to "Confirmed"
    "COMPLETED"      -> StatusGreen to "Completed"
    "CANCELLED"      -> StatusRed  to "Cancelled"
    "QUOTE_SENT"     -> KntOrange  to "Awaiting Commuter Response"
    else             -> KntMuted   to status.replace("_", " ")
}

// ── Admin quote update sheet ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminQuoteSheet(
    current   : Double?,
    onDismiss : () -> Unit,
    onConfirm : (Double) -> Unit,
) {
    val c      = LocalAppColors.current
    var amount by remember { mutableStateOf(current?.let { "%.2f".format(it) } ?: "") }
    val valid  = amount.toDoubleOrNull()?.let { it > 0 } == true

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = c.surface1,
        dragHandle = {
            Box(Modifier.padding(vertical = 10.dp).width(36.dp).height(4.dp)
                .clip(RoundedCornerShape(2.dp)).background(c.borderColor))
        },
    ) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
            Text("Update Quote", style = MaterialTheme.typography.titleMedium, color = c.textBright)
            Spacer(Modifier.height(16.dp))
            KntTextField(
                value         = amount,
                onValueChange = { amount = it },
                label         = "Amount (R)",
                leadingIcon   = Icons.Rounded.Payments,
                keyboardType  = androidx.compose.ui.text.input.KeyboardType.Decimal,
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick  = { amount.toDoubleOrNull()?.let { onConfirm(it) } },
                enabled  = valid,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = c.yellow, contentColor = Color.Black),
            ) {
                Text("Save Quote", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel", color = c.textMuted)
            }
        }
    }
}
