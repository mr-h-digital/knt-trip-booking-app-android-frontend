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
import com.kntransport.app.R
import com.kntransport.app.data.Vehicle
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*

private val editVehicleTypes = listOf("MINIBUS", "BUS", "SUV", "SEDAN")

@Composable
fun AdminEditVehicleScreen(
    vehicle : Vehicle,
    onBack  : () -> Unit,
    onSaved : () -> Unit,
) {
    val c = LocalAppColors.current

    var make         by remember { mutableStateOf(vehicle.make) }
    var model        by remember { mutableStateOf(vehicle.model) }
    var colour       by remember { mutableStateOf(vehicle.colour) }
    var plate        by remember { mutableStateOf(vehicle.plate) }
    var year         by remember { mutableStateOf(vehicle.year.toString()) }
    var notes        by remember { mutableStateOf(vehicle.notes) }
    var selectedType by remember { mutableStateOf(vehicle.vehicleType.name) }
    var saved        by remember { mutableStateOf(false) }

    val isValid   = make.isNotBlank() && model.isNotBlank() && colour.isNotBlank() &&
                    plate.isNotBlank() && year.toIntOrNull() != null
    val hasChanges = make != vehicle.make || model != vehicle.model ||
                     colour != vehicle.colour || plate != vehicle.plate ||
                     year != vehicle.year.toString() || notes != vehicle.notes ||
                     selectedType != vehicle.vehicleType.name

    if (saved) {
        VehicleUpdatedSuccess(
            make   = make,
            model  = model,
            colour = colour,
            plate  = plate,
            onDone = onSaved,
        )
        return
    }

    KntScaffold(title = "Edit Vehicle", onBack = onBack) { pv ->
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
                        "${vehicle.colour} ${vehicle.make} ${vehicle.model}  ·  ${vehicle.plate}",
                        style = MaterialTheme.typography.labelMedium.copy(color = KntYellow, letterSpacing = 0.5.sp),
                    )
                }
            }

            Column(Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(20.dp))
                SectionHeader(title = "Vehicle Details")

                KntTextField(value = make, onValueChange = { make = it },
                    label = "Make", leadingIcon = Icons.Rounded.DirectionsBus)
                Spacer(Modifier.height(12.dp))

                KntTextField(value = model, onValueChange = { model = it },
                    label = "Model", leadingIcon = Icons.Rounded.DirectionsBus)
                Spacer(Modifier.height(12.dp))

                KntTextField(value = colour, onValueChange = { colour = it },
                    label = "Colour", leadingIcon = Icons.Rounded.Palette)
                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KntTextField(
                        value         = plate,
                        onValueChange = { plate = it.uppercase() },
                        label         = "Number Plate",
                        leadingIcon   = Icons.Rounded.ConfirmationNumber,
                        modifier      = Modifier.weight(1.5f),
                    )
                    KntTextField(
                        value         = year,
                        onValueChange = { year = it },
                        label         = "Year",
                        leadingIcon   = Icons.Rounded.CalendarMonth,
                        modifier      = Modifier.weight(1f),
                    )
                }

                Spacer(Modifier.height(20.dp))
                SectionHeader(title = "Vehicle Type")
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    editVehicleTypes.forEach { type ->
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
                                style    = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal),
                                color    = if (selected) Color.White else c.textMuted,
                                modifier = Modifier.padding(vertical = 10.dp)
                                    .wrapContentWidth(Alignment.CenterHorizontally),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                KntTextField(
                    value         = notes,
                    onValueChange = { notes = it },
                    label         = "Notes (optional)",
                    leadingIcon   = Icons.Rounded.Notes,
                    singleLine    = false,
                    maxLines      = 3,
                )

                Spacer(Modifier.height(28.dp))

                KntPrimaryButton(
                    text    = "Save Changes",
                    onClick = { saved = true },
                    enabled = isValid && hasChanges,
                    icon    = Icons.Rounded.Check,
                )
                Spacer(Modifier.height(8.dp))
                KntSecondaryButton(text = "Cancel", onClick = onBack)
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun VehicleUpdatedSuccess(
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
                modifier = Modifier.size(80.dp).clip(CircleShape).background(c.blue.copy(0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Check, null, tint = c.blue, modifier = Modifier.size(44.dp))
            }
            Text("Vehicle Updated!", style = MaterialTheme.typography.headlineSmall, color = c.textBright)
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
                "The vehicle details have been updated successfully.",
                style    = MaterialTheme.typography.bodyMedium,
                color    = c.textMuted,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(8.dp))
            KntPrimaryButton(text = "Back to Vehicle", onClick = onDone, icon = Icons.Rounded.DirectionsBus)
        }
    }
}
