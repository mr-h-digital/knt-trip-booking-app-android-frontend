package com.kntransport.app.ui.components

import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import com.kntransport.app.network.observeConnectivity
import androidx.compose.ui.unit.*
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.kntransport.app.R
import com.kntransport.app.data.TripStatus
import com.kntransport.app.data.LiftClubStatus
import com.kntransport.app.network.UserDto
import com.kntransport.app.network.VehicleDto
import com.kntransport.app.ui.theme.*

// ── Screen scaffold with KNT header ──────────────────────────────────────────

@Composable
fun KntScaffold(
    title        : String,
    onBack       : (() -> Unit)? = null,
    actions      : @Composable RowScope.() -> Unit = {},
    snackbarHost : @Composable () -> Unit = {},
    bottomBar    : @Composable () -> Unit = {},
    content      : @Composable (PaddingValues) -> Unit,
) {
    val c = LocalAppColors.current
    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost   = snackbarHost,
        bottomBar      = bottomBar,
        modifier = Modifier.background(
            Brush.linearGradient(
                listOf(c.bgGradientTop, c.bgGradientMid, c.bgGradientBottom),
                start = Offset(0f, 0f),
                end   = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
            )
        ),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(c.headerStart, c.headerEnd)))
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Rounded.ArrowBackIosNew, null, tint = c.headerText, modifier = Modifier.size(20.dp))
                        }
                    } else {
                        Spacer(Modifier.width(12.dp))
                    }
                    Text(
                        text  = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = c.headerText,
                        modifier = Modifier.weight(1f),
                    )
                    Row(content = actions)
                }
                Box(
                    Modifier.align(Alignment.BottomEnd)
                        .width(40.dp).height(2.dp)
                        .background(c.yellow)
                )
            }
        }
    ) { pv ->
        Column {
            NetworkBanner()
            content(pv)
        }
    }
}

// ── KNT Branded logo badge ────────────────────────────────────────────────────

@Composable
fun KntLogoBadge(modifier: Modifier = Modifier, size: Dp = 56.dp) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape((size.value * 0.22f).dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .border(
                BorderStroke(1.dp, KntYellow.copy(alpha = 0.55f)),
                RoundedCornerShape((size.value * 0.22f).dp),
            )
            .padding((size.value * 0.08f).dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter            = painterResource(R.drawable.logo),
            contentDescription = "K&T Transport",
            modifier           = Modifier.fillMaxSize(),
        )
    }
}

// ── User avatar — photo if set, else gradient initials ────────────────────────

@Composable
fun UserAvatar(
    name      : String,
    avatarUri : Uri?    = null,
    avatarUrl : String? = null,
    size      : Dp = 72.dp,
    modifier  : Modifier = Modifier,
    onClick   : (() -> Unit)? = null,
) {
    val c        = LocalAppColors.current
    val context  = LocalContext.current
    val initials = name.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString("")
    val borderColor = c.bgDeep
    val baseMod  = modifier
        .size(size)
        .border(4.dp, borderColor, CircleShape)
        .clip(CircleShape)
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)

    // Resolve the image source: local pick takes priority, then persisted URL.
    val imageData: Any? = when {
        avatarUri != null  -> avatarUri
        !avatarUrl.isNullOrBlank() -> {
            val baseUrl = com.kntransport.app.BuildConfig.ACTIVE_API_URL.trimEnd('/')
            if (avatarUrl.startsWith("http")) avatarUrl else "$baseUrl$avatarUrl"
        }
        else -> null
    }

    // null  = loading/unknown, true = loaded OK, false = failed
    var imageState by remember(imageData) { mutableStateOf<Boolean?>(null) }

    Box(baseMod, contentAlignment = Alignment.Center) {
        if (imageData != null) {
            AsyncImage(
                model              = ImageRequest.Builder(context).data(imageData).build(),
                contentDescription = name,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize(),
                onSuccess          = {
                    android.util.Log.d("UserAvatar", "Loaded OK: $imageData")
                    imageState = true
                },
                onError            = { err ->
                    android.util.Log.e("UserAvatar", "Failed: $imageData — ${err.result.throwable}")
                    imageState = false
                },
            )
        }
        // Show initials while loading (null) or on failure (false); hide on success (true)
        if (imageData == null || imageState != true) {
            Box(
                Modifier.fillMaxSize()
                    .background(Brush.linearGradient(listOf(c.blue, c.yellow.copy(0.75f)))),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text  = initials,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color      = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = (size.value * 0.34f).sp,
                    ),
                )
            }
        }
    }
}

// ── Cancel trip bottom sheet ──────────────────────────────────────────────────

val COMMUTER_CANCEL_REASONS = listOf(
    "Change of plans",
    "Found alternative transport",
    "Booked by mistake",
    "Pick-up time no longer suitable",
    "Other",
)

