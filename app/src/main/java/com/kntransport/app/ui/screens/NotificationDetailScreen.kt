package com.kntransport.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kntransport.app.data.AppNotification
import com.kntransport.app.data.NotifType
import com.kntransport.app.ui.components.KntPrimaryButton
import com.kntransport.app.ui.components.KntScaffold
import com.kntransport.app.ui.components.KntSecondaryButton
import com.kntransport.app.ui.components.formatRelativeTime
import com.kntransport.app.ui.theme.*

@Composable
fun NotificationDetailScreen(
    notification  : AppNotification,
    onBack        : () -> Unit,
    onViewTrip    : ((tripId: String) -> Unit)? = null,
    onViewLiftClub: ((clubId: String) -> Unit)? = null,
    onReviewQuote : ((quoteId: String) -> Unit)? = null,
) {
    val c                    = LocalAppColors.current
    val (icon, tint, label)  = notifMeta(notification.type)

    KntScaffold(title = "Notification", onBack = onBack) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(32.dp))

            // Type icon badge
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(36.dp))
            }

            Spacer(Modifier.height(16.dp))

            // Type label chip
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = tint.copy(alpha = 0.12f),
            ) {
                Text(
                    text  = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = tint,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }

            Spacer(Modifier.height(16.dp))

            // Title
            Text(
                text      = notification.title,
                style     = MaterialTheme.typography.headlineSmall,
                color     = c.textBright,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(8.dp))

            // Timestamp
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Rounded.Schedule, null, tint = c.textDim, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text  = formatRelativeTime(notification.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = c.textDim,
                )
            }

            Spacer(Modifier.height(24.dp))

            // Body card
            Surface(
                shape  = RoundedCornerShape(16.dp),
                color  = c.surface2,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text     = notification.body,
                    style    = MaterialTheme.typography.bodyMedium,
                    color    = c.textMuted,
                    modifier = Modifier.padding(20.dp),
                )
            }

            // Contextual action button
            val refId   = notification.referenceId
            val refType = notification.referenceType

            if (refId != null) {
                Spacer(Modifier.height(28.dp))

                when {
                    refType == "QUOTE" && onReviewQuote != null -> {
                        KntPrimaryButton(
                            text    = "Review Quote",
                            onClick = { onReviewQuote(refId) },
                            icon    = Icons.Rounded.RequestQuote,
                        )
                    }
                    refType == "TRIP" && onViewTrip != null -> {
                        KntPrimaryButton(
                            text    = "View Trip",
                            onClick = { onViewTrip(refId) },
                            icon    = Icons.Rounded.DirectionsBus,
                        )
                    }
                    refType == "LIFT_CLUB" && onViewLiftClub != null -> {
                        KntPrimaryButton(
                            text    = "View Lift Club",
                            onClick = { onViewLiftClub(refId) },
                            icon    = Icons.Rounded.Groups,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            KntSecondaryButton(text = "Back", onClick = onBack)
            Spacer(Modifier.height(32.dp))
        }
    }
}

private data class NotifMeta(val icon: ImageVector, val tint: androidx.compose.ui.graphics.Color, val label: String)

private fun notifMeta(type: NotifType): NotifMeta = when (type) {
    NotifType.QUOTE_RECEIVED   -> NotifMeta(Icons.Rounded.RequestQuote,  KntYellow,    "Quote Received")
    NotifType.TRIP_CONFIRMED   -> NotifMeta(Icons.Rounded.CheckCircle,   StatusGreen,  "Trip Confirmed")
    NotifType.LIFT_CLUB_UPDATE -> NotifMeta(Icons.Rounded.Groups,        KntBlueBright,"Lift Club Update")
    NotifType.GENERAL          -> NotifMeta(Icons.Rounded.Notifications, KntMuted,     "General")
}
