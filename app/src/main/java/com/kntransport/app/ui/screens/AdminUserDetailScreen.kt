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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kntransport.app.R
import com.kntransport.app.network.ApiResult
import com.kntransport.app.network.UserDto
import com.kntransport.app.network.VehicleDto
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*
import com.kntransport.app.viewmodel.AdminViewModel

@Composable
fun AdminUserDetailScreen(
    user      : UserDto,
    onBack    : () -> Unit,
    onEdit    : () -> Unit = {},
    viewModel : AdminViewModel = viewModel(),
) {
    val c               = LocalAppColors.current
    val deleteState     by viewModel.deleteState.collectAsState()
    val assignState     by viewModel.assignState.collectAsState()
    val vehiclesState   by viewModel.vehicles.collectAsState()

    var showDialog       by remember { mutableStateOf(false) }
    var showVehiclePicker by remember { mutableStateOf(false) }
    var deleted          by remember { mutableStateOf(false) }
    var errorMessage     by remember { mutableStateOf<String?>(null) }

    // Track assignment state locally so UI updates without re-navigating
    var currentVehicleId     by remember { mutableStateOf(user.currentVehicleId) }
    var currentVehicleMake   by remember { mutableStateOf(user.currentVehicleMake) }
    var currentVehicleModel  by remember { mutableStateOf(user.currentVehicleModel) }
    var currentVehicleColour by remember { mutableStateOf(user.currentVehicleColour) }
    var currentVehiclePlate  by remember { mutableStateOf(user.currentVehiclePlate) }
    var currentVehicleType   by remember { mutableStateOf(user.currentVehicleType) }

    // Load active vehicles when screen opens (driver only)
    LaunchedEffect(Unit) {
        if (user.role.uppercase() == "DRIVER") viewModel.loadVehicles(activeOnly = true)
    }

    LaunchedEffect(assignState) {
        when (val s = assignState) {
            is ApiResult.Success -> {
                currentVehicleId     = s.data.currentVehicleId
                currentVehicleMake   = s.data.currentVehicleMake
                currentVehicleModel  = s.data.currentVehicleModel
                currentVehicleColour = s.data.currentVehicleColour
                currentVehiclePlate  = s.data.currentVehiclePlate
                currentVehicleType   = s.data.currentVehicleType
                viewModel.resetAssignState()
            }
            is ApiResult.Error -> { errorMessage = s.message; viewModel.resetAssignState() }
            else -> {}
        }
    }

    val (roleTint, roleLabel, roleIcon) = when (user.role.uppercase()) {
        "DRIVER" -> Triple(KntYellow,  "Driver",        Icons.Rounded.LocalShipping)
        "ADMIN"  -> Triple(KntOrange,  "Administrator", Icons.Rounded.AdminPanelSettings)
        else     -> Triple(KntBlue,    "Commuter",      Icons.Rounded.DirectionsBus)
    }

    val initials = user.name.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")

    LaunchedEffect(deleteState) {
        when (val s = deleteState) {
            is ApiResult.Success -> { viewModel.resetDeleteState(); deleted = true }
            is ApiResult.Error   -> { errorMessage = s.message; viewModel.resetDeleteState() }
            else -> {}
        }
    }

    val snackbarState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        errorMessage?.let { snackbarState.showSnackbar(it); errorMessage = null }
    }

    if (deleted) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    // Vehicle picker sheet
    if (showVehiclePicker) {
        val vehicles = (vehiclesState as? ApiResult.Success)?.data ?: emptyList()
        VehiclePickerSheet(
            vehicles  = vehicles,
            isLoading = vehiclesState is ApiResult.Loading,
            currentId = currentVehicleId,
            onSelect  = { vehicle ->
                viewModel.assignVehicle(user.id, vehicle.id)
            },
            onUnassign = if (currentVehicleId != null) ({
                viewModel.assignVehicle(user.id, null)
            }) else null,
            onDismiss = { showVehiclePicker = false },
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor   = c.surface2,
            icon = {
                Icon(Icons.Rounded.PersonOff, null, tint = StatusRed, modifier = Modifier.size(28.dp))
            },
            title = {
                Text("Delete Account?", style = MaterialTheme.typography.titleMedium, color = c.textBright)
            },
            text = {
                Text(
                    "Are you sure you want to delete ${user.name}'s account? This cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.textMuted,
                )
            },
            confirmButton = {
                Button(
                    onClick = { showDialog = false; viewModel.deleteUser(user.id) },
                    colors  = ButtonDefaults.buttonColors(containerColor = StatusRed, contentColor = Color.White),
                    shape   = RoundedCornerShape(10.dp),
                ) { Text("Delete") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDialog = false },
                    shape   = RoundedCornerShape(10.dp),
                    border  = BorderStroke(1.dp, c.borderColor),
                    colors  = ButtonDefaults.outlinedButtonColors(contentColor = c.textMuted),
                ) { Text("Cancel") }
            },
        )
    }

    KntScaffold(
        title        = "User Detail",
        onBack       = onBack,
        snackbarHost = { SnackbarHost(snackbarState) },
        actions      = {
            IconButton(onClick = onEdit) {
                Icon(Icons.Rounded.Edit, "Edit user", tint = KntWhite)
            }
        },
    ) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv)
                .verticalScroll(rememberScrollState()),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
                contentAlignment = Alignment.Center,
            ) {
                HeroBgImage(resId = R.drawable.hero_bg_2, modifier = Modifier.fillMaxSize(), darkOverlay = 0.62f)
                Box(Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, c.bgDeep.copy(0.7f))
                    )
                ))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(roleTint.copy(0.8f), roleTint.copy(0.4f)))
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            initials,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(user.name, style = MaterialTheme.typography.headlineSmall, color = c.textBright)
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = roleTint.copy(alpha = 0.15f),
                    ) {
                        Row(
                            Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Icon(roleIcon, null, tint = roleTint, modifier = Modifier.size(14.dp))
                            Text(roleLabel, style = MaterialTheme.typography.labelMedium, color = roleTint)
                        }
                    }
                }
            }

            Column(Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(8.dp))

                SectionHeader(title = "Account Details")
                KntCard {
                    UserDetailRow(Icons.Rounded.Badge,  "Full Name", user.name)
                    KntDivider()
                    UserDetailRow(Icons.Rounded.Email,  "Email",     user.email)
                    KntDivider()
                    UserDetailRow(Icons.Rounded.Phone,  "Phone",     user.phone)
                    KntDivider()
                    UserDetailRow(Icons.Rounded.Key,    "User ID",   user.id)
                }

                if (user.role.uppercase() == "DRIVER") {
                    Spacer(Modifier.height(16.dp))
                    SectionHeader(
                        title    = "Assigned Vehicle",
                        action   = if (currentVehicleId != null) "Change" else "Assign",
                        onAction = { showVehiclePicker = true },
                    )

                    val isAssigning = assignState is ApiResult.Loading
                    if (isAssigning) {
                        Box(Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    } else if (currentVehicleId != null) {
                        KntCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                VehiclePhotoAvatar(
                                    photoUrl = user.currentVehiclePhotoUrl,
                                    size     = 44.dp,
                                    shape    = RoundedCornerShape(10.dp),
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        "${currentVehicleColour ?: ""} ${currentVehicleMake ?: ""} ${currentVehicleModel ?: ""}".trim(),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = c.textBright,
                                    )
                                    Text(
                                        "${currentVehiclePlate ?: ""} · ${currentVehicleType?.lowercase()?.replaceFirstChar { it.uppercase() } ?: ""}".trim(' ', '·', ' '),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = c.textMuted,
                                    )
                                }
                                Surface(shape = RoundedCornerShape(8.dp), color = StatusGreen.copy(0.12f)) {
                                    Text("Assigned", style = MaterialTheme.typography.labelSmall, color = StatusGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }
                        }
                    } else {
                        Surface(
                            shape    = RoundedCornerShape(14.dp),
                            color    = c.surface2,
                            border   = BorderStroke(1.dp, KntOrange.copy(0.3f)),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.DirectionsBus, null, tint = c.textDim, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Text("No vehicle assigned", style = MaterialTheme.typography.bodyMedium, color = c.textMuted, modifier = Modifier.weight(1f))
                                TextButton(onClick = { showVehiclePicker = true }) {
                                    Text("Assign", style = MaterialTheme.typography.labelMedium, color = KntOrange)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                SectionHeader(title = "Actions")

                KntPrimaryButton(
                    text    = "Edit Profile",
                    onClick = onEdit,
                    icon    = Icons.Rounded.Edit,
                )

                if (user.role.uppercase() != "ADMIN") {
                    Spacer(Modifier.height(10.dp))
                    val isDeleting = deleteState is ApiResult.Loading
                    Button(
                        onClick   = { showDialog = true },
                        modifier  = Modifier.fillMaxWidth().height(52.dp),
                        shape     = RoundedCornerShape(14.dp),
                        colors    = ButtonDefaults.buttonColors(
                            containerColor = StatusRed.copy(alpha = 0.12f),
                            contentColor   = StatusRed,
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                        enabled   = !isDeleting,
                    ) {
                        if (isDeleting) {
                            CircularProgressIndicator(color = StatusRed, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Rounded.PersonOff, null, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("Delete Account", style = MaterialTheme.typography.labelLarge)
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun UserDetailRow(icon: ImageVector, label: String, value: String) {
    val c = LocalAppColors.current
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = c.blue, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = c.textMuted)
            Text(value, style = MaterialTheme.typography.bodyMedium, color = c.textBright)
        }
    }
}
