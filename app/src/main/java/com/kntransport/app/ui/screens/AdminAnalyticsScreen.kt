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
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*

private data class PeriodData(
    val tripsCompleted : Int,
    val newUsers       : Int,
    val revenue        : String,
    val avgRating      : Float,
)

private data class RouteData(val name: String, val trips: Int)
private data class StatusBreakdown(val completed: Int, val inProgress: Int, val pending: Int)

private val weekData  = PeriodData(12,  3, "R2,850",  4.3f)
private val monthData = PeriodData(48,  11, "R10,200", 4.5f)
private val yearData  = PeriodData(312, 74, "R85,400", 4.4f)

private val routes = listOf(
    RouteData("Beacon Valley → Cape Town CBD", 98),
    RouteData("Tafelsig → Bellville Station",  72),
    RouteData("Westridge → Athlone",           55),
    RouteData("Lentegeur → Somerset West",     43),
)

private val weekBreakdown  = StatusBreakdown(9,  2, 1)
private val monthBreakdown = StatusBreakdown(38, 6, 4)
private val yearBreakdown  = StatusBreakdown(258,32,22)

@Composable
fun AdminAnalyticsScreen(onBack: () -> Unit) {
    val c       = LocalAppColors.current
    val periods = listOf("This Week", "This Month", "This Year")
    var selected by remember { mutableIntStateOf(1) }

    val data = when (selected) {
        0    -> weekData
        2    -> yearData
        else -> monthData
    }
    val breakdown = when (selected) {
        0    -> weekBreakdown
        2    -> yearBreakdown
        else -> monthBreakdown
    }

    KntScaffold(title = "Analytics", onBack = onBack) { pv ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                periods.forEachIndexed { i, label ->
                    val sel = i == selected
                    Surface(
                        onClick = { selected = i },
                        shape   = RoundedCornerShape(20.dp),
                        color   = if (sel) c.blue else c.surface2,
                        border  = BorderStroke(1.dp, if (sel) c.blue else c.borderColor),
                    ) {
                        Text(
                            label,
                            style    = MaterialTheme.typography.labelMedium,
                            color    = if (sel) Color.White else c.textMuted,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AnalyticChip("${data.tripsCompleted}", "Trips Done",  c.blue,   Modifier.weight(1f))
                AnalyticChip("${data.newUsers}",       "New Users",   c.yellow, Modifier.weight(1f))
                AnalyticChip(data.revenue,             "Revenue",     StatusGreen, Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))
            SectionHeader(title = "Top Routes")
            Spacer(Modifier.height(12.dp))

            val maxTrips = routes.maxOf { it.trips }.toFloat()
            routes.forEach { route ->
                RouteBar(route = route, maxTrips = maxTrips, color = c.blue)
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(24.dp))
            SectionHeader(title = "Trip Status Breakdown")
            Spacer(Modifier.height(12.dp))

            val total  = (breakdown.completed + breakdown.inProgress + breakdown.pending).toFloat()
            val cRatio = breakdown.completed  / total
            val pRatio = breakdown.inProgress / total

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
                            size = Size(w * pRatio, h),
                        )
                        drawRoundRect(
                            StatusRed,
                            topLeft = Offset(w * (cRatio + pRatio), 0f),
                            size = Size(w * (1f - cRatio - pRatio), h),
                            cornerRadius = CornerRadius(10f),
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        LegendItem(StatusGreen,  "Completed (${breakdown.completed})")
                        LegendItem(StatusAmber,  "In Progress (${breakdown.inProgress})")
                        LegendItem(StatusRed,    "Pending (${breakdown.pending})")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            SectionHeader(title = "Average Rating")
            Spacer(Modifier.height(12.dp))

            Surface(
                shape    = RoundedCornerShape(14.dp),
                color    = c.surface2,
                border   = BorderStroke(1.dp, c.borderColor),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    GradientText(
                        text   = String.format("%.1f", data.avgRating),
                        style  = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                        colors = listOf(KntYellow, KntOrange),
                    )
                    Column {
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            repeat(5) { i ->
                                val fill = (data.avgRating - i).coerceIn(0f, 1f)
                                Icon(
                                    imageVector = if (fill >= 1f) Icons.Rounded.Star
                                                  else if (fill > 0f) Icons.Rounded.StarHalf
                                                  else Icons.Rounded.StarOutline,
                                    contentDescription = null,
                                    tint     = KntYellow,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
                        Text("Based on all rated trips", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                    }
                }
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
private fun RouteBar(route: RouteData, maxTrips: Float, color: Color) {
    val c = LocalAppColors.current
    Surface(
        shape  = RoundedCornerShape(10.dp),
        color  = c.surface2,
        border = BorderStroke(1.dp, c.borderColor),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(route.name, style = MaterialTheme.typography.bodySmall, color = c.textBright, modifier = Modifier.weight(1f))
                Text("${route.trips}", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = color)
            }
            Spacer(Modifier.height(6.dp))
            val ratio = route.trips / maxTrips
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
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall, color = LocalAppColors.current.textMuted)
    }
}
