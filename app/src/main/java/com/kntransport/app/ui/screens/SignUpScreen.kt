package com.kntransport.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kntransport.app.R
import com.kntransport.app.network.ApiResult
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*
import com.kntransport.app.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SignUpScreen(
    onSignedUp: () -> Unit,
    onLogin   : () -> Unit,
    viewModel : AuthViewModel = viewModel(),
) {
    val c           = LocalAppColors.current
    val signUpState by viewModel.signUpState.collectAsState()

    var fullName         by remember { mutableStateOf("") }
    var email            by remember { mutableStateOf("") }
    var phone            by remember { mutableStateOf("") }
    var password         by remember { mutableStateOf("") }
    var confirmPassword  by remember { mutableStateOf("") }
    var passwordVisible  by remember { mutableStateOf(false) }
    var confirmVisible   by remember { mutableStateOf(false) }
    var termsAccepted    by remember { mutableStateOf(false) }
    var errorMessage     by remember { mutableStateOf<String?>(null) }
    var headerVisible    by remember { mutableStateOf(false) }
    var formVisible      by remember { mutableStateOf(false) }

    val isLoading      = signUpState is ApiResult.Loading
    val passwordsMatch = confirmPassword.isEmpty() || password == confirmPassword
    var showSuccess    by remember { mutableStateOf(false) }
    var registeredName by remember { mutableStateOf("") }

    LaunchedEffect(signUpState) {
        when (val s = signUpState) {
            is ApiResult.Success -> {
                registeredName = s.data.user.name.split(" ").first()
                viewModel.resetSignUpState()
                showSuccess = true
            }
            is ApiResult.Error -> { errorMessage = s.message; viewModel.resetSignUpState() }
            else -> {}
        }
    }

    // Auto-navigate after showing success for 2 seconds
    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            delay(2000)
            onSignedUp()
        }
    }

    if (showSuccess) {
        SignUpSuccessScreen(name = registeredName, onGetStarted = onSignedUp)
        return
    }

    val snackbarState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        errorMessage?.let { snackbarState.showSnackbar(it); errorMessage = null }
    }

    LaunchedEffect(Unit) {
        delay(80);  headerVisible = true
        delay(220); formVisible   = true
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost   = { SnackbarHost(snackbarState) },
    ) { scaffoldPadding ->
    Box(
        modifier = Modifier.fillMaxSize().padding(scaffoldPadding),
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
                    if (password.isNotEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        PasswordStrengthIndicator(password = password)
                        if (password.length < 8) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "Minimum 8 characters required",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }

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
                    val passwordLongEnough = password.length >= 8
                    val canSubmit = fullName.isNotBlank() && email.isNotBlank() && phone.isNotBlank()
                        && passwordLongEnough && passwordsMatch && confirmPassword.isNotBlank()
                        && termsAccepted && !isLoading

                    Button(
                        onClick  = {
                            viewModel.register(
                                fullName.trim(),
                                email.trim(),
                                phone.trim(),
                                password,
                            )
                        },
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
                        if (isLoading) {
                            CircularProgressIndicator(
                                color       = Color.White,
                                modifier    = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
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
                    }

                    Spacer(Modifier.height(16.dp))

                    // Terms & Conditions checkbox
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (termsAccepted) c.blue.copy(0.08f) else c.surface2.copy(0.5f)
                            )
                            .border(
                                1.dp,
                                if (termsAccepted) c.blue.copy(0.4f) else c.borderColor,
                                RoundedCornerShape(10.dp),
                            )
                            .clickable { termsAccepted = !termsAccepted }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked         = termsAccepted,
                            onCheckedChange = { termsAccepted = it },
                            colors          = CheckboxDefaults.colors(
                                checkedColor        = c.blue,
                                uncheckedColor      = c.textDim,
                                checkmarkColor      = Color.White,
                            ),
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text  = "I have read and agree to the Terms of Service and Privacy Policy.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (termsAccepted) c.textBright else c.textMuted,
                            ),
                        )
                    }

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
    } // Scaffold
}

@Composable
private fun SignUpSuccessScreen(name: String, onGetStarted: () -> Unit) {
    val c = LocalAppColors.current

    var iconVisible   by remember { mutableStateOf(false) }
    var textVisible   by remember { mutableStateOf(false) }
    var buttonVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100); iconVisible   = true
        delay(400); textVisible   = true
        delay(600); buttonVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(c.bgGradientTop, c.bgGradientMid, c.bgGradientBottom))
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Subtle radial glow behind the icon
        Box(
            Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    listOf(StatusGreen.copy(0.12f), Color.Transparent),
                    center = Offset(Float.POSITIVE_INFINITY / 2, Float.POSITIVE_INFINITY / 3),
                    radius = 700f,
                )
            )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier.padding(horizontal = 40.dp),
        ) {
            // Animated check icon
            AnimatedVisibility(
                visible = iconVisible,
                enter   = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness    = Spring.StiffnessMedium,
                    )
                ) + fadeIn(tween(300)),
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(StatusGreen.copy(0.15f))
                        .border(2.dp, StatusGreen.copy(0.4f), androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint     = StatusGreen,
                        modifier = Modifier.size(52.dp),
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // Text block
            AnimatedVisibility(
                visible = textVisible,
                enter   = fadeIn(tween(500)) + slideInVertically(tween(500, easing = EaseOutCubic)) { 30 },
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Welcome${if (name.isNotBlank()) ", $name!" else "!"}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color      = c.textBright,
                        ),
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Your account has been created successfully.\nYou're all set to start booking trips.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = c.textMuted),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            // Button
            AnimatedVisibility(
                visible = buttonVisible,
                enter   = fadeIn(tween(400)) + slideInVertically(tween(400, easing = EaseOutCubic)) { 20 },
            ) {
                Button(
                    onClick   = onGetStarted,
                    modifier  = Modifier.fillMaxWidth().height(54.dp),
                    shape     = RoundedCornerShape(14.dp),
                    colors    = ButtonDefaults.buttonColors(
                        containerColor = StatusGreen,
                        contentColor   = Color.White,
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                ) {
                    Icon(Icons.Rounded.RocketLaunch, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Get Started",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
            }
        }
    }
}