val DRIVER_CANCEL_REASONS = listOf(
    "Vehicle breakdown",
    "Unable to reach pickup location",
    "Medical emergency",
    "Trip route not feasible",
    "Other",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CancelTripSheet(
    reasons   : List<String>,
    onDismiss : () -> Unit,
    onConfirm : (reason: String, note: String) -> Unit,
) {
    val c = LocalAppColors.current
    var selected by remember { mutableStateOf<String?>(null) }
    var note     by remember { mutableStateOf("") }
    val isOther  = selected == "Other"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = c.surface1,
        dragHandle = {
            Box(
                Modifier.padding(vertical = 10.dp).width(36.dp).height(4.dp)
                    .clip(RoundedCornerShape(2.dp)).background(c.borderColor)
            )
        },
    ) {
        Column(
            Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                        .background(StatusRed.copy(0.12f)),
                    Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Cancel, null, tint = StatusRed, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    "Cancel Trip",
                    style = MaterialTheme.typography.titleMedium,
                    color = c.textBright,
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "Please select a reason for cancelling.",
                style = MaterialTheme.typography.bodySmall,
                color = c.textMuted,
            )
            Spacer(Modifier.height(16.dp))

            reasons.forEach { reason ->
                val sel = reason == selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (sel) StatusRed.copy(0.08f)
                            else     c.surface2.copy(0.6f)
                        )
                        .border(
                            1.dp,
                            if (sel) StatusRed.copy(0.45f) else c.borderColor,
                            RoundedCornerShape(12.dp),
                        )
                        .clickable { selected = reason }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = sel,
                        onClick  = { selected = reason },
                        colors   = RadioButtonDefaults.colors(
                            selectedColor   = StatusRed,
                            unselectedColor = c.textDim,
                        ),
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        reason,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (sel) c.textBright else c.textMuted,
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            AnimatedVisibility(
                visible = isOther,
                enter   = expandVertically(tween(220)) + fadeIn(tween(220)),
                exit    = shrinkVertically(tween(180)) + fadeOut(tween(180)),
            ) {
                Column {
                    OutlinedTextField(
                        value         = note,
                        onValueChange = { note = it },
                        label         = { Text("Additional details (optional)", style = MaterialTheme.typography.bodySmall) },
                        placeholder   = { Text("Describe your reason…", style = MaterialTheme.typography.bodySmall) },
                        minLines      = 3,
                        maxLines      = 5,
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(12.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor      = StatusRed.copy(0.6f),
                            unfocusedBorderColor    = c.borderColor,
                            focusedLabelColor       = StatusRed,
                            unfocusedLabelColor     = c.textMuted,
                            focusedTextColor        = c.textBright,
                            unfocusedTextColor      = c.textBright,
                            cursorColor             = StatusRed,
                            focusedContainerColor   = c.surface2,
                            unfocusedContainerColor = c.surface2,
                        ),
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }

            Button(
                onClick  = { selected?.let { onConfirm(it, note.trim()) } },
                enabled  = selected != null,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = StatusRed,
                    contentColor           = Color.White,
                    disabledContainerColor = StatusRed.copy(0.3f),
                    disabledContentColor   = Color.White.copy(0.5f),
                ),
            ) {
                Icon(Icons.Rounded.Cancel, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Confirm Cancellation", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Keep Trip", style = MaterialTheme.typography.labelLarge, color = c.textMuted)
            }
        }
    }
}

