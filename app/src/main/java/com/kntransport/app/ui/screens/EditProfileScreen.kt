package com.kntransport.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kntransport.app.data.SampleData
import com.kntransport.app.network.ApiResult
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*
import com.kntransport.app.viewmodel.UserViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack    : () -> Unit,
    onSaved   : () -> Unit,
    viewModel : UserViewModel = viewModel(),
) {
    val c           = LocalAppColors.current
    val context     = LocalContext.current
    val user        = SampleData.currentUser
    val updateState by viewModel.updateState.collectAsState()

    var name  by remember { mutableStateOf(user.name) }
    var email by remember { mutableStateOf(user.email) }
    var phone by remember { mutableStateOf(user.phone) }
    var pendingAvatarUri by remember { mutableStateOf(user.avatarUri) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isLoading = updateState is ApiResult.Loading

    LaunchedEffect(updateState) {
        when (val s = updateState) {
            is ApiResult.Success -> {
                // Persist name locally so other screens see it instantly
                SampleData.currentUser = SampleData.currentUser.copy(
                    name      = s.data.name,
                    email     = s.data.email,
                    phone     = s.data.phone,
                    avatarUri = pendingAvatarUri,
                )
                viewModel.resetUpdateState()
                onSaved()
            }
            is ApiResult.Error -> {
                errorMessage = s.message
                viewModel.resetUpdateState()
            }
            else -> {}
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        errorMessage?.let { snackbarHostState.showSnackbar(it); errorMessage = null }
    }

    var showDiscardDialog    by remember { mutableStateOf(false) }
    var showPhotoSheet       by remember { mutableStateOf(false) }

    // Temp file URI for camera capture
    val cameraUri: Uri = remember {
        val dir  = File(context.cacheDir, "camera").also { it.mkdirs() }
        val file = File(dir, "profile_photo.jpg")
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    // Gallery / photo picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) pendingAvatarUri = uri }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success) pendingAvatarUri = cameraUri }

    // Camera permission launcher
    val cameraPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) cameraLauncher.launch(cameraUri) }

    val isDirty = name != user.name || email != user.email
        || phone != user.phone || pendingAvatarUri != user.avatarUri
    val canSave = name.isNotBlank() && email.isNotBlank() && phone.isNotBlank() && !isLoading

    val handleBack = { if (isDirty) showDiscardDialog = true else onBack() }

    val doSave = {
        if (pendingAvatarUri != null && pendingAvatarUri != user.avatarUri) {
            viewModel.uploadAvatar(context, pendingAvatarUri!!)
        } else {
            viewModel.updateProfile(name.trim(), email.trim(), phone.trim())
        }
    }

    // ── Discard dialog ────────────────────────────────────────────────────────
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            containerColor   = c.surface1,
            icon  = { Icon(Icons.Rounded.WarningAmber, null, tint = c.yellow, modifier = Modifier.size(28.dp)) },
            title = { Text("Discard Changes?", style = MaterialTheme.typography.headlineSmall, color = c.textBright) },
            text  = { Text("You have unsaved changes. Are you sure you want to go back?", style = MaterialTheme.typography.bodyMedium, color = c.textMuted) },
            confirmButton = {
                Button(
                    onClick = { showDiscardDialog = false; onBack() },
                    colors  = ButtonDefaults.buttonColors(containerColor = StatusRed, contentColor = androidx.compose.ui.graphics.Color.White),
                    shape   = RoundedCornerShape(10.dp),
                ) { Text("Discard", style = MaterialTheme.typography.labelLarge) }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDiscardDialog = false },
                    shape   = RoundedCornerShape(10.dp),
                    border  = BorderStroke(1.dp, c.borderColor),
                    colors  = ButtonDefaults.outlinedButtonColors(contentColor = c.textMuted),
                ) { Text("Keep Editing", style = MaterialTheme.typography.labelLarge) }
            },
        )
    }

    // ── Photo picker bottom sheet ─────────────────────────────────────────────
    if (showPhotoSheet) {
        PhotoPickerSheet(
            onDismiss = { showPhotoSheet = false },
            onGallery = { galleryLauncher.launch("image/*") },
            onCamera  = {
                val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
                if (hasPerm) cameraLauncher.launch(cameraUri)
                else cameraPermLauncher.launch(Manifest.permission.CAMERA)
            },
            onRemove  = if (pendingAvatarUri != null) ({ pendingAvatarUri = null }) else null,
        )
    }

    KntScaffold(
        title        = "Edit Profile",
        onBack       = handleBack,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        actions = {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier  = Modifier.size(20.dp).padding(end = 8.dp),
                    strokeWidth = 2.dp,
                    color     = c.yellow,
                )
            }
            TextButton(onClick = doSave, enabled = canSave && isDirty) {
                Text(
                    "Save",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color      = if (canSave && isDirty) c.yellow else c.textDim,
                    ),
                )
            }
        },
    ) { pv ->
        Column(
            modifier = Modifier.fillMaxSize().padding(pv).verticalScroll(rememberScrollState()),
        ) {
            // ── Avatar header ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(c.headerEnd, c.bgDeep)))
                    .padding(28.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Avatar with camera overlay badge
                    Box(contentAlignment = Alignment.BottomEnd) {
                        UserAvatar(
                            name      = name.ifBlank { user.name },
                            avatarUri = pendingAvatarUri,
                            size      = 88.dp,
                            onClick   = { showPhotoSheet = true },
                        )
                        // Camera badge
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(c.blue)
                                .border(2.dp, c.bgDeep, CircleShape)
                                .clickable { showPhotoSheet = true },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.CameraAlt, null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(15.dp))
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Tap photo to change",
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textDim,
                    )
                }
            }

            Column(Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(16.dp))
                SectionHeader(title = "Personal Details")

                KntTextField(value = name,  onValueChange = { name = it },  label = "Full Name",     leadingIcon = Icons.Rounded.Person)
                Spacer(Modifier.height(12.dp))
                KntTextField(value = email, onValueChange = { email = it }, label = "Email Address", leadingIcon = Icons.Rounded.Email)
                Spacer(Modifier.height(12.dp))
                KntTextField(value = phone, onValueChange = { phone = it }, label = "Phone Number",  leadingIcon = Icons.Rounded.Phone)

                Spacer(Modifier.height(24.dp))
                SectionHeader(title = "Account")
                KntCard {
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Badge, null, tint = c.textDim, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Role", style = MaterialTheme.typography.labelMedium, color = c.textMuted)
                            Text(user.role.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodyMedium, color = c.textDim)
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = c.surface2) {
                            Text("Assigned by admin", style = MaterialTheme.typography.labelSmall, color = c.textDim,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))
                KntPrimaryButton(text = "Save Changes", onClick = doSave, enabled = canSave && isDirty, icon = Icons.Rounded.Check)
                Spacer(Modifier.height(12.dp))
                KntSecondaryButton(text = "Cancel", onClick = handleBack)
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
