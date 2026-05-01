package com.kntransport.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.*
import com.kntransport.app.network.ApiResult
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*
import com.kntransport.app.viewmodel.UserViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Blocking screen shown after login when termsAcceptedAt is null.
 * The user cannot proceed until they explicitly accept the T&Cs.
 * Tapping "Terms of Service" or "Privacy Policy" opens the respective in-app screen.
 */
@Composable
fun AcceptTermsScreen(
    onAccepted  : () -> Unit,
    onTerms     : () -> Unit,
    onPrivacy   : () -> Unit,
    onSignOut   : () -> Unit,
    viewModel   : UserViewModel = viewModel(),
) {
    val c           = LocalAppColors.current
    val updateState by viewModel.updateState.collectAsState()

    var termsAccepted   by remember { mutableStateOf(false) }
    var privacyAccepted by remember { mutableStateOf(false) }
    var errorMessage    by remember { mutableStateOf<String?>(null) }

    val bothAccepted = termsAccepted && privacyAccepted
    val isLoading    = updateState is ApiResult.Loading

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        errorMessage?.let { snackbar.showSnackbar(it); errorMessage = null }
    }

    LaunchedEffect(updateState) {
        when (val s = updateState) {
            is ApiResult.Success -> { viewModel.resetUpdateState(); onAccepted() }
            is ApiResult.Error   -> { errorMessage = s.message; viewModel.resetUpdateState() }
            else -> {}
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost   = { SnackbarHost(snackbar) },
    ) { pv ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv)
                .background(
                    Brush.verticalGradient(listOf(c.bgGradientTop, c.bgGradientMid, c.bgGradientBottom))
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(48.dp))

                // Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(c.blue.copy(0.15f))
                        .border(1.5.dp, c.blue.copy(0.4f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.Gavel, null, tint = c.blue, modifier = Modifier.size(40.dp))
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    "Before You Continue",
                    style     = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color     = c.textBright,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "K&T Transport has updated its Terms of Service and Privacy Policy. " +
                    "Please read and accept them to continue using the app.",
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = c.textMuted,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(36.dp))

                // Terms of Service checkbox row
                TermsCheckRow(
                    checked     = termsAccepted,
                    onToggle    = { termsAccepted = it },
                    linkLabel   = "Terms of Service",
                    onLinkClick = onTerms,
                    prefix      = "I have read and agree to the ",
                )

                Spacer(Modifier.height(12.dp))

                // Privacy Policy checkbox row
                TermsCheckRow(
                    checked     = privacyAccepted,
                    onToggle    = { privacyAccepted = it },
                    linkLabel   = "Privacy Policy",
                    onLinkClick = onPrivacy,
                    prefix      = "I have read and agree to the ",
                )

                Spacer(Modifier.height(36.dp))

                // Accept button
                Button(
                    onClick  = { viewModel.acceptTerms() },
                    enabled  = bothAccepted && !isLoading,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = c.blue,
                        contentColor           = Color.White,
                        disabledContainerColor = c.surface2,
                        disabledContentColor   = c.textDim,
                    ),
                    elevation = ButtonDefaults.buttonElevation(4.dp),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Rounded.CheckCircle, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Accept & Continue",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Sign out escape — user can decline and exit
                TextButton(onClick = onSignOut) {
                    Text(
                        "Decline & Sign Out",
                        style = MaterialTheme.typography.labelMedium,
                        color = c.textDim,
                    )
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun TermsCheckRow(
    checked     : Boolean,
    onToggle    : (Boolean) -> Unit,
    linkLabel   : String,
    onLinkClick : () -> Unit,
    prefix      : String,
) {
    val c = LocalAppColors.current
    val annotated = buildAnnotatedString {
        withStyle(SpanStyle(color = if (checked) c.textBright else c.textMuted)) { append(prefix) }
        pushStringAnnotation("LINK", "link")
        withStyle(SpanStyle(color = c.blue, fontWeight = FontWeight.SemiBold)) { append(linkLabel) }
        pop()
        withStyle(SpanStyle(color = if (checked) c.textBright else c.textMuted)) { append(".") }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (checked) c.blue.copy(0.08f) else c.surface2.copy(0.5f))
            .border(1.dp, if (checked) c.blue.copy(0.4f) else c.borderColor, RoundedCornerShape(12.dp))
            .clickable { onToggle(!checked) }
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Checkbox(
            checked         = checked,
            onCheckedChange = onToggle,
            colors          = CheckboxDefaults.colors(
                checkedColor   = c.blue,
                uncheckedColor = c.textDim,
                checkmarkColor = Color.White,
            ),
            modifier = Modifier.size(20.dp).padding(top = 1.dp),
        )
        Spacer(Modifier.width(10.dp))
        ClickableText(
            text  = annotated,
            style = MaterialTheme.typography.bodySmall,
            onClick = { offset ->
                annotated.getStringAnnotations("LINK", offset, offset)
                    .firstOrNull()?.let { onLinkClick() }
                    ?: onToggle(!checked)
            },
        )
    }
}