// ── Driver picker sheet (used from Vehicle Detail to assign a driver) ─────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverPickerSheet(
    drivers      : List<UserDto>,
    isLoading    : Boolean,
    currentId    : String?,           // pre-selected / already assigned driver id
    onSelect     : (UserDto) -> Unit,
    onUnassign   : (() -> Unit)?,     // null when no driver is currently assigned
    onDismiss    : () -> Unit,
) {
    val c = LocalAppColors.current
    var query by remember { mutableStateOf("") }
    val filtered = drivers.filter {
        query.isBlank() || it.name.contains(query, ignoreCase = true) ||
        it.email.contains(query, ignoreCase = true)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = c.surface1,
        dragHandle = {
            Box(Modifier.padding(vertical = 10.dp).width(36.dp).height(4.dp)
                .clip(RoundedCornerShape(2.dp)).background(c.borderColor))
        },
    ) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                    .background(KntYellow.copy(0.12f)), Alignment.Center) {
                    Icon(Icons.Rounded.Person, null, tint = KntYellow, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text("Assign Driver", style = MaterialTheme.typography.titleMedium, color = c.textBright)
            }
            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value         = query,
                onValueChange = { query = it },
                placeholder   = { Text("Search drivers…", style = MaterialTheme.typography.bodySmall) },
                leadingIcon   = { Icon(Icons.Rounded.Search, null, tint = c.textMuted, modifier = Modifier.size(18.dp)) },
                trailingIcon  = if (query.isNotEmpty()) {{
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Rounded.Close, null, tint = c.textMuted, modifier = Modifier.size(16.dp))
                    }
                }} else null,
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = c.blue,
                    unfocusedBorderColor    = c.borderColor,
                    focusedTextColor        = c.textBright,
                    unfocusedTextColor      = c.textBright,
                    cursorColor             = c.blue,
                    focusedContainerColor   = c.surface2,
                    unfocusedContainerColor = c.surface2,
                ),
            )
            Spacer(Modifier.height(12.dp))

            when {
                isLoading -> Box(Modifier.fillMaxWidth().height(120.dp), Alignment.Center) {
                    CircularProgressIndicator()
                }
                filtered.isEmpty() -> Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), Alignment.Center) {
                    Text("No drivers found", style = MaterialTheme.typography.bodyMedium, color = c.textMuted)
                }
                else -> Column(
                    Modifier.verticalScroll(rememberScrollState()).heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    filtered.forEach { driver ->
                        val isSelected = driver.id == currentId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) KntYellow.copy(0.08f) else c.surface2.copy(0.6f))
                                .border(1.dp, if (isSelected) KntYellow.copy(0.45f) else c.borderColor, RoundedCornerShape(12.dp))
                                .clickable { onSelect(driver); onDismiss() }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val initials = driver.name.split(" ")
                                .mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
                            Box(
                                Modifier.size(40.dp).clip(CircleShape)
                                    .background(KntYellow.copy(if (isSelected) 0.3f else 0.12f)),
                                Alignment.Center,
                            ) {
                                Text(initials, style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold), color = KntYellow)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(driver.name, style = MaterialTheme.typography.titleSmall, color = c.textBright)
                                Text(driver.email, style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                            }
                            if (isSelected) {
                                Icon(Icons.Rounded.CheckCircle, null, tint = KntYellow, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            if (onUnassign != null) {
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick  = { onUnassign(); onDismiss() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape    = RoundedCornerShape(12.dp),
                    border   = BorderStroke(1.dp, StatusRed.copy(0.5f)),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                ) {
                    Icon(Icons.Rounded.PersonOff, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Remove assigned driver", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(Modifier.height(4.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge, color = c.textMuted)
            }
        }
    }
}

// ── Vehicle picker sheet (used from User Detail to assign a vehicle) ──────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiclePickerSheet(
    vehicles     : List<VehicleDto>,
    isLoading    : Boolean,
    currentId    : String?,           // pre-selected / already assigned vehicle id
    onSelect     : (VehicleDto) -> Unit,
    onUnassign   : (() -> Unit)?,
    onDismiss    : () -> Unit,
) {
    val c = LocalAppColors.current
    var query by remember { mutableStateOf("") }
    val filtered = vehicles.filter {
        query.isBlank() ||
        it.make.contains(query, ignoreCase = true) ||
        it.model.contains(query, ignoreCase = true) ||
        it.plate.contains(query, ignoreCase = true)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = c.surface1,
        dragHandle = {
            Box(Modifier.padding(vertical = 10.dp).width(36.dp).height(4.dp)
                .clip(RoundedCornerShape(2.dp)).background(c.borderColor))
        },
    ) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                    .background(KntBlue.copy(0.12f)), Alignment.Center) {
                    Icon(Icons.Rounded.DirectionsBus, null, tint = KntBlue, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text("Assign Vehicle", style = MaterialTheme.typography.titleMedium, color = c.textBright)
            }
            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value         = query,
                onValueChange = { query = it },
                placeholder   = { Text("Search by make, model or plate…", style = MaterialTheme.typography.bodySmall) },
                leadingIcon   = { Icon(Icons.Rounded.Search, null, tint = c.textMuted, modifier = Modifier.size(18.dp)) },
                trailingIcon  = if (query.isNotEmpty()) {{
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Rounded.Close, null, tint = c.textMuted, modifier = Modifier.size(16.dp))
                    }
                }} else null,
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = c.blue,
                    unfocusedBorderColor    = c.borderColor,
                    focusedTextColor        = c.textBright,
                    unfocusedTextColor      = c.textBright,
                    cursorColor             = c.blue,
                    focusedContainerColor   = c.surface2,
                    unfocusedContainerColor = c.surface2,
                ),
            )
            Spacer(Modifier.height(12.dp))

            when {
                isLoading -> Box(Modifier.fillMaxWidth().height(120.dp), Alignment.Center) {
                    CircularProgressIndicator()
                }
                filtered.isEmpty() -> Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), Alignment.Center) {
                    Text("No active vehicles found", style = MaterialTheme.typography.bodyMedium, color = c.textMuted)
                }
                else -> Column(
                    Modifier.verticalScroll(rememberScrollState()).heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    filtered.forEach { vehicle ->
                        val isSelected = vehicle.id == currentId
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) KntBlue.copy(0.08f) else c.surface2.copy(0.6f))
                                .border(1.dp, if (isSelected) KntBlue.copy(0.45f) else c.borderColor, RoundedCornerShape(12.dp))
                                .clickable { onSelect(vehicle); onDismiss() }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            VehiclePhotoAvatar(
                                photoUrl = vehicle.photoUrl,
                                size     = 40.dp,
                                shape    = RoundedCornerShape(10.dp),
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "${vehicle.colour} ${vehicle.make} ${vehicle.model}",
                                    style = MaterialTheme.typography.titleSmall, color = c.textBright,
                                )
                                Text(
                                    "${vehicle.plate} · ${vehicle.vehicleType.lowercase().replaceFirstChar { it.uppercase() }}",
                                    style = MaterialTheme.typography.bodySmall, color = c.textMuted,
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Rounded.CheckCircle, null, tint = KntBlue, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            if (onUnassign != null) {
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick  = { onUnassign(); onDismiss() },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape    = RoundedCornerShape(12.dp),
                    border   = BorderStroke(1.dp, StatusRed.copy(0.5f)),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                ) {
                    Icon(Icons.Rounded.DirectionsBus, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Remove assigned vehicle", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(Modifier.height(4.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge, color = c.textMuted)
            }
        }
    }
}

