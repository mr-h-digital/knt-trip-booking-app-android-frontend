package com.kntransport.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kntransport.app.data.*
import com.kntransport.app.network.ApiResult
import com.kntransport.app.network.NotificationDto
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.components.formatRelativeTime
import com.kntransport.app.ui.theme.*
import com.kntransport.app.viewmodel.NotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack              : () -> Unit,
    onNotificationClick : (AppNotification) -> Unit = {},
    viewModel           : NotificationViewModel = viewModel(),
) {
    val c      = LocalAppColors.current
    val state by viewModel.notifications.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadNotifications() }

    val notifs = (state as? ApiResult.Success)?.data ?: emptyList()
    val hasUnread = notifs.any { !it.read }

    KntScaffold(
        title   = "Notifications",
        onBack  = onBack,
        actions = {
            if (hasUnread) {
                TextButton(onClick = { viewModel.markAllRead() }) {
                    Text("Mark all read", style = MaterialTheme.typography.labelMedium, color = c.blue)
                }
            }
        },
    ) { pv ->
        if (state is ApiResult.Loading) {
            Box(Modifier.fillMaxSize().padding(pv), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@KntScaffold
        }
        if (state is ApiResult.Error) {
            Box(Modifier.fillMaxSize().padding(pv), contentAlignment = Alignment.Center) {
                ErrorState(message = (state as ApiResult.Error).message, onRetry = { viewModel.loadNotifications() })
            }
            return@KntScaffold
        }
        if (notifs.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pv), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Rounded.NotificationsNone, null, tint = c.textDim, modifier = Modifier.size(56.dp))
                    Text("No notifications yet", style = MaterialTheme.typography.bodyMedium, color = c.textMuted)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pv)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Hint row
                Row(
                    Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(Icons.Rounded.SwipeLeft, null, tint = c.textDim, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Swipe right to mark read · swipe left to dismiss",
                        style = MaterialTheme.typography.labelSmall,
                        color = c.textDim,
                    )
                }

                notifs.forEachIndexed { idx, notif ->
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(notif.id) {
                        kotlinx.coroutines.delay(idx * 60L)
                        visible = true
                    }
                    AnimatedVisibility(
                        visible = visible,
                        enter   = slideInVertically(tween(320, easing = EaseOutCubic)) { it / 3 } + fadeIn(tween(320)),
                    ) {
                        SwipeableNotifDtoCard(
                            notif     = notif,
                            onRead    = { viewModel.markRead(notif.id) },
                            onDismiss = { viewModel.dismissLocal(notif.id) },
                            onClick   = {
                                viewModel.markRead(notif.id)
                                // Map to AppNotification for the existing detail screen
                                onNotificationClick(AppNotification(
                                    id            = notif.id,
                                    type          = runCatching { NotifType.valueOf(notif.type) }.getOrElse { NotifType.GENERAL },
                                    title         = notif.title,
                                    body          = notif.body,
                                    timestamp     = notif.timestamp,
                                    read          = notif.read,
                                    referenceId   = notif.referenceId,
                                    referenceType = notif.referenceType,
                                ))
                            },
                        )
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableNotifDtoCard(
    notif    : NotificationDto,
    onRead   : () -> Unit,
    onDismiss: () -> Unit,
    onClick  : () -> Unit = {},
) {
    val type = runCatching { NotifType.valueOf(notif.type) }.getOrElse { NotifType.GENERAL }
    val appNotif = AppNotification(
        id = notif.id, type = type, title = notif.title, body = notif.body,
        timestamp = notif.timestamp, read = notif.read,
        referenceId = notif.referenceId, referenceType = notif.referenceType,
    )
    SwipeableNotifCard(notif = appNotif, onRead = onRead, onDismiss = onDismiss, onClick = onClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableNotifCard(
    notif    : AppNotification,
    onRead   : () -> Unit,
    onDismiss: () -> Unit,
    onClick  : () -> Unit = {},
) {
    val c             = LocalAppColors.current
    val (icon, tint)  = notifStyle(notif.type)
    val dismissState  = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> { onRead();    true }
                SwipeToDismissBoxValue.EndToStart -> { onDismiss(); true }
                else -> false
            }
        },
        positionalThreshold = { it * 0.35f },
    )

    SwipeToDismissBox(
        state            = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val isRead    = direction == SwipeToDismissBoxValue.StartToEnd
            val bgColor   = if (isRead) StatusGreen.copy(alpha = 0.18f) else StatusRed.copy(alpha = 0.18f)
            val iconV     = if (isRead) Icons.Rounded.MarkEmailRead else Icons.Rounded.Delete
            val iconTint  = if (isRead) StatusGreen else StatusRed
            val align     = if (isRead) Alignment.CenterStart else Alignment.CenterEnd

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(bgColor)
                    .padding(horizontal = 20.dp),
                contentAlignment = align,
            ) {
                Icon(iconV, null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
        },
        enableDismissFromStartToEnd = !notif.read,
        enableDismissFromEndToStart = true,
    ) {
        Surface(
            onClick  = onClick,
            shape    = RoundedCornerShape(14.dp),
            color    = if (notif.read) c.surface1.copy(0.9f) else c.surface2,
            border   = if (!notif.read) BorderStroke(1.dp, tint.copy(alpha = 0.3f)) else
                       BorderStroke(1.dp, androidx.compose.ui.graphics.Brush.linearGradient(
                           listOf(androidx.compose.ui.graphics.Color.White.copy(0.08f), c.blue.copy(0.12f))
                       )),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                Box(
                    Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                        .background(tint.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        notif.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (notif.read) c.textMuted else c.textBright,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(notif.body, style = MaterialTheme.typography.bodySmall, color = c.textMuted, maxLines = 2)
                    Spacer(Modifier.height(4.dp))
                    Text(formatRelativeTime(notif.timestamp), style = MaterialTheme.typography.labelSmall, color = c.textDim)
                }
                if (!notif.read) {
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.size(8.dp).clip(CircleShape).background(tint))
                }
            }
        }
    }
}

private fun notifStyle(type: NotifType): Pair<ImageVector, androidx.compose.ui.graphics.Color> = when (type) {
    NotifType.QUOTE_RECEIVED   -> Icons.Rounded.RequestQuote  to KntYellow
    NotifType.TRIP_CONFIRMED   -> Icons.Rounded.CheckCircle   to StatusGreen
    NotifType.LIFT_CLUB_UPDATE -> Icons.Rounded.Groups        to KntBlueBright
    NotifType.GENERAL          -> Icons.Rounded.Notifications to KntMuted
}
