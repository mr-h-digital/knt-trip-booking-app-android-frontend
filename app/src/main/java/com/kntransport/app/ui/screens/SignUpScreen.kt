package com.kntransport.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import com.kntransport.app.R
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SignUpScreen(
    onSignedUp: () -> Unit,
    onLogin   : () -> Unit,
) {
    val c = LocalAppColors.current

    var fullName         by remember { mutableStateOf("") }
    var email            by remember { mutableStateOf("") }
    var phone            by remember { mutableStateOf("") }
    var password         by remember { mutableStateOf("") }
    var confirmPassword  by remember { mutableStateOf("") }
    var passwordVisible  by remember { mutableStateOf(false) }
    var confirmVisible   by remember { mutableStateOf(false) }
    var headerVisible    by remember { mutableStateOf(false) }
    var formVisible      by remember { mutableStateOf(false) }

    val passwordsMatch = confirmPassword.isEmpty() || password == confirmPassword

    LaunchedEffect(Unit) {
        delay(80);  headerVisible = true
        delay(220); formVisible   = true
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        // Hero photo full-bleed
        HeroBgImage(
            resId       = R.drawable.hero_bg_2,
            modifier    = Modifier.fillMaxSize(),
            darkOverlay = 0.70f,
        )
        // Blue radial glow overlay
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    colors = listOf(KntBlue.copy(alpha = 0.18f), Color.Transparent),
                    center = Offset(900f, 600f),
                    radius = 900f,
                )
            )
        )

        // Decorative stripes
        Box(Modifier.align(Alignment.CenterEnd).width(5.dp).fillMaxHeight(0.50f).background(c.blue))
        Box(Modifier.align(Alignment.CenterEnd).offset(x = (-14).dp).width(2.dp).fillMaxHeight(0.30f).background(c.yellow))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(16.dp))

            // ── Header with back button ───────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onLogin, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Rounded.ArrowBackIosNew,
                        contentDescription = "Back to login",
                        tint = c.textMuted,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Logo & heading ────────────────────────────────────────────────
            AnimatedVisibility(
                visible = headerVisible,
                enter   = fadeIn(tween(700)) + slideInVertically(tween(700, easing = EaseOutCubic)) { -30 },
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(R.drawable.logo),
                        contentDescription = "K&T Transport",
                        modifier = Modifier.width(120.dp),
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text  = "Create Account",
                        style = MaterialTheme.typography.displaySmall.copy(color = KntWhite),
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        Modifier
                            .width(48.dp).height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Brush.horizontalGradient(listOf(KntYellow, KntYellowDim)))
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text  = "Join K&T Transport today",
                        style = MaterialTheme.typography.bodyMedium.copy(color = KntMuted),
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Form ─────────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = formVisible,
                enter   = fadeIn(tween(600, 150)) + slideInVertically(tween(600, 150)) { 40 },
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    // Section label
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.width(3.dp).height(16.dp).clip(RoundedCornerShape(2.dp)).background(c.yellow))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Personal Details",
                            style = MaterialTheme.typography.labelLarge.copy(color = c.textMuted),
                        )
                    }

                    KntTextField(
                        value         = fullName,
                        onValueChange = { fullName = it },
                        label         = "Full Name",
                        leadingIcon   = Icons.Rounded.Person,
                    )

                    Spacer(Modifier.height(12.dp))

                    KntTextField(
                        value         = email,
                        onValueChange = { email = it },
                        label         = "Email Address",
                        leadingIcon   = Icons.Rounded.Email,
                    )

                    Spacer(Modifier.height(12.dp))

                    KntTextField(
                        value         = phone,
                        onValueChange = { phone = it },
                        label         = "Phone Number",
                        leadingIcon   = Icons.Rounded.Phone,
                    )

                    Spacer(Modifier.height(24.dp))

                    // Section label
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.width(3.dp).height(16.dp).clip(RoundedCornerShape(2.dp)).background(c.blue))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Security",
                            style = MaterialTheme.typography.labelLarge.copy(color = c.textMuted),
                        )
                    }

                    // Password
                    OutlinedTextField(
                        value         = password,
                        onValueChange = { password = it },
                        label         = { Text("Password", style = MaterialTheme.typography.bodySmall) },
                        leadingIcon   = {
                            Icon(Icons.Rounded.Lock, null, tint = c.textMuted, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon  = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                    contentDescription = null,
                                    tint = c.textMuted,
                                    modifier = Modifier.size(18.dp),
                                )
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

                    Spacer(Modifier.height(12.dp))

                    // Confirm Password
                    OutlinedTextField(
                        value         = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label         = { Text("Confirm Password", style = MaterialTheme.typography.bodySmall) },
                        leadingIcon   = {
                            Icon(Icons.Rounded.Lock, null, tint = c.textMuted, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon  = {
                            IconButton(onClick = { confirmVisible = !confirmVisible }) {
                                Icon(
                                    imageVector = if (confirmVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                    contentDescription = null,
                                    tint = c.textMuted,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        },
                        visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError       = !passwordsMatch,
                        supportingText = if (!passwordsMatch) {
                            { Text("Passwords do not match", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
                        } else null,
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = RoundedCornerShape(12.dp),
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor      = if (passwordsMatch) c.blue else MaterialTheme.colorScheme.error,
                            unfocusedBorderColor    = if (passwordsMatch) c.borderColor else MaterialTheme.colorScheme.error,
                            focusedLabelColor       = c.blue,
                            unfocusedLabelColor     = c.textMuted,
                            focusedTextColor        = c.textBright,
                            unfocusedTextColor      = c.textBright,
                            cursorColor             = c.blue,
                            focusedContainerColor   = c.surface2,
                            unfocusedContainerColor = c.surface2,
                        ),
                    )

                    Spacer(Modifier.height(28.dp))

                    // Register button
                    val canSubmit = fullName.isNotBlank() && email.isNotBlank() && phone.isNotBlank()
                        && password.isNotBlank() && passwordsMatch && confirmPassword.isNotBlank()

                    Button(
                        onClick  = onSignedUp,
                        enabled  = canSubmit,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor         = c.blue,
                            contentColor           = Color.White,
                            disabledContainerColor = c.surface2,
                            disabledContentColor   = c.textDim,
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    ) {
                        Icon(Icons.Rounded.HowToReg, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Create Account",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // Terms note
                    Text(
                        text  = "By creating an account you agree to our Terms of Service and Privacy Policy.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = c.textDim,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(28.dp))

                    // Already have account
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            "Already have an account?",
                            style = MaterialTheme.typography.bodySmall.copy(color = c.textMuted),
                        )
                        TextButton(onClick = onLogin, contentPadding = PaddingValues(horizontal = 6.dp)) {
                            Text(
                                "Sign In",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color      = c.yellow,
                                    fontWeight = FontWeight.Bold,
                                ),
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}