// ── Vehicle photo avatar — photo if URL set, else icon placeholder ────────────

@Composable
fun VehiclePhotoAvatar(
    photoUrl : String?,
    size     : Dp = 56.dp,
    modifier : Modifier = Modifier,
    shape    : Shape = RoundedCornerShape((size.value * 0.22f).dp),
) {
    val c = LocalAppColors.current
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(c.yellow.copy(0.12f))
            .border(1.dp, Brush.linearGradient(listOf(KntYellow.copy(0.5f), KntBlueBright.copy(0.3f))), shape),
        contentAlignment = Alignment.Center,
    ) {
        var vehiclePhotoFailed by remember(photoUrl) { mutableStateOf(false) }
        if (!photoUrl.isNullOrBlank() && !vehiclePhotoFailed) {
            AsyncImage(
                model              = ImageRequest.Builder(LocalContext.current).data(photoUrl).build(),
                contentDescription = "Vehicle photo",
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize(),
                onError            = { vehiclePhotoFailed = true },
            )
        } else {
            Icon(Icons.Rounded.DirectionsBus, null, tint = c.yellow, modifier = Modifier.size(size * 0.48f))
        }
    }
}

// ── Photo picker bottom sheet ─────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoPickerSheet(
    onDismiss   : () -> Unit,
    onGallery   : () -> Unit,
    onCamera    : () -> Unit,
    onRemove    : (() -> Unit)? = null,
    title       : String = "Update Profile Photo",
) {
    val c = LocalAppColors.current
    ModalBottomSheet(
        onDismissRequest  = onDismiss,
        containerColor    = c.surface1,
        dragHandle        = {
            Box(
                Modifier.padding(vertical = 10.dp).width(36.dp).height(4.dp)
                    .clip(RoundedCornerShape(2.dp)).background(c.borderColor)
            )
        },
    ) {
        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
            Text(
                title,
                style    = MaterialTheme.typography.titleMedium,
                color    = c.textBright,
                modifier = Modifier.padding(bottom = 20.dp),
            )
            PhotoOption(Icons.Rounded.PhotoLibrary, "Choose from Gallery",  "Browse photos & files",  c.blue)   { onGallery(); onDismiss() }
            Spacer(Modifier.height(10.dp))
            PhotoOption(Icons.Rounded.CameraAlt,    "Take a Photo",         "Use your camera",         c.yellow) { onCamera(); onDismiss() }
            if (onRemove != null) {
                Spacer(Modifier.height(10.dp))
                PhotoOption(Icons.Rounded.Delete, "Remove Photo", "Revert to initials", StatusRed) { onRemove(); onDismiss() }
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel", style = MaterialTheme.typography.labelLarge, color = c.textMuted)
            }
        }
    }
}

@Composable
private fun PhotoOption(
    icon    : ImageVector,
    title   : String,
    subtitle: String,
    tint    : Color,
    onClick : () -> Unit,
) {
    val c = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(c.surface2.copy(0.9f), c.surface2.copy(0.6f))))
            .border(1.dp, Brush.linearGradient(listOf(tint.copy(0.25f), Color.White.copy(0.05f))), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(tint.copy(0.14f)),
            Alignment.Center,
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title,    style = MaterialTheme.typography.titleSmall, color = c.textBright)
            Text(subtitle, style = MaterialTheme.typography.bodySmall,  color = c.textMuted)
        }
        Spacer(Modifier.weight(1f))
        Icon(Icons.Rounded.ChevronRight, null, tint = c.textDim, modifier = Modifier.size(18.dp))
    }
}

// ── Bottom nav tab definition ─────────────────────────────────────────────────

enum class KntNavTab(val label: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    HOME      ("Home",       Icons.Rounded.Home,          Icons.Rounded.Home),
    TRIPS     ("My Trips",   Icons.Rounded.DirectionsBus, Icons.Rounded.DirectionsBus),
    LIFT_CLUBS("Lift Clubs", Icons.Rounded.Groups,        Icons.Rounded.Groups),
    PROFILE   ("Profile",    Icons.Rounded.Person,        Icons.Rounded.Person),
}

enum class DriverNavTab(val label: String, val icon: ImageVector) {
    HOME        ("Home",        Icons.Rounded.Home),
    TRIPS       ("Trips",       Icons.Rounded.DirectionsBus),
    EARNINGS    ("Earnings",    Icons.Rounded.Payments),
    PROFILE     ("Profile",     Icons.Rounded.Person),
}

