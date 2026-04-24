package com.kntransport.app.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kntransport.app.R
import com.kntransport.app.network.AnalyticsDto
import com.kntransport.app.network.ApiResult
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*
import com.kntransport.app.viewmodel.AdminViewModel

@Composable
fun AdminAnalyticsScreen(
    onBack   : () -> Unit,
    viewModel: AdminViewModel = viewModel(),
) {
    val c              = LocalAppColors.current
    val analyticsState by viewModel.analytics.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadAnalytics() }

    KntScaffold(title = "Analytics", onBack = onBack) { pv ->
        when (val state = analyticsState) {
            is ApiResult.Loading -> Box(Modifier.fillMaxSize().padding(pv), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is ApiResult.Error -> Box(Modifier.fillMaxSize().padding(pv), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Rounded.ErrorOutline, null, tint = c.textDim, modifier = Modifier.size(44.dp))
                    Text(state.message, style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                    KntPrimaryButton(text = "Retry", onClick = { viewModel.loadAnalytics() }, modifier = Modifier.fillMaxWidth(0.5f))
                }
            }
            is ApiResult.Success -> AnalyticsContent(data = state.data, onBack = onBack, pv = pv)
        }
    }
}

@Composable
private fun AnalyticsContent(data: AnalyticsDto, onBack: () -> Unit, pv: androidx.compose.foundation.layout.PaddingValues) {
    val c = LocalAppColors.current

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
            HeroBgImage(resId = R.drawable.hero_bg_6, modifier = Modifier.fillMaxSize(), darkOverlay = 0.52f)
            Column(Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 12.dp)) {
                Text(
                    "Business insights",
                    style = MaterialTheme.typography.labelMedium.copy(color = KntYellow, letterSpacing = 0.5.sp),
                )
            }
        }

        Column(Modifier.padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(20.dp))
            SectionHeader(title = "Users")
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AnalyticChip("${data.totalUsers}",     "Total Users",  c.blue,      Modifier.weight(1f))
                AnalyticChip("${data.totalCommuters}", "Commuters",    c.yellow,    Modifier.weight(1f))
                AnalyticChip("${data.totalDrivers}",   "Drivers",      KntOrange,   Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))
            SectionHeader(title = "Trips")
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AnalyticChip("${data.totalTrips}",     "Total Trips",  c.blue,      Modifier.weight(1f))
                AnalyticChip("${data.completedTrips}", "Completed",    StatusGreen, Modifier.weight(1f))
                AnalyticChip("${data.confirmedTrips}", "Confirmed",    c.yellow,    Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))
            SectionHeader(title = "Revenue")
            Spacer(Modifier.height(12.dp))

            Surface(
                shape    = RoundedCornerShape(14.dp),
                color    = c.surface2,
                border   = BorderStroke(1.dp, StatusGreen.copy(0.25f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                        Column {
                            Text("Total Revenue", style = MaterialTheme.typography.labelSmall, color = c.textMuted)
                            GradientText(
                                text   = "R${String.format("%.0f", data.totalRevenue)}",
                                style  = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                colors = listOf(KntYellow, KntOrange),
                            )
                        }
                        Column {
                            Text("Avg per Trip", style = MaterialTheme.typography.labelSmall, color = c.textMuted)
                            Text(
                                "R${String.format("%.0f", data.averageTripValue)}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = StatusGreen,
                            )
                        }
                    }
                }
            }

            if (data.tripStatusBreakdown.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                SectionHeader(title = "Trip Status Breakdown")
                Spacer(Modifier.height(12.dp))

                val completed   = data.tripStatusBreakdown.find { it.status == "COMPLETED" }?.count?.toFloat() ?: 0f
                val confirmed   = data.tripStatusBreakdown.find { it.status == "CONFIRMED" }?.count?.toFloat() ?: 0f
                val pending     = data.tripStatusBreakdown.find { it.status == "PENDING_QUOTE" }?.count?.toFloat() ?: 0f
                val total       = (completed + confirmed + pending).coerceAtLeast(1f)
                val cRatio      = completed / total
                val inRatio     = confirmed / total

                Surface(
                    shape    = RoundedCornerShape(14.dp),
                    color    = c.surface2,
                    border   = BorderStroke(1.dp, c.borderColor),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Canvas(modifier = Modifier.fillMaxWidth().height(20.dp).clip(RoundedCornerShape(10.dp))) {
                            val w = size.width
                            val h = size.height
                            drawRoundRect(StatusGreen, size = Size(w * cRatio, h), cornerRadius = CornerRadius(10f))
                            drawRoundRect(
                                StatusAmber,
                                topLeft = Offset(w * cRatio, 0f),
                                size = Size(w * inRatio, h),
                            )
                            drawRoundRect(
                                StatusRed,
                                topLeft = Offset(w * (cRatio + inRatio), 0f),
                                size = Size(w * (1f - cRatio - inRatio), h),
                                cornerRadius = CornerRadius(10f),
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            LegendDot(StatusGreen, "Completed (${completed.toInt()})")
                            LegendDot(StatusAmber, "Confirmed (${confirmed.toInt()})")
                            LegendDot(StatusRed,   "Pending (${pending.toInt()})")
                        }
                    }
                }
            }

            if (data.revenueByMonth.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                SectionHeader(title = "Revenue by Month")
                Spacer(Modifier.height(12.dp))

                val maxRevenue = data.revenueByMonth.maxOf { it.revenue }.toFloat().coerceAtLeast(1f)
                data.revenueByMonth.take(6).forEach { month ->
                    MonthRevenueBar(
                        month      = month.month,
                        revenue    = month.revenue,
                        maxRevenue = maxRevenue,
                        color      = c.blue,
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }

            Spacer(Modifier.height(24.dp))
            SectionHeader(title = "Lift Clubs")
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AnalyticChip("${data.totalLiftClubs}",  "Total",  c.blue,      Modifier.weight(1f))
                AnalyticChip("${data.openLiftClubs}",   "Open",   c.yellow,    Modifier.weight(1f))
                AnalyticChip("${data.activeLiftClubs}", "Active", StatusGreen, Modifier.weight(1f))
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AnalyticChip(value: String, label: String, tint: Color, modifier: Modifier = Modifier) {
    val c = LocalAppColors.current
    Surface(
        shape    = RoundedCornerShape(12.dp),
        color    = c.surface2,
        border   = BorderStroke(1.dp, tint.copy(alpha = 0.25f)),
        modifier = modifier,
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            GradientText(
                text   = value,
                style  = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                colors = listOf(tint, tint.copy(0.7f)),
            )
            Text(label, style = MaterialTheme.typography.labelSmall, color = c.textMuted)
        }
    }
}

@Composable
private fun MonthRevenueBar(month: String, revenue: Double, maxRevenue: Float, color: Color) {
    val c = LocalAppColors.current
    Surface(
        shape  = RoundedCornerShape(10.dp),
        color  = c.surface2,
        border = BorderStroke(1.dp, c.borderColor),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(month, style = MaterialTheme.typography.bodySmall, color = c.textBright, modifier = Modifier.weight(1f))
                Text("R${String.format("%.0f", revenue)}", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = color)
            }
            Spacer(Modifier.height(6.dp))
            val ratio = (revenue.toFloat() / maxRevenue).coerceIn(0f, 1f)
            Box(
                Modifier.fillMaxWidth().height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(c.surface1)
            ) {
                Box(
                    Modifier.fillMaxWidth(ratio).fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(color)
                )
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall, color = LocalAppColors.current.textMuted)
    }
}
