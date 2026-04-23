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
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.draw.clip
import com.kntransport.app.R
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*

@Composable
fun AdminCreateDriverScreen(
    onBack   : () -> Unit,
    onCreated: () -> Unit,
) {
    val c = LocalAppColors.current

    var name             by remember { mutableStateOf("") }
    var email            by remember { mutableStateOf("") }
    var phone            by remember { mutableStateOf("") }
    var password         by remember { mutableStateOf("") }
    var confirmPassword  by remember { mutableStateOf("") }
    var passwordVisible  by remember { mutableStateOf(false) }
    var confirmVisible   by remember { mutableStateOf(false) }
    var created          by remember { mutableStateOf(false) }

    val passwordsMatch = password.isNotBlank() && password == confirmPassword
    val isValid = name.isNotBlank() && isValidEmail(email) && phone.isNotBlank() &&
                  password.length >= 6 && passwordsMatch

    if (created) {
        DriverCreatedSuccess(driverName = name, onDone = onCreated)
        return
    }

    KntScaffold(title = "Add Driver", onBack = onBack) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv)
                .verticalScroll(rememberScrollState()),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
            ) {
                HeroBgImage(resId = R.drawable.hero_bg_4, modifier = Modifier.fillMaxSize(), darkOverlay = 0.52f)
                Column(Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 12.dp)) {
                    Text(
                        "Add a new driver to your team",
                        style = MaterialTheme.typography.labelMedium.copy(color = KntYellow, letterSpacing = 0.5.sp),
                    )
                }
            }

            Column(Modifier.padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(20.dp))

            Surface(
                shape    = RoundedCornerShape(12.dp),
                color    = c.orange.copy(alpha = 0.08f),
                border   = BorderStroke(1.dp, c.orange.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Info, null, tint = c.orange, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Driver accounts are created by admin only. The driver will receive login credentials via email.",
                        style = MaterialTheme.typography.bodySmall,
                        color = c.textMuted,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            SectionHeader(title = "Driver Details")

            KntTextField(value = name, onValueChange = { name = it },
                label = "Full Name", leadingIcon = Icons.Rounded.Person)
            Spacer(Modifier.height(12.dp))

            KntTextField(value = email, onValueChange = { email = it },
                label = "Email Address", leadingIcon = Icons.Rounded.Email)
            Spacer(Modifier.height(12.dp))

            KntTextField(value = phone, onValueChange = { phone = it },
                label = "Phone Number", leadingIcon = Icons.Rounded.Phone)
            Spacer(Modifier.height(12.dp))

            Surface(
                shape    = RoundedCornerShape(12.dp),
                color    = c.surface2,
                border   = BorderStroke(1.dp, c.yellow.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Lock, null, tint = c.yellow, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Role", style = MaterialTheme.typography.bodySmall, color = c.textMuted, modifier = Modifier.weight(1f))
                    Surface(shape = RoundedCornerShape(8.dp), color = KntYellow.copy(alpha = 0.12f)) {
                        Text("Driver", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = KntYellow, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            SectionHeader(title = "Set Password")

            OutlinedTextField(
                value         = password,
                onValueChange = { password = it },
                label         = { Text("Password", style = MaterialTheme.typography.bodySmall) },
                leadingIcon   = { Icon(Icons.Rounded.VpnKey, null, tint = c.textMuted, modifier = Modifier.size(18.dp)) },
                trailingIcon  = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                            null, tint = c.textMuted, modifier = Modifier.size(18.dp))
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp),
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

            if (password.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                PasswordStrengthIndicator(password = password)
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value         = confirmPassword,
                onValueChange = { confirmPassword = it },
                label         = { Text("Confirm Password", style = MaterialTheme.typography.bodySmall) },
                leadingIcon   = { Icon(Icons.Rounded.VpnKey, null, tint = c.textMuted, modifier = Modifier.size(18.dp)) },
                trailingIcon  = {
                    IconButton(onClick = { confirmVisible = !confirmVisible }) {
                        Icon(if (confirmVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                            null, tint = c.textMuted, modifier = Modifier.size(18.dp))
                    }
                },
                visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                shape         = RoundedCornerShape(12.dp),
                isError       = confirmPassword.isNotBlank() && !passwordsMatch,
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = c.blue,
                    unfocusedBorderColor    = c.borderColor,
                    errorBorderColor        = StatusRed,
                    focusedLabelColor       = c.blue,
                    unfocusedLabelColor     = c.textMuted,
                    focusedTextColor        = c.textBright,
                    unfocusedTextColor      = c.textBright,
                    cursorColor             = c.blue,
                    focusedContainerColor   = c.surface2,
                    unfocusedContainerColor = c.surface2,
                ),
            )
            if (confirmPassword.isNotBlank() && !passwordsMatch) {
                Text("Passwords do not match", style = MaterialTheme.typography.labelSmall, color = StatusRed,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp))
            }

            Spacer(Modifier.height(28.dp))

            KntPrimaryButton(
                text    = "Create Driver Account",
                onClick = { created = true },
                enabled = isValid,
                icon    = Icons.Rounded.PersonAdd,
            )
            Spacer(Modifier.height(8.dp))
            KntSecondaryButton(text = "Cancel", onClick = onBack)
            Spacer(Modifier.height(32.dp))
            } // close inner padding Column
        }
    }
}

@Composable
private fun DriverCreatedSuccess(driverName: String, onDone: () -> Unit) {
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
                modifier = Modifier.size(80.dp).clip(CircleShape)
                    .background(StatusGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.CheckCircle, null, tint = StatusGreen, modifier = Modifier.size(44.dp))
            }
            Text("Driver Account Created!", style = MaterialTheme.typography.headlineSmall, color = c.textBright)
            Surface(shape = RoundedCornerShape(12.dp), color = c.surface2) {
                Row(Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.LocalShipping, null, tint = KntYellow, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(driverName, style = MaterialTheme.typography.titleSmall, color = c.textBright)
                }
            }
            Text(
                "Login credentials have been sent to the driver's email address.",
                style = MaterialTheme.typography.bodyMedium,
                color = c.textMuted,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(8.dp))
            KntPrimaryButton(text = "Done", onClick = onDone, icon = Icons.Rounded.People)
        }
    }
}