enum class AdminNavTab(val label: String, val icon: ImageVector) {
    DASHBOARD   ("Dashboard",   Icons.Rounded.Home),
    USERS       ("Users",       Icons.Rounded.People),
    FLEET       ("Fleet",       Icons.Rounded.DirectionsBus),
    ANALYTICS   ("Analytics",   Icons.Rounded.BarChart),
    PROFILE     ("Profile",     Icons.Rounded.Person),
}

// ── Generic role bottom nav (Driver / Admin) ──────────────────────────────────

data class NavTabItem(val label: String, val icon: ImageVector)

@Composable
fun RoleBottomNav(
    tabs     : List<NavTabItem>,
    selected : Int,
    onSelect : (Int) -> Unit,
    modifier : Modifier = Modifier,
) {
    val c = LocalAppColors.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(c.navBackground),
    ) {
        Box(
            Modifier.fillMaxWidth().height(1.dp).background(
                Brush.horizontalGradient(listOf(
                    Color.Transparent, c.yellow.copy(0.5f), c.blue.copy(0.7f), Color.Transparent,
                ))
            )
        )

        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth().padding(top = 1.dp),
        ) {
            val tabWidth  = maxWidth / tabs.size
            val pillWidth = tabWidth * 0.74f
            val pillX by animateDpAsState(
                targetValue   = tabWidth * selected + (tabWidth - pillWidth) / 2,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                label         = "roleNavPill",
            )
            Box(
                modifier = Modifier
                    .padding(vertical = 5.dp)
                    .offset(x = pillX)
                    .width(pillWidth).height(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(c.yellow.copy(0.14f), c.blue.copy(0.07f))))
            )
            val haptics = LocalHapticFeedback.current
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                tabs.forEachIndexed { i, tab ->
                    val sel   = i == selected
                    val tint  by animateColorAsState(
                        targetValue   = if (sel) c.yellow else c.textMuted,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label         = "roleNavTint$i",
                    )
                    val scale by animateFloatAsState(
                        targetValue   = if (sel) 1.10f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                        label         = "roleNavScale$i",
                    )
                    Column(
                        modifier = Modifier
                            .scale(scale)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSelect(i)
                            }
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Icon(tab.icon, tab.label, tint = tint, modifier = Modifier.size(22.dp))
                        Text(tab.label, style = MaterialTheme.typography.labelSmall, color = tint)
                    }
                }
            }
        }
    }
}

// ── Bottom nav with animated sliding pill ─────────────────────────────────────

@Composable
fun KntBottomNav(
    selected : KntNavTab,
    onSelect : (KntNavTab) -> Unit,
    modifier : Modifier = Modifier,
) {
    val c    = LocalAppColors.current
    val tabs = KntNavTab.entries

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(c.navBackground)
    ) {
        // Shimmer top border
        Box(
            Modifier.fillMaxWidth().height(1.dp).background(
                Brush.horizontalGradient(listOf(
                    Color.Transparent, c.blue.copy(alpha = 0.7f),
                    c.yellow.copy(alpha = 0.5f), Color.Transparent,
                ))
            )
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 1.dp),
        ) {
            val tabCount  = tabs.size
            val tabWidth  = maxWidth / tabCount
            val pillWidth = tabWidth * 0.74f
            val selIdx    = tabs.indexOf(selected)

            // Animated pill
            val pillX by animateDpAsState(
                targetValue   = tabWidth * selIdx + (tabWidth - pillWidth) / 2,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness    = Spring.StiffnessMedium,
                ),
                label = "pillX",
            )
            Box(
                modifier = Modifier
                    .padding(vertical = 5.dp)
                    .offset(x = pillX)
                    .width(pillWidth)
                    .height(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(c.yellow.copy(0.14f), c.blue.copy(0.07f))
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                tabs.forEach { tab ->
                    KntNavItem(tab = tab, selected = tab == selected, onClick = { onSelect(tab) })
                }
            }
        }
    }
}

@Composable
private fun KntNavItem(tab: KntNavTab, selected: Boolean, onClick: () -> Unit) {
    val c       = LocalAppColors.current
    val haptics = LocalHapticFeedback.current
    val tint by animateColorAsState(
        targetValue   = if (selected) c.yellow else c.textMuted,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label         = "navTint",
    )
    val scale by animateFloatAsState(
        targetValue   = if (selected) 1.10f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label         = "navScale",
    )
    Column(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .clickable {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(tab.selectedIcon, tab.label, tint = tint, modifier = Modifier.size(22.dp))
        Text(tab.label, style = MaterialTheme.typography.labelSmall, color = tint)
    }
}

// ── Glassmorphism card with press-scale micro-interaction ─────────────────────

@Composable
fun KntCard(
    modifier  : Modifier = Modifier,
    onClick   : (() -> Unit)? = null,
    content   : @Composable ColumnScope.() -> Unit,
) {
    val c       = LocalAppColors.current
    val haptics = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isPressed && onClick != null) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label         = "cardScale",
    )

    val clickMod = if (onClick != null) Modifier.clickable(
        interactionSource = interactionSource,
        indication        = null,
    ) {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        onClick()
    } else Modifier

    Column(
        modifier = modifier
            .scale(scale)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (c.logoBg == Color.Transparent) // dark mode sentinel
                    Brush.linearGradient(listOf(c.surface1.copy(0.93f), c.surface1.copy(0.78f)))
                else
                    Brush.linearGradient(listOf(Color.White.copy(0.96f), c.surface1.copy(0.80f)))
            )
            .border(
                BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.13f),
                            c.blue.copy(alpha = 0.20f),
                            Color.White.copy(alpha = 0.06f),
                        )
                    )
                ),
                RoundedCornerShape(16.dp),
            )
            .then(clickMod)
            .padding(16.dp),
        content = content,
    )
}

