package com.kntransport.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kntransport.app.R
import com.kntransport.app.network.ApiResult
import com.kntransport.app.network.VehicleDto
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*
import com.kntransport.app.viewmodel.AdminViewModel
import java.io.File

private val editVehicleTypes = listOf("MINIBUS", "BUS", "SUV", "SEDAN")

@Composable
fun AdminEditVehicleScreen(
    vehicle   : VehicleDto,
    onBack    : () -> Unit,
    onSaved   : () -> Unit,
    viewModel : AdminViewModel = viewModel(),
) {
    val c           = LocalAppColors.current
    val actionState by viewModel.vehicleActionState.collectAsState()
    val context     = LocalContext.current

    var make             by remember { mutableStateOf(vehicle.make) }
    var model            by remember { mutableStateOf(vehicle.model) }
    var colour           by remember { mutableStateOf(vehicle.colour) }
    var plate            by remember { mutableStateOf(vehicle.plate) }
    var year             by remember { mutableStateOf(vehicle.year.toString()) }
    var notes            by remember { mutableStateOf(vehicle.notes) }
    var selectedType     by remember { mutableStateOf(vehicle.vehicleType) }
    var vehiclePhotoUri  by remember { mutableStateOf<Uri?>(null) }
    var showPhotoSheet   by remember { mutableStateOf(false) }
    var errorMessage     by remember { mutableStateOf<String?>(null) }

    val isLoading  = actionState is ApiResult.Loading
    val isValid    = make.isNotBlank() && model.isNotBlank() && colour.isNotBlank() &&
                     plate.isNotBlank() && year.toIntOrNull() != null
    val hasChanges = make != vehicle.make || model != vehicle.model ||
                     colour != vehicle.colour || plate != vehicle.plate ||
                     year != vehicle.year.toString() || notes != vehicle.notes ||
                     selectedType != vehicle.vehicleType || vehiclePhotoUri != null

    LaunchedEffect(actionState) {
        when (val s = actionState) {
            is ApiResult.Success -> { viewModel.resetVehicleActionState(); onSaved() }
            is ApiResult.Error   -> { errorMessage = s.message; viewModel.resetVehicleActionState() }
            else -> {}
        }
    }

    val snackbarState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        errorMessage?.let { snackbarState.showSnackbar(it); errorMessage = null }
    }

    val cameraUri: Uri = remember {
        val dir  = File(context.cacheDir, "camera").also { it.mkdirs() }
        val file = File(dir, "vehicle_photo.jpg")
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) vehiclePhotoUri = uri
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) vehiclePhotoUri = cameraUri
    }
    val cameraPermLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) cameraLauncher.launch(cameraUri)
    }

    if (showPhotoSheet) {
        PhotoPickerSheet(
            title     = "Update Vehicle Photo",
            onDismiss = { showPhotoSheet = false },
            onGallery = { galleryLauncher.launch("image/*") },
            onCamera  = {
                val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
                if (hasPerm) cameraLauncher.launch(cameraUri) else cameraPermLauncher.launch(Manifest.permission.CAMERA)
            },
            onRemove  = if (vehiclePhotoUri != null || vehicle.photoUrl != null) ({
                vehiclePhotoUri = null
            }) else null,
        )
    }

    if (actionState is ApiResult.Success) {
        VehicleUpdatedSuccess(make = make, model = model, colour = colour, plate = plate, onDone = onSaved)
        return
    }

    KntScaffold(title = "Edit Vehicle", onBack = onBack, snackbarHost = { SnackbarHost(snackbarState) }) { pv ->
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

                SectionHeader(title = "Vehicle Photo")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        VehiclePhotoAvatar(
                            photoUrl = vehiclePhotoUri?.toString() ?: vehicle.photoUrl,
                            size     = 80.dp,
                            shape    = RoundedCornerShape(14.dp),
                        )
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(c.blue)
                                .border(2.dp, c.bgDeep, CircleShape)
                                .clickable { showPhotoSheet = true },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.CameraAlt, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                    Column {
                        Text(
                            "Tap the camera icon or button to update",
                            style = MaterialTheme.typography.bodySmall,
                            color = c.textMuted,
                        )
                        Spacer(Modifier.height(6.dp))
                        Surface(
                            onClick = { showPhotoSheet = true },
                            shape   = RoundedCornerShape(10.dp),
                            color   = c.surface2,
                            border  = BorderStroke(1.dp, c.borderColor),
                        ) {
                            Row(
                                Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(Icons.Rounded.AddAPhoto, null, tint = c.blue, modifier = Modifier.size(16.dp))
                                Text("Change Photo", style = MaterialTheme.typography.labelMedium, color = c.blue)
                            }
                        }
                    }
                }
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
                    text    = if (isLoading) "Saving..." else "Save Changes",
                    onClick = {
                        viewModel.updateVehicle(vehicle.id, make, model, colour,
                            plate.uppercase(), year.toInt(), selectedType, notes)
                        if (vehiclePhotoUri != null) {
                            val file = run {
                                val input = context.contentResolver.openInputStream(vehiclePhotoUri!!) ?: return@run null
                                val f = File(context.cacheDir, "vehicle_upload.jpg")
                                java.io.FileOutputStream(f).use { out -> input.copyTo(out) }
                                f
                            }
                            if (file != null) viewModel.uploadVehiclePhoto(vehicle.id, file)
                        }
                    },
                    enabled = isValid && hasChanges && !isLoading,
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
            Text("Vehicle Updated!", style = MaterialTheme.typography.headlineSmall, color = c.textOnBg)
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
