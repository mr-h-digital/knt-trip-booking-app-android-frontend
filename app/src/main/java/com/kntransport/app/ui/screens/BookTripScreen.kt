package com.kntransport.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kntransport.app.R
import com.kntransport.app.network.ApiResult
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*
import com.kntransport.app.viewmodel.TripViewModel

@Composable
fun BookTripScreen(
    onBack     : () -> Unit,
    onSubmitted: () -> Unit,
    viewModel  : TripViewModel = viewModel(),
) {
    val c           = LocalAppColors.current
    val createState by viewModel.createState.collectAsState()

    var pickup     by remember { mutableStateOf("") }
    var dropoff    by remember { mutableStateOf("") }
    var date       by remember { mutableStateOf("") }
    var time       by remember { mutableStateOf("") }
    var passengers by remember { mutableIntStateOf(1) }
    var notes      by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isLoading = createState is ApiResult.Loading
    val isValid   = pickup.isNotBlank() && dropoff.isNotBlank() && date.isNotBlank() && time.isNotBlank()

    LaunchedEffect(createState) {
        when (val s = createState) {
            is ApiResult.Success -> { viewModel.resetCreateState(); onSubmitted() }
            is ApiResult.Error   -> { errorMessage = s.message; viewModel.resetCreateState() }
            else -> {}
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        errorMessage?.let { snackbarHostState.showSnackbar(it); errorMessage = null }
    }

    KntScaffold(title = "Book a Trip", onBack = onBack, snackbarHost = { SnackbarHost(snackbarHostState) }) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv)
                .verticalScroll(rememberScrollState()),
        ) {
            // Hero banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            ) {
                HeroBgImage(resId = R.drawable.hero_bg_4, modifier = Modifier.fillMaxSize(), darkOverlay = 0.48f)
                Column(Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 14.dp)) {
                    Text("Where are you heading?", style = MaterialTheme.typography.titleMedium.copy(color = KntWhite))
                    Text("Book your trip in minutes", style = MaterialTheme.typography.bodySmall.copy(color = KntYellow))
                }
            }

            Column(Modifier.padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(20.dp))

            // Info banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = c.blue.copy(alpha = 0.10f),
                border = BorderStroke(1.dp, c.blue.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Info, null, tint = c.blue, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Submit your trip request. K&T will send you a quote — accept it to confirm your booking.",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textMuted,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            SectionHeader(title = "Trip Details")

            KntTextField(
                value = pickup, onValueChange = { pickup = it },
                label = "Pickup Address",
                leadingIcon = Icons.Rounded.LocationOn,
            )
            Spacer(Modifier.height(12.dp))

            KntTextField(
                value = dropoff, onValueChange = { dropoff = it },
                label = "Drop-off Address",
                leadingIcon = Icons.Rounded.LocationOn,
            )
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KntTextField(
                    value = date, onValueChange = { date = it },
                    label = "Date (e.g. 25 Apr)",
                    leadingIcon = Icons.Rounded.CalendarMonth,
                    modifier = Modifier.weight(1f),
                )
                KntTextField(
                    value = time, onValueChange = { time = it },
                    label = "Time (e.g. 07:30)",
                    leadingIcon = Icons.Rounded.Schedule,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(16.dp))
            SectionHeader(title = "Passengers")

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(c.surface2)
                    .border(1.dp, c.borderColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Icon(Icons.Rounded.Person, null, tint = c.textMuted, modifier = Modifier.size(18.dp))
                Text("Passengers", style = MaterialTheme.typography.bodyMedium, color = c.textBright, modifier = Modifier.weight(1f))
                IconButton(
                    onClick  = { if (passengers > 1) passengers-- },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(Icons.Rounded.Remove, null, tint = if (passengers > 1) c.blue else c.textDim)
                }
                Text(
                    passengers.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(color = c.textBright),
                    modifier = Modifier.width(24.dp),
                )
                IconButton(
                    onClick  = { if (passengers < 10) passengers++ },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(Icons.Rounded.Add, null, tint = c.blue)
                }
            }

            Spacer(Modifier.height(16.dp))

            KntTextField(
                value = notes, onValueChange = { notes = it },
                label = "Additional Notes (optional)",
                leadingIcon = Icons.Rounded.Notes,
                singleLine = false, maxLines = 3,
            )

            Spacer(Modifier.height(24.dp))

            KntPrimaryButton(
                text    = "Submit Trip Request",
                onClick = { viewModel.createTrip(pickup, dropoff, date, time, passengers, notes) },
                enabled = isValid,
                icon    = Icons.Rounded.Send,
            )

            Spacer(Modifier.height(8.dp))

            KntSecondaryButton(
                text    = "Cancel",
                onClick = onBack,
            )

            Spacer(Modifier.height(32.dp))
            } // close inner padding Column
        }
    }
}