// ── Status chips ──────────────────────────────────────────────────────────────

@Composable
fun TripStatusChip(status: TripStatus) {
    val (label, bg, fg) = when (status) {
        TripStatus.PENDING_QUOTE  -> Triple("Awaiting Quote",  Color(0xFF1A3A5C),  KntMuted)
        TripStatus.QUOTE_SENT     -> Triple("Quote Ready",     Color(0xFF3A2A00),  KntYellow)
        TripStatus.QUOTE_ACCEPTED -> Triple("Accepted",        Color(0xFF1A3A1A),  StatusGreen)
        TripStatus.CONFIRMED      -> Triple("Confirmed",       Color(0xFF1A3A1A),  StatusGreen)
        TripStatus.IN_PROGRESS    -> Triple("On the Way",      Color(0xFF0D2040),  KntBlueBright)
        TripStatus.COMPLETED      -> Triple("Completed",       Color(0xFF1A2A1A),  Color(0xFF7BC47B))
        TripStatus.CANCELLED      -> Triple("Cancelled",       Color(0xFF3A1A1A),  StatusRed)
    }
    Surface(shape = RoundedCornerShape(20.dp), color = bg, modifier = Modifier.wrapContentSize()) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}

@Composable
fun LiftClubStatusChip(status: LiftClubStatus) {
    val (label, bg, fg) = when (status) {
        LiftClubStatus.OPEN       -> Triple("Open",        Color(0xFF0D3A1A),  StatusGreen)
        LiftClubStatus.QUOTA_MET  -> Triple("Quota Met",   Color(0xFF3A2A00),  KntYellow)
        LiftClubStatus.QUOTE_SENT -> Triple("Quote Sent",  Color(0xFF3A2800),  KntOrange)
        LiftClubStatus.ACTIVE     -> Triple("Active",      Color(0xFF0D2040),  KntBlueBright)
        LiftClubStatus.COMPLETED  -> Triple("Completed",   Color(0xFF1A2A1A),  Color(0xFF7BC47B))
        LiftClubStatus.CANCELLED  -> Triple("Cancelled",   Color(0xFF3A1A1A),  StatusRed)
    }
    Surface(shape = RoundedCornerShape(20.dp), color = bg, modifier = Modifier.wrapContentSize()) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}

// ── Info row ─────────────────────────────────────────────────────────────────

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String, iconTint: Color = KntMuted) {
    val c = LocalAppColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    ) {
        Icon(icon, null, tint = iconTint, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = c.textMuted, modifier = Modifier.width(90.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = c.textBright, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

// ── Section header ────────────────────────────────────────────────────────────

@Composable
fun SectionHeader(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    val c = LocalAppColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
    ) {
        Box(Modifier.width(3.dp).height(18.dp).clip(RoundedCornerShape(2.dp)).background(c.yellow))
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = c.textOnBg, modifier = Modifier.weight(1f))
        if (action != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(action, style = MaterialTheme.typography.labelMedium, color = c.blue)
            }
        }
    }
}

// ── Buttons with press-scale micro-interaction ────────────────────────────────

@Composable
fun KntPrimaryButton(
    text     : String,
    onClick  : () -> Unit,
    modifier : Modifier = Modifier,
    enabled  : Boolean  = true,
    icon     : ImageVector? = null,
) {
    val c       = LocalAppColors.current
    val haptics = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isPressed && enabled) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label         = "btnScale",
    )
    Button(
        onClick  = { haptics.performHapticFeedback(HapticFeedbackType.LongPress); onClick() },
        enabled  = enabled,
        modifier = modifier.scale(scale).fillMaxWidth().height(52.dp),
        shape    = RoundedCornerShape(14.dp),
        colors   = ButtonDefaults.buttonColors(containerColor = c.blue, contentColor = Color.White),
        interactionSource = interactionSource,
    ) {
        if (icon != null) { Icon(icon, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)) }
        Text(text, style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp))
    }
}

@Composable
fun KntSecondaryButton(
    text    : String,
    onClick : () -> Unit,
    modifier: Modifier = Modifier,
    icon    : ImageVector? = null,
) {
    val c       = LocalAppColors.current
    val haptics = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label         = "secBtnScale",
    )
    OutlinedButton(
        onClick  = { haptics.performHapticFeedback(HapticFeedbackType.LongPress); onClick() },
        modifier = modifier.scale(scale).fillMaxWidth().height(52.dp),
        shape    = RoundedCornerShape(14.dp),
        border   = BorderStroke(1.5.dp, c.yellow),
        colors   = ButtonDefaults.outlinedButtonColors(contentColor = c.yellow),
        interactionSource = interactionSource,
    ) {
        if (icon != null) { Icon(icon, null, tint = c.yellow, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)) }
        Text(text, style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp))
    }
}

