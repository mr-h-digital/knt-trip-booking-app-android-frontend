package com.kntransport.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
fun LoginScreen(
    onLogin          : (role: String, termsAccepted: Boolean) -> Unit,
    onSignUp         : () -> Unit,
    onForgotPassword : () -> Unit = {},
    viewModel        : AuthViewModel = viewModel(),
) {
    val c          = LocalAppColors.current
    val loginState by viewModel.loginState.collectAsState()

    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage    by remember { mutableStateOf<String?>(null) }
    var headerVisible   by remember { mutableStateOf(false) }
    var formVisible     by remember { mutableStateOf(false) }
    var footerVisible   by remember { mutableStateOf(false) }

    val isLoading = loginState is ApiResult.Loading
    val isValid   = email.isNotBlank() && password.isNotBlank()

    LaunchedEffect(loginState) {
        when (val s = loginState) {
            is ApiResult.Success -> {
                viewModel.resetLoginState()
                onLogin(s.data.role, s.data.user.termsAcceptedAt != null)
            }
            is ApiResult.Error -> {
                errorMessage = s.message
                viewModel.resetLoginState()
            }
            else -> {}
        }
    }

    val snackbarState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        errorMessage?.let { snackbarState.showSnackbar(it); errorMessage = null }
    }

    LaunchedEffect(Unit) {
        delay(80);  headerVisible = true
        delay(200); formVisible   = true
        delay(200); footerVisible = true
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
            resId       = R.drawable.hero_bg_3,
            modifier    = Modifier.fillMaxSize(),
            darkOverlay = 0.68f,
        )

        // Blue radial glow
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    colors = listOf(KntBlue.copy(alpha = 0.20f), Color.Transparent),
                    center = Offset(500f, 300f),
                    radius = 900f,
                )
            )
        )

        // Decorative right-side stripes
        Box(Modifier.align(Alignment.CenterEnd).width(5.dp).fillMaxHeight(0.55f).background(c.blue))
        Box(Modifier.align(Alignment.CenterEnd).offset(x = (-14).dp).width(2.dp).fillMaxHeight(0.35f).background(c.yellow))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(0.6f))

            // ── Logo & heading ────────────────────────────────────────────────
            AnimatedVisibility(
                visible = headerVisible,
                enter   = fadeIn(tween(700)) + slideInVertically(tween(700, easing = EaseOutCubic)) { -40 },
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(R.drawable.logo),
                        contentDescription = "K&T Transport",
                        modifier = Modifier.width(160.dp),
                    )
                    Spacer(Modifier.height(28.dp))
                    Text(
                        text  = "Welcome Back",
                        style = MaterialTheme.typography.displaySmall.copy(color = KntWhite),
                    )
                    Spacer(Modifier.height(4.dp))
                    // Yellow underline accent
                    Box(
                        Modifier
                            .width(48.dp).height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Brush.horizontalGradient(listOf(KntYellow, KntYellowDim)))
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text  = "Sign in to continue your journey",
                        style = MaterialTheme.typography.bodyMedium.copy(color = KntMuted),
                    )
                }
            }

            Spacer(Modifier.height(36.dp))

            // ── Form ─────────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = formVisible,
                enter   = fadeIn(tween(600, 150)) + slideInVertically(tween(600, 150)) { 40 },
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    // Email / Phone
                    KntTextField(
                        value         = email,
                        onValueChange = { email = it },
                        label         = "Email or Phone Number",
                        leadingIcon   = Icons.Rounded.Person,
                    )

                    Spacer(Modifier.height(14.dp))

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
                                    contentDescription = "Toggle password",
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

                    // Forgot password
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onForgotPassword) {
                            Text(
                                "Forgot password?",
                                style = MaterialTheme.typography.labelMedium.copy(color = c.blue),
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Sign in button
                    Button(
                        onClick  = { viewModel.login(email.trim(), password) },
                        enabled  = isValid && !isLoading,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = c.blue, contentColor = Color.White),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color       = Color.White,
                                modifier    = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(
                                "Sign In",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize   = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                            )
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Rounded.ArrowForward, null, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Divider — create account
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        HorizontalDivider(Modifier.weight(1f), color = c.borderColor)
                        Text(
                            "  or  ",
                            style = MaterialTheme.typography.bodySmall.copy(color = c.textDim),
                        )
                        HorizontalDivider(Modifier.weight(1f), color = c.borderColor)
                    }

                    Spacer(Modifier.height(14.dp))

                    // Create account button
                    OutlinedButton(
                        onClick  = onSignUp,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape    = RoundedCornerShape(14.dp),
                        border   = BorderStroke(1.5.dp, c.yellow),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = c.yellow),
                    ) {
                        Icon(Icons.Rounded.PersonAdd, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Create an Account",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // ── Footer ────────────────────────────────────────────────────────
            AnimatedVisibility(
                visible = footerVisible,
                enter   = fadeIn(tween(600, 400)),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 24.dp),
                ) {
                    Box(
                        Modifier
                            .width(32.dp).height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.Transparent, c.yellow.copy(alpha = 0.5f), Color.Transparent)
                                )
                            )
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Moving Communities Forward",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color        = KntMuted.copy(alpha = 0.5f),
                            letterSpacing = 1.sp,
                        ),
                    )
                }
            }
        }
    }
    } // Scaffold
}

