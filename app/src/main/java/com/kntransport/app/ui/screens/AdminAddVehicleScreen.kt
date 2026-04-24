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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kntransport.app.R
import com.kntransport.app.network.ApiResult
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*
import com.kntransport.app.viewmodel.AdminViewModel

private val vehicleTypes = listOf("MINIBUS", "BUS", "SUV", "SEDAN")

@Composable
fun AdminAddVehicleScreen(
    onBack    : () -> Unit,
    onAdded   : () -> Unit,
    viewModel : AdminViewModel = viewModel(),
) {
    val c           = LocalAppColors.current
    val actionState by viewModel.vehicleActionState.collectAsState()

    var make         by remember { mutableStateOf("") }
    var model        by remember { mutableStateOf("") }
    var colour       by remember { mutableStateOf("") }
    var plate        by remember { mutableStateOf("") }
    var year         by remember { mutableStateOf("") }
    var notes        by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("MINIBUS") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isValid   = make.isNotBlank() && model.isNotBlank() && colour.isNotBlank() &&
                    plate.isNotBlank() && year.toIntOrNull() != null
    val isLoading = actionState is ApiResult.Loading

    LaunchedEffect(actionState) {
        when (val s = actionState) {
            is ApiResult.Success -> { viewModel.resetVehicleActionState(); onAdded() }
            is ApiResult.Error   -> { errorMessage = s.message; viewModel.resetVehicleActionState() }
            else -> {}
        }
    }

    val snackbarState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        errorMessage?.let { snackbarState.showSnackbar(it); errorMessage = null }
    }

    if (actionState is ApiResult.Success) {
        VehicleAddedSuccess(make = make, model = model, colour = colour, plate = plate, onDone = onAdded)
        return
    }

    KntScaffold(title = "Add Vehicle", onBack = onBack, snackbarHost = { SnackbarHost(snackbarState) }) { pv ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pv).verticalScroll(rememberScrollState()),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(110.dp)
                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            ) {
                HeroBgImage(resId = R.drawable.hero_bg_4, modifier = Modifier.fillMaxSize(), darkOverlay = 0.52f)
                Column(Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 12.dp)) {
                    Text(
                        "Add a vehicle to the fleet",
                        style = MaterialTheme.typography.labelMedium.copy(color = KntYellow, letterSpacing = 0.5.sp),
                    )
                }
            }

            Column(Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(20.dp))
                SectionHeader(title = "Vehicle Details")

                KntTextField(value = make, onValueChange = { make = it },
                    label = "Make (e.g. Toyota)", leadingIcon = Icons.Rounded.DirectionsBus)
                Spacer(Modifier.height(12.dp))

                KntTextField(value = model, onValueChange = { model = it },
                    label = "Model (e.g. HiAce)", leadingIcon = Icons.Rounded.DirectionsBus)
                Spacer(Modifier.height(12.dp))

                KntTextField(value = colour, onValueChange = { colour = it },
                    label = "Colour (e.g. White)", leadingIcon = Icons.Rounded.Palette)
                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KntTextField(value = plate, onValueChange = { plate = it.uppercase() },
                        label = "Number Plate", leadingIcon = Icons.Rounded.ConfirmationNumber,
                        modifier = Modifier.weight(1.5f))
                    KntTextField(value = year, onValueChange = { year = it },
                        label = "Year", leadingIcon = Icons.Rounded.CalendarMonth,
                        modifier = Modifier.weight(1f))
                }

                Spacer(Modifier.height(20.dp))
                SectionHeader(title = "Vehicle Type")
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    vehicleTypes.forEach { type ->
                        val selected = type == selectedType
                        Surface(
                            onClick  = { selectedType = type },
                            shape    = RoundedCornerShape(10.dp),
                            color    = if (selected) c.blue else c.surface2,
                            border   = BorderStroke(1.dp, if (selected) c.blue else c.borderColor),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                type.lowercase().replaceFirstChar { it.uppercase() },
                                style    = MaterialTheme.typography.labelSmall.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal),
                                color    = if (selected) Color.White else c.textMuted,
                                modifier = Modifier.padding(vertical = 10.dp).wrapContentWidth(Alignment.CenterHorizontally),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                KntTextField(value = notes, onValueChange = { notes = it },
                    label = "Notes (optional)", leadingIcon = Icons.Rounded.Notes,
                    singleLine = false, maxLines = 3)

                Spacer(Modifier.height(28.dp))

                KntPrimaryButton(
                    text    = if (isLoading) "Adding..." else "Add to Fleet",
                    onClick = {
                        viewModel.createVehicle(make, model, colour, plate.uppercase(),
                            year.toInt(), selectedType, notes)
                    },
                    enabled = isValid && !isLoading,
                    icon    = Icons.Rounded.DirectionsBus,
                )
                Spacer(Modifier.height(8.dp))
                KntSecondaryButton(text = "Cancel", onClick = onBack)
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun VehicleAddedSuccess(
    make  : String,
    model : String,
    colour: String,
    plate : String,
    onDone: () -> Unit,
) {
    val c = LocalAppColors.current
    Box(
        modifier = Modifier.fillMaxSize().background(c.bgDeep),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Box(
                modifier = Modifier.size(80.dp).clip(CircleShape).background(StatusGreen.copy(0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.CheckCircle, null, tint = StatusGreen, modifier = Modifier.size(44.dp))
            }
            Text("Vehicle Added!", style = MaterialTheme.typography.headlineSmall, color = c.textBright)
            Surface(shape = RoundedCornerShape(12.dp), color = c.surface2) {
                Row(
                    Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(Icons.Rounded.DirectionsBus, null, tint = KntYellow, modifier = Modifier.size(20.dp))
                    Column {
                        Text("$colour $make $model", style = MaterialTheme.typography.titleSmall, color = c.textBright)
                        Text(plate, style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                    }
                }
            }
            Text(
                "The vehicle has been added to the fleet and is ready to be assigned to a driver.",
                style = MaterialTheme.typography.bodyMedium, color = c.textMuted,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(8.dp))
            KntPrimaryButton(text = "Back to Fleet", onClick = onDone, icon = Icons.Rounded.DirectionsBus)
        }
    }
}