// ── Input field ───────────────────────────────────────────────────────────────

@Composable
fun KntTextField(
    value        : String,
    onValueChange: (String) -> Unit,
    label        : String,
    modifier     : Modifier = Modifier,
    leadingIcon  : ImageVector? = null,
    singleLine   : Boolean = true,
    readOnly     : Boolean = false,
    maxLines     : Int = 1,
    keyboardType : KeyboardType = KeyboardType.Text,
    isError      : Boolean = false,
    supportingText: String? = null,
) {
    val c = LocalAppColors.current
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label          = { Text(label, style = MaterialTheme.typography.bodySmall) },
        leadingIcon    = if (leadingIcon != null) { { Icon(leadingIcon, null, tint = c.textMuted, modifier = Modifier.size(18.dp)) } } else null,
        singleLine     = singleLine,
        readOnly       = readOnly,
        maxLines       = maxLines,
        isError        = isError,
        supportingText = if (supportingText != null) { { Text(supportingText, style = MaterialTheme.typography.bodySmall) } } else null,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
        modifier       = modifier.fillMaxWidth(),
        shape          = RoundedCornerShape(12.dp),
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = c.blue,
            unfocusedBorderColor    = c.borderColor,
            focusedLabelColor       = c.blue,
            unfocusedLabelColor     = c.textMuted,
            focusedTextColor        = c.textBright,
            unfocusedTextColor      = c.textBright,
            cursorColor             = c.blue,
            focusedContainerColor   = c.surface2,
            unfocusedContainerColor = c.surface2,
        ),
    )
}

// ── Divider ───────────────────────────────────────────────────────────────────

@Composable
fun KntDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier.padding(vertical = 8.dp), color = LocalAppColors.current.borderColor)
}

// ── Hero background image with gradient overlay ───────────────────────────────

@Composable
fun HeroBgImage(
    resId          : Int,
    modifier       : Modifier = Modifier,
    darkOverlay    : Float = 0.55f,   // 0 = fully visible, 1 = fully dark
    contentScale   : androidx.compose.ui.layout.ContentScale = androidx.compose.ui.layout.ContentScale.Crop,
) {
    Box(modifier = modifier) {
        Image(
            painter      = painterResource(resId),
            contentDescription = null,
            contentScale = contentScale,
            modifier     = Modifier.matchParentSize(),
        )
        // Dark gradient scrim so text stays readable
        Box(
            Modifier.matchParentSize().background(
                Brush.verticalGradient(
                    listOf(
                        Color.Black.copy(alpha = darkOverlay * 0.6f),
                        Color.Black.copy(alpha = darkOverlay),
                    )
                )
            )
        )
    }
}

// ── Gradient text ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalTextApi::class)
@Composable
fun GradientText(
    text    : String,
    style   : androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier,
    colors  : List<Color> = listOf(KntBlueBright, KntYellow),
) {
    Text(
        text     = text,
        style    = style.copy(
            brush = Brush.linearGradient(
                colors = colors,
                start  = Offset(0f, 0f),
                end    = Offset(Float.POSITIVE_INFINITY, 0f),
            )
        ),
        modifier = modifier,
    )
}

// ── Staggered list item wrapper ───────────────────────────────────────────────

@Composable
fun StaggeredItem(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(index) {
        kotlinx.coroutines.delay(index * 70L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter   = slideInVertically(tween(340, easing = EaseOutCubic)) { it / 3 } + fadeIn(tween(340)),
    ) {
        content()
    }
}

// ── Shimmer loading placeholder ───────────────────────────────────────────────

@Composable
fun ShimmerBox(modifier: Modifier = Modifier, shape: Shape = RoundedCornerShape(8.dp)) {
    val c = LocalAppColors.current
    val transition = rememberInfiniteTransition(label = "shimmer")
    val offset by transition.animateFloat(
        initialValue  = -800f,
        targetValue   =  800f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing)),
        label         = "shimmerOffset",
    )
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(c.surface1, c.surface2, c.surface1),
                    start  = Offset(offset, 0f),
                    end    = Offset(offset + 600f, 300f),
                )
            )
    )
}

// ── Shimmer card skeleton (for loading states) ────────────────────────────────

@Composable
fun TripCardShimmer() {
    val c = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(c.surface1.copy(alpha = 0.9f))
            .border(BorderStroke(1.dp, Brush.linearGradient(listOf(Color.White.copy(0.10f), c.blue.copy(0.15f)))), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ShimmerBox(Modifier.size(40.dp), RoundedCornerShape(10.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ShimmerBox(Modifier.fillMaxWidth(0.65f).height(12.dp))
                ShimmerBox(Modifier.fillMaxWidth(0.45f).height(10.dp))
            }
            ShimmerBox(Modifier.width(72.dp).height(22.dp), RoundedCornerShape(20.dp))
        }
        ShimmerBox(Modifier.fillMaxWidth().height(1.dp))
        ShimmerBox(Modifier.fillMaxWidth(0.8f).height(10.dp))
        ShimmerBox(Modifier.fillMaxWidth(0.5f).height(10.dp))
    }
}

