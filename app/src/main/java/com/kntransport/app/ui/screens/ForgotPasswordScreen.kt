package com.kntransport.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*

@Composable
fun ForgotPasswordScreen(onBack: () -> Unit) {
    val c = LocalAppColors.current
    var email     by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }

    val emailValid = isValidEmail(email)

    Box(Modifier.fillMaxSize().background(KntBlack)) {
        HeroBgImage(resId = com.kntransport.app.R.drawable.hero_bg_3, modifier = Modifier.fillMaxSize(), darkOverlay = 0.75f)
        Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(KntBlue.copy(0.15f), Color.Transparent), radius = 900f)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Rounded.ArrowBackIosNew, null, tint = KntWhite, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.weight(0.4f))

            if (!submitted) {
                // Icon
                Box(
                    Modifier.size(72.dp).clip(RoundedCornerShape(20.dp))
                        .background(KntBlue.copy(0.18f))
                        .border(1.5.dp, KntBlue.copy(0.4f), RoundedCornerShape(20.dp)),
                    Alignment.Center,
                ) {
                    Icon(Icons.Rounded.LockReset, null, tint = KntBlueBright, modifier = Modifier.size(36.dp))
                }
                Spacer(Modifier.height(24.dp))
                Text("Reset Password", style = MaterialTheme.typography.displaySmall.copy(color = KntWhite, fontWeight = FontWeight.ExtraBold))
                Spacer(Modifier.height(8.dp))
                Box(Modifier.width(48.dp).height(3.dp).clip(RoundedCornerShape(2.dp)).background(Brush.horizontalGradient(listOf(KntYellow, KntYellowDim))))
                Spacer(Modifier.height(10.dp))
                Text(
                    "Enter the email address linked to your account. We'll send you a reset link.",
                    style    = MaterialTheme.typography.bodyMedium.copy(color = KntMuted),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(32.dp))

                KntTextField(
                    value         = email,
                    onValueChange = { email = it },
                    label         = "Email Address",
                    leadingIcon   = Icons.Rounded.Email,
                    keyboardType  = KeyboardType.Email,
                    isError       = email.isNotBlank() && !emailValid,
                    supportingText = if (email.isNotBlank() && !emailValid) "Enter a valid email address" else null,
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick  = { if (emailValid) submitted = true },
                    enabled  = emailValid,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = KntBlue, contentColor = Color.White),
                ) {
                    Icon(Icons.Rounded.Send, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Send Reset Link", style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold))
                }
            } else {
                // Success state
                Box(
                    Modifier.size(72.dp).clip(RoundedCornerShape(20.dp))
                        .background(StatusGreen.copy(0.15f))
                        .border(1.5.dp, StatusGreen.copy(0.4f), RoundedCornerShape(20.dp)),
                    Alignment.Center,
                ) {
                    Icon(Icons.Rounded.MarkEmailRead, null, tint = StatusGreen, modifier = Modifier.size(36.dp))
                }
                Spacer(Modifier.height(24.dp))
                GradientText(
                    text   = "Check Your Email",
                    style  = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                    colors = listOf(KntWhite, StatusGreen),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "We've sent a password reset link to\n$email",
                    style    = MaterialTheme.typography.bodyMedium.copy(color = KntMuted),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(32.dp))
                KntPrimaryButton(text = "Back to Login", onClick = onBack, icon = Icons.Rounded.ArrowBack)
            }

            Spacer(Modifier.weight(1f))
        }
    }
}
