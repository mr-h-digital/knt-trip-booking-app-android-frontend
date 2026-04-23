package com.kntransport.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.kntransport.app.R
import com.kntransport.app.data.SampleData
import com.kntransport.app.data.Vehicle
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*

@Composable
fun AdminFleetScreen(
    onBack         : () -> Unit,
    onVehicleDetail: (String) -> Unit,
    onAddVehicle   : () -> Unit,
) {
    val c    = LocalAppColors.current
    val tabs = listOf("Active", "All")
    var selectedTab by remember { mutableIntStateOf(0) }

    val filtered = when (selectedTab) {
        0    -> SampleData.vehicles.filter { it.active }
        else -> SampleData.vehicles
    }

    KntScaffold(
        title   = "Fleet",
        onBack  = onBack,
        actions = {
            IconButton(onClick = onAddVehicle) {
                Icon(Icons.Rounded.Add, "Add Vehicle", tint = KntWhite)
            }
        },
    ) { pv ->
        Column(Modifier.fillMaxSize().padding(pv)) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            ) {
                HeroBgImage(resId = R.drawable.hero_bg_3, modifier = Modifier.fillMaxSize(), darkOverlay = 0.55f)
                Column(Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 12.dp)) {
                    Text(
                        "Manage your fleet",
                        style = MaterialTheme.typography.labelMedium.copy(color = KntYellow, letterSpacing = 0.5.sp),
                    )
                }
            }

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor   = c.surface1,
                contentColor     = c.blue,
                indicator        = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color    = c.yellow,
                    )
                },
                divider = {},
            ) {
                tabs.forEachIndexed { i, label ->
                    Tab(
                        selected = i == selectedTab,
                        onClick  = { selectedTab = i },
                        text     = { Text(label, style = MaterialTheme.typography.labelMedium) },
                        selectedContentColor   = c.yellow,
                        unselectedContentColor = c.textMuted,
                    )
                }
            }

            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Rounded.DirectionsBus, null, tint = c.textDim, modifier = Modifier.size(52.dp))
                        Text("No vehicles found", style = MaterialTheme.typography.bodyMedium, color = c.textMuted)
                        KntPrimaryButton(text = "Add Vehicle", onClick = onAddVehicle, icon = Icons.Rounded.Add,
                            modifier = Modifier.fillMaxWidth(0.6f))
                    }
                }
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    filtered.forEachIndexed { idx, vehicle ->
                        StaggeredItem(index = idx) {
                            VehicleCard(vehicle = vehicle, onClick = { onVehicleDetail(vehicle.id) })
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun VehicleCard(vehicle: Vehicle, onClick: () -> Unit) {
    val c = LocalAppColors.current
    val assignedDriver = SampleData.vehicles.find { it.id == vehicle.id }
        ?.assignedDriverId?.let { dId ->
            adminSampleUsers.find { it.id == dId }
        }

    KntCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                    .background(c.blue.copy(0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    when (vehicle.vehicleType.name) {
                        "SEDAN", "SUV" -> Icons.Rounded.DirectionsCar
                        else           -> Icons.Rounded.DirectionsBus
                    },
                    null, tint = c.blue, modifier = Modifier.size(24.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "${vehicle.make} ${vehicle.model}",
                    style = MaterialTheme.typography.titleSmall,
                    color = c.textBright,
                )
                Text(
                    "${vehicle.colour} · ${vehicle.plate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textMuted,
                )
                Text(
                    "${vehicle.vehicleType.name.lowercase().replaceFirstChar { it.uppercase() }} · ${vehicle.year}",
                    style = MaterialTheme.typography.labelSmall,
                    color = c.textDim,
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                if (assignedDriver != null) {
                    Surface(shape = RoundedCornerShape(8.dp), color = StatusGreen.copy(0.12f)) {
                        Text(
                            assignedDriver.name.split(" ").first(),
                            style    = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color    = StatusGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                } else {
                    Surface(shape = RoundedCornerShape(8.dp), color = KntOrange.copy(0.12f)) {
                        Text(
                            "Unassigned",
                            style    = MaterialTheme.typography.labelSmall,
                            color    = KntOrange,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        )
                    }
                }
                Icon(Icons.Rounded.ChevronRight, null, tint = c.textDim, modifier = Modifier.size(18.dp))
            }
        }
    }
}
