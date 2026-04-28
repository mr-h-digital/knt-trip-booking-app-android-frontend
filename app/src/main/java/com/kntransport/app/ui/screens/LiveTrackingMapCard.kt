package com.kntransport.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.kntransport.app.network.DriverLocationDto
import com.kntransport.app.ui.theme.*

/**
 * Commuter-facing map card embedded in TripDetailScreen when status is IN_PROGRESS.
 * Polls via TripViewModel.driverLocation and animates the camera to the driver marker.
 */
@Composable
fun LiveTrackingMapCard(
    driverLocation: DriverLocationDto?,
    driverName    : String?,
    modifier      : Modifier = Modifier,
) {
    val c = LocalAppColors.current

    val defaultPosition = LatLng(-33.9249, 18.4241) // Cape Town CBD fallback
    val driverLatLng = driverLocation?.let { LatLng(it.latitude, it.longitude) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(driverLatLng ?: defaultPosition, 15f)
    }

    // Animate camera to driver position whenever it updates
    LaunchedEffect(driverLatLng) {
        if (driverLatLng != null) {
            cameraPositionState.animate(
                update        = CameraUpdateFactory.newLatLngZoom(driverLatLng, 15f),
                durationMs    = 800,
            )
        }
    }

    // Pulse animation for the "Live" badge
    val pulse = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulse.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.25f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "pulseScale",
    )

    Surface(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        color     = c.surface1,
        border    = BorderStroke(1.dp, c.blue.copy(0.35f)),
    ) {
        Column {
            // ── Header row ────────────────────────────────────────────────
            Row(
                modifier          = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.MyLocation,
                    contentDescription = null,
                    tint     = KntBlueBright,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text  = if (driverName != null) "Tracking $driverName" else "Live Tracking",
                    style = MaterialTheme.typography.titleSmall,
                    color = c.textBright,
                    modifier = Modifier.weight(1f),
                )
                // Live badge
                if (driverLocation != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(KntBlue.copy(0.15f))
                            .border(BorderStroke(1.dp, KntBlue.copy(0.4f)), RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(KntBlueBright),
                        )
                        Spacer(Modifier.width(5.dp))
                        Text("LIVE", style = MaterialTheme.typography.labelSmall, color = KntBlueBright)
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(c.surface2)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(8.dp), strokeWidth = 1.5.dp, color = c.textMuted)
                        Spacer(Modifier.width(5.dp))
                        Text("Locating…", style = MaterialTheme.typography.labelSmall, color = c.textMuted)
                    }
                }
            }

            // ── Map ───────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
            ) {
                GoogleMap(
                    modifier            = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings          = MapUiSettings(
                        zoomControlsEnabled  = false,
                        myLocationButtonEnabled = false,
                        mapToolbarEnabled    = false,
                    ),
                    properties = MapProperties(mapType = MapType.NORMAL),
                ) {
                    if (driverLatLng != null) {
                        Marker(
                            state   = MarkerState(position = driverLatLng),
                            title   = driverName ?: "Driver",
                            snippet = "Current location",
                            icon    = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                        )
                    }
                }

                // Overlay gradient at bottom so it blends with the card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(c.surface1.copy(alpha = 0f), c.surface1.copy(alpha = 0.5f))
                            )
                        )
                )
            }
        }
    }
}

/**
 * Driver-facing location sharing badge shown at the top of DriverTripDetailScreen
 * while the trip is IN_PROGRESS.
 */
@Composable
fun DriverLocationSharingBadge(
    isSharing: Boolean,
    onToggle : () -> Unit,
    modifier : Modifier = Modifier,
) {
    val c = LocalAppColors.current

    val pulse = rememberInfiniteTransition(label = "sharePulse")
    val pulseAlpha by pulse.animateFloat(
        initialValue  = 0.5f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "shareAlpha",
    )

    Surface(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        color     = if (isSharing) KntBlue.copy(0.12f) else c.surface2,
        border    = BorderStroke(
            1.dp,
            if (isSharing) KntBlue.copy(0.5f) else c.borderColor,
        ),
    ) {
        Row(
            modifier          = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSharing) KntBlueBright.copy(alpha = pulseAlpha)
                        else c.textDim
                    ),
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text  = if (isSharing) "Sharing your location" else "Location sharing off",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isSharing) KntBlueBright else c.textMuted,
                )
                Text(
                    text  = if (isSharing) "Commuters can see your live position" else "Commuters cannot track this trip",
                    style = MaterialTheme.typography.bodySmall,
                    color = c.textDim,
                )
            }
            Switch(
                checked         = isSharing,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor       = KntBlueBright,
                    checkedTrackColor       = KntBlue.copy(0.4f),
                    uncheckedThumbColor     = c.textDim,
                    uncheckedTrackColor     = c.surface2,
                ),
            )
        }
    }
}
