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
import androidx.compose.ui.unit.*
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*

private val DAYS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

@Composable
fun CreateLiftClubScreen(
    onBack     : () -> Unit,
    onSubmitted: () -> Unit,
) {
    val c = LocalAppColors.current

    var title        by remember { mutableStateOf("") }
    var pickupArea   by remember { mutableStateOf("") }
    var dropArea     by remember { mutableStateOf("") }
    var departureTime by remember { mutableStateOf("") }
    var returnTime   by remember { mutableStateOf("") }
    var maxPassengers by remember { mutableIntStateOf(4) }
    var description  by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(setOf("Mon", "Tue", "Wed", "Thu", "Fri")) }
    var submitted    by remember { mutableStateOf(false) }

    val isValid = title.isNotBlank() && pickupArea.isNotBlank() && dropArea.isNotBlank() && departureTime.isNotBlank()

    if (submitted) {
        LiftClubSubmittedSuccess(onDone = onSubmitted)
        return
    }

    KntScaffold(title = "Create Lift Club", onBack = onBack) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(20.dp))

            // Info banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = c.yellow.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, c.yellow.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Lightbulb, null, tint = c.yellow, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Post your lift club request. Once enough commuters subscribe, K&T will confirm and provide a quote for approval.",
                        style = MaterialTheme.typography.bodySmall, color = c.textMuted,
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            SectionHeader(title = "Lift Club Details")

            KntTextField(value = title, onValueChange = { title = it },
                label = "Title (e.g. Beacon Valley → CBD)",
                leadingIcon = Icons.Rounded.Groups)
            Spacer(Modifier.height(12.dp))

            KntTextField(value = pickupArea, onValueChange = { pickupArea = it },
                label = "Pickup Area",
                leadingIcon = Icons.Rounded.LocationOn)
            Spacer(Modifier.height(12.dp))

            KntTextField(value = dropArea, onValueChange = { dropArea = it },
                label = "Drop-off Area",
                leadingIcon = Icons.Rounded.Flag)
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KntTextField(value = departureTime, onValueChange = { departureTime = it },
                    label = "Departs (e.g. 06:30)",
                    leadingIcon = Icons.Rounded.Schedule,
                    modifier = Modifier.weight(1f))
                KntTextField(value = returnTime, onValueChange = { returnTime = it },
                    label = "Returns (optional)",
                    leadingIcon = Icons.Rounded.Schedule,
                    modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(16.dp))
            SectionHeader(title = "Days of Travel")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                DAYS.forEach { day ->
                    val selected = day in selectedDays
                    Surface(
                        shape    = RoundedCornerShape(8.dp),
                        color    = if (selected) c.blue else c.surface2,
                        border   = BorderStroke(1.dp, if (selected) c.blue else c.borderColor),
                        modifier = Modifier.weight(1f).clickable {
                            selectedDays = if (selected) selectedDays - day else selectedDays + day
                        },
                    ) {
                        Text(
                            text     = day,
                            style    = MaterialTheme.typography.labelSmall,
                            color    = if (selected) androidx.compose.ui.graphics.Color.White else c.textMuted,
                            modifier = Modifier.padding(vertical = 8.dp).wrapContentWidth(Alignment.CenterHorizontally),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionHeader(title = "Max Passengers")

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(c.surface2)
                    .border(1.dp, c.borderColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Icon(Icons.Rounded.Group, null, tint = c.textMuted, modifier = Modifier.size(18.dp))
                Text("Max passengers", style = MaterialTheme.typography.bodyMedium, color = c.textBright, modifier = Modifier.weight(1f))
                IconButton(onClick = { if (maxPassengers > 2) maxPassengers-- }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Rounded.Remove, null, tint = if (maxPassengers > 2) c.blue else c.textDim)
                }
                Text(maxPassengers.toString(), style = MaterialTheme.typography.titleMedium.copy(color = c.textBright))
                IconButton(onClick = { if (maxPassengers < 20) maxPassengers++ }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Rounded.Add, null, tint = c.blue)
                }
            }

            Spacer(Modifier.height(12.dp))

            KntTextField(value = description, onValueChange = { description = it },
                label = "Description / additional details",
                leadingIcon = Icons.Rounded.Notes,
                singleLine = false, maxLines = 4)

            Spacer(Modifier.height(24.dp))

            KntPrimaryButton(
                text    = "Post Lift Club Request",
                onClick = { submitted = true },
                enabled = isValid,
                icon    = Icons.Rounded.Send,
            )
            Spacer(Modifier.height(8.dp))
            KntSecondaryButton(text = "Cancel", onClick = onBack)
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LiftClubSubmittedSuccess(onDone: () -> Unit) {
    val c = LocalAppColors.current
    Box(Modifier.fillMaxSize().background(c.bgDeep), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Surface(shape = androidx.compose.foundation.shape.CircleShape, color = c.yellow.copy(alpha = 0.15f)) {
                Icon(Icons.Rounded.CheckCircle, null, tint = c.yellow,
                    modifier = Modifier.size(80.dp).padding(18.dp))
            }
            Text("Lift Club Posted!", style = MaterialTheme.typography.headlineMedium, color = c.textBright)
            Text(
                "Your lift club request is now live. K&T Transport will confirm and send a quote once the passenger quota is met.",
                style = MaterialTheme.typography.bodyMedium, color = c.textMuted,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(8.dp))
            KntPrimaryButton(text = "Browse Lift Clubs", onClick = onDone, icon = Icons.Rounded.Groups)
        }
    }
}
