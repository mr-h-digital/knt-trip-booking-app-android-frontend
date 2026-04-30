package com.kntransport.app.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
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
                update     = CameraUpdateFactory.newLatLngZoom(driverLatLng, 15f),
                durationMs = 800,
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

    // Build the custom vehicle marker bitmap once
    val vehicleMarker: BitmapDescriptor = remember { buildVehicleMarkerIcon() }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        color    = c.surface1,
        border   = BorderStroke(1.dp, c.blue.copy(0.35f)),
    ) {
        Column {
            // ── Header row ────────────────────────────────────────────────
            Row(
                modifier          = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector        = Icons.Rounded.MyLocation,
                    contentDescription = null,
                    tint               = KntBlueBright,
                    modifier           = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text     = if (driverName != null) "Tracking $driverName" else "Live Tracking",
                    style    = MaterialTheme.typography.titleSmall,
                    color    = c.textBright,
                    modifier = Modifier.weight(1f),
                )
                // Live / Locating badge
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
                        zoomControlsEnabled     = false,
                        myLocationButtonEnabled = false,
                        mapToolbarEnabled       = false,
                    ),
                    properties = MapProperties(mapType = MapType.NORMAL),
                ) {
                    if (driverLatLng != null) {
                        Marker(
                            state   = MarkerState(position = driverLatLng),
                            title   = driverName ?: "Driver",
                            snippet = "Your K&T driver",
                            icon    = vehicleMarker,
                            anchor  = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
                        )
                    }
                }

                // Bottom gradient blend
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
 * Draws a circular KNT-branded marker:
 *   - Blue filled circle with a thin yellow ring
 *   - White minibus icon drawn with canvas primitives
 *   - Small white triangle pointing down as the map pin tail
 */
private fun buildVehicleMarkerIcon(): BitmapDescriptor {
    val size   = 120  // px — large enough to look sharp on hdpi screens
    val radius = size / 2f
    val tail   = 20   // height of the downward triangle

    val bmp    = Bitmap.createBitmap(size, size + tail, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)

    val blue   = android.graphics.Color.parseColor("#1565C0")
    val yellow = android.graphics.Color.parseColor("#FFC107")
    val white  = android.graphics.Color.WHITE

    // ── Drop shadow ───────────────────────────────────────────────────────────
    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color   = android.graphics.Color.argb(60, 0, 0, 0)
        maskFilter = android.graphics.BlurMaskFilter(6f, android.graphics.BlurMaskFilter.Blur.NORMAL)
    }
    canvas.drawCircle(radius, radius + 3f, radius - 4f, shadowPaint)

    // ── Yellow outer ring ─────────────────────────────────────────────────────
    val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = yellow }
    canvas.drawCircle(radius, radius, radius - 2f, ringPaint)

    // ── Blue filled circle ────────────────────────────────────────────────────
    val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = blue }
    canvas.drawCircle(radius, radius, radius - 8f, circlePaint)

    // ── Pin tail (white triangle pointing down) ───────────────────────────────
    val tailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = blue }
    val tailPath  = android.graphics.Path().apply {
        moveTo(radius - 10f, size - 4f)
        lineTo(radius + 10f, size - 4f)
        lineTo(radius,       (size + tail).toFloat())
        close()
    }
    canvas.drawPath(tailPath, tailPaint)
    // yellow border on tail
    val tailBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color   = yellow
        style   = Paint.Style.STROKE
        strokeWidth = 2.5f
    }
    canvas.drawPath(tailPath, tailBorder)

    // ── Minibus icon (drawn with canvas primitives) ───────────────────────────
    val cx   = radius
    val cy   = radius - 4f
    val wp   = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = white }

    // Bus body
    val bodyLeft  = cx - 28f; val bodyTop    = cy - 14f
    val bodyRight = cx + 28f; val bodyBottom = cy + 12f
    val bodyRect  = RectF(bodyLeft, bodyTop, bodyRight, bodyBottom)
    canvas.drawRoundRect(bodyRect, 7f, 7f, wp)

    // Windows strip
    val winPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = blue }
    val winRect  = RectF(bodyLeft + 4f, bodyTop + 3f, bodyRight - 4f, bodyTop + 12f)
    canvas.drawRoundRect(winRect, 3f, 3f, winPaint)

    // Window dividers
    val divPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color       = white
        strokeWidth = 1.5f
    }
    listOf(cx - 8f, cx + 8f).forEach { x ->
        canvas.drawLine(x, bodyTop + 3f, x, bodyTop + 12f, divPaint)
    }

    // Wheels
    val wheelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = blue }
    val wheelRim   = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = yellow }
    listOf(cx - 16f, cx + 16f).forEach { wx ->
        canvas.drawCircle(wx, bodyBottom, 7f, wheelPaint)
        canvas.drawCircle(wx, bodyBottom, 3f, wheelRim)
    }

    // K&T label on body
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color     = blue
        textSize  = 11f
        typeface  = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText("K&T", cx, bodyBottom - 1f, textPaint)

    return BitmapDescriptorFactory.fromBitmap(bmp)
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
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(14.dp),
        color    = if (isSharing) KntBlue.copy(0.12f) else c.surface2,
        border   = BorderStroke(1.dp, if (isSharing) KntBlue.copy(0.5f) else c.borderColor),
    ) {
        Row(
            modifier          = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isSharing) KntBlueBright.copy(alpha = pulseAlpha) else c.textDim),
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
                colors          = SwitchDefaults.colors(
                    checkedThumbColor   = KntBlueBright,
                    checkedTrackColor   = KntBlue.copy(0.4f),
                    uncheckedThumbColor = c.textDim,
                    uncheckedTrackColor = c.surface2,
                ),
            )
        }
    }
}