// ── Network connectivity banner ───────────────────────────────────────────────

@Composable
fun NetworkBanner() {
    val context = LocalContext.current
    val isOnline by observeConnectivity(context).collectAsState(initial = true)

    AnimatedVisibility(
        visible = !isOnline,
        enter   = slideInVertically { -it } + fadeIn(),
        exit    = slideOutVertically { -it } + fadeOut(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StatusRed.copy(alpha = 0.92f))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Rounded.WifiOff, null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "No internet connection",
                style = MaterialTheme.typography.labelMedium.copy(color = Color.White),
            )
        }
    }
}

// ── Error state with retry ────────────────────────────────────────────────────

@Composable
fun ErrorState(
    message  : String,
    onRetry  : (() -> Unit)? = null,
    modifier : Modifier = Modifier,
) {
    val c = LocalAppColors.current
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Rounded.ErrorOutline, null, tint = StatusRed, modifier = Modifier.size(48.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = c.textMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        if (onRetry != null) {
            KntSecondaryButton(text = "Retry", onClick = onRetry,
                icon = Icons.Rounded.Refresh, modifier = Modifier.fillMaxWidth(0.6f))
        }
    }
}

// ── Password strength indicator ───────────────────────────────────────────────

@Composable
fun PasswordStrengthIndicator(password: String) {
    val c = LocalAppColors.current
    val strength = when {
        password.length >= 12 &&
            password.any { it.isDigit() } &&
            password.any { it.isUpperCase() } &&
            password.any { !it.isLetterOrDigit() } -> 4
        password.length >= 8 &&
            password.any { it.isDigit() } &&
            password.any { it.isUpperCase() }       -> 3
        password.length >= 6 &&
            password.any { it.isDigit() }           -> 2
        password.isNotEmpty()                       -> 1
        else                                        -> 0
    }
    val label = when (strength) {
        4    -> "Strong"
        3    -> "Good"
        2    -> "Fair"
        1    -> "Weak"
        else -> ""
    }
    val color = when (strength) {
        4    -> StatusGreen
        3    -> KntBlueBright
        2    -> KntYellow
        1    -> StatusRed
        else -> Color.Transparent
    }

    if (password.isEmpty()) return

    Column(Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(4) { idx ->
                Box(
                    Modifier.weight(1f).height(3.dp).clip(RoundedCornerShape(2.dp))
                        .background(if (idx < strength) color else c.surface2)
                )
            }
        }
        Spacer(Modifier.height(3.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

// ── Validation helpers ────────────────────────────────────────────────────────

fun isValidEmail(email: String): Boolean =
    android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

fun isValidSAPhone(phone: String): Boolean {
    val cleaned = phone.replace(" ", "").replace("-", "")
    return cleaned.matches(Regex("^(\\+27|0)[6-8][0-9]{8}\$"))
}

fun formatRelativeTime(isoTimestamp: String): String {
    return try {
        val instant  = java.time.Instant.parse(isoTimestamp)
        val diffSecs = java.time.Instant.now().epochSecond - instant.epochSecond
        when {
            diffSecs < 60      -> "Just now"
            diffSecs < 3600    -> "${diffSecs / 60} min ago"
            diffSecs < 86400   -> "${diffSecs / 3600} hr ago"
            diffSecs < 172800  -> "Yesterday"
            diffSecs < 604800  -> "${diffSecs / 86400} days ago"
            else               -> java.time.format.DateTimeFormatter
                .ofPattern("d MMM")
                .withZone(java.time.ZoneId.systemDefault())
                .format(instant)
        }
    } catch (_: Exception) {
        isoTimestamp
    }
}

// ── Legal document helpers ────────────────────────────────────────────────────

@Composable
fun LegalLastUpdated(date: String) {
    val c = LocalAppColors.current
    Text(
        "Last updated: $date",
        style = MaterialTheme.typography.labelSmall,
        color = c.textDim,
    )
}

@Composable
fun LegalSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    val c = LocalAppColors.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color      = c.textBright,
            ),
        )
        content()
    }
}

@Composable
fun LegalSubSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    val c = LocalAppColors.current
    Column(
        modifier = Modifier.padding(start = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color      = c.textMuted,
            ),
        )
        content()
    }
}

@Composable
fun LegalBody(text: String) {
    val c = LocalAppColors.current
    Text(
        text,
        style = MaterialTheme.typography.bodySmall.copy(
            color      = c.textMuted,
            lineHeight = 20.sp,
        ),
    )
}

@Composable
fun LegalBullet(text: String) {
    val c = LocalAppColors.current
    Row(
        modifier = Modifier.padding(start = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("•", style = MaterialTheme.typography.bodySmall, color = c.yellow)
        Text(
            text,
            style = MaterialTheme.typography.bodySmall.copy(
                color      = c.textMuted,
                lineHeight = 20.sp,
            ),
        )
    }
}
