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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.kntransport.app.R
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*
import com.kntransport.app.util.FinancialReportData
import com.kntransport.app.util.ReportExporter
import com.kntransport.app.util.RouteEntry
import com.kntransport.app.util.TransactionEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
private fun ExportOptionRow(
    icon    : ImageVector,
    tint    : Color,
    title   : String,
    subtitle: String,
    loading : Boolean,
    onClick : () -> Unit,
) {
    val c = LocalAppColors.current
    Surface(
        onClick  = { if (!loading) onClick() },
        shape    = RoundedCornerShape(14.dp),
        color    = c.surface1,
        border   = BorderStroke(1.dp, tint.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                if (loading) {
                    CircularProgressIndicator(color = tint, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title,    style = MaterialTheme.typography.titleSmall, color = c.textBright)
                Text(subtitle, style = MaterialTheme.typography.bodySmall,  color = c.textMuted)
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = c.textDim, modifier = Modifier.size(18.dp))
        }
    }
}

private data class FinancialPeriod(
    val totalRevenue  : String,
    val collected     : String,
    val outstanding   : String,
    val routes        : List<RouteRevenue>,
    val transactions  : List<Transaction>,
)

private data class RouteRevenue(val route: String, val amount: Double, val maxAmount: Double)

private data class Transaction(
    val commuter : String,
    val route    : String,
    val amount   : String,
    val date     : String,
    val paid     : Boolean,
)

private val weekFinancials = FinancialPeriod(
    totalRevenue = "R2,850",
    collected    = "R2,400",
    outstanding  = "R450",
    routes = listOf(
        RouteRevenue("Beacon Valley → CBD",        1200.0, 1200.0),
        RouteRevenue("Tafelsig → Bellville",       850.0,  1200.0),
        RouteRevenue("Westridge → Athlone",        480.0,  1200.0),
        RouteRevenue("Lentegeur → Somerset West",  320.0,  1200.0),
    ),
    transactions = listOf(
        Transaction("Tayla Hendricks",  "→ Cape Town CBD",      "R180.00",  "21 Apr", true),
        Transaction("Nadia Adams",      "→ Bellville Station",  "R150.00",  "21 Apr", true),
        Transaction("Yusuf Daniels",    "→ Athlone",            "R95.00",   "20 Apr", false),
        Transaction("Fatima Jacobs",    "→ Cape Town CBD",      "R180.00",  "19 Apr", true),
        Transaction("Bradley September","→ Somerset West",      "R220.00",  "18 Apr", false),
    ),
)

private val monthFinancials = FinancialPeriod(
    totalRevenue = "R10,200",
    collected    = "R8,900",
    outstanding  = "R1,300",
    routes = listOf(
        RouteRevenue("Beacon Valley → CBD",        4200.0, 4200.0),
        RouteRevenue("Tafelsig → Bellville",       3100.0, 4200.0),
        RouteRevenue("Westridge → Athlone",        1800.0, 4200.0),
        RouteRevenue("Lentegeur → Somerset West",  1100.0, 4200.0),
    ),
    transactions = listOf(
        Transaction("Tayla Hendricks",  "→ Cape Town CBD",      "R180.00",  "21 Apr", true),
        Transaction("Nadia Adams",      "→ Bellville Station",  "R650.00",  "15 Apr", true),
        Transaction("Yusuf Daniels",    "→ Athlone",            "R95.00",   "12 Apr", false),
        Transaction("Fatima Jacobs",    "→ Cape Town CBD",      "R360.00",  "08 Apr", true),
        Transaction("Bradley September","→ Somerset West",      "R480.00",  "01 Apr", false),
    ),
)

private val yearFinancials = FinancialPeriod(
    totalRevenue = "R85,400",
    collected    = "R79,200",
    outstanding  = "R6,200",
    routes = listOf(
        RouteRevenue("Beacon Valley → CBD",        34000.0, 34000.0),
        RouteRevenue("Tafelsig → Bellville",       25000.0, 34000.0),
        RouteRevenue("Westridge → Athlone",        15400.0, 34000.0),
        RouteRevenue("Lentegeur → Somerset West",  11000.0, 34000.0),
    ),
    transactions = listOf(
        Transaction("Tayla Hendricks",  "→ Cape Town CBD",      "R180.00",  "21 Apr", true),
        Transaction("Nadia Adams",      "→ Bellville Station",  "R650.00",  "15 Apr", true),
        Transaction("Yusuf Daniels",    "→ Athlone",            "R95.00",   "12 Apr", false),
        Transaction("Fatima Jacobs",    "→ Cape Town CBD",      "R360.00",  "08 Apr", true),
        Transaction("Bradley September","→ Somerset West",      "R480.00",  "01 Apr", false),
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFinancialsScreen(onBack: () -> Unit) {
    val c               = LocalAppColors.current
    val context         = LocalContext.current
    val periods         = listOf("This Week", "This Month", "This Year")
    var selected        by remember { mutableIntStateOf(1) }
    val snackbarState   = remember { SnackbarHostState() }
    val scope           = rememberCoroutineScope()
    var showExportSheet by remember { mutableStateOf(false) }
    var exporting       by remember { mutableStateOf(false) }

    val fin = when (selected) {
        0    -> weekFinancials
        2    -> yearFinancials
        else -> monthFinancials
    }

    fun buildReportData() = FinancialReportData(
        period       = periods[selected],
        totalRevenue = fin.totalRevenue,
        collected    = fin.collected,
        outstanding  = fin.outstanding,
        routes       = fin.routes.map { RouteEntry(it.route, "R${String.format("%.0f", it.amount)}") },
        transactions = fin.transactions.map { TransactionEntry(it.commuter, it.route, it.amount, it.date, it.paid) },
    )

    if (showExportSheet) {
        ModalBottomSheet(
            onDismissRequest  = { showExportSheet = false },
            containerColor    = c.surface2,
            dragHandle        = {
                Box(
                    Modifier.padding(vertical = 12.dp)
                        .width(40.dp).height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(c.borderColor)
                )
            },
        ) {
            Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
                Text("Export Report", style = MaterialTheme.typography.titleMedium, color = c.textBright)
                Text(
                    "Choose a format to export the ${periods[selected]} financial report.",
                    style    = MaterialTheme.typography.bodySmall,
                    color    = c.textMuted,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
                )

                ExportOptionRow(
                    icon     = Icons.Rounded.PictureAsPdf,
                    tint     = StatusRed,
                    title    = "Export as PDF",
                    subtitle = "Formatted report ready to print or share",
                    loading  = exporting,
                    onClick  = {
                        scope.launch {
                            exporting = true
                            withContext(Dispatchers.IO) { ReportExporter.sharePdf(context, buildReportData()) }
                            exporting        = false
                            showExportSheet  = false
                        }
                    },
                )
                Spacer(Modifier.height(10.dp))

                ExportOptionRow(
                    icon     = Icons.Rounded.TableChart,
                    tint     = StatusGreen,
                    title    = "Export as CSV",
                    subtitle = "Spreadsheet-compatible data file",
                    loading  = exporting,
                    onClick  = {
                        scope.launch {
                            exporting = true
                            withContext(Dispatchers.IO) { ReportExporter.shareCsv(context, buildReportData()) }
                            exporting        = false
                            showExportSheet  = false
                        }
                    },
                )
                Spacer(Modifier.height(10.dp))

                ExportOptionRow(
                    icon     = Icons.Rounded.Email,
                    tint     = c.blue,
                    title    = "Send via Email",
                    subtitle = "Attach CSV and send to accountant",
                    loading  = exporting,
                    onClick  = {
                        scope.launch {
                            exporting = true
                            withContext(Dispatchers.IO) { ReportExporter.shareViaEmail(context, buildReportData()) }
                            exporting        = false
                            showExportSheet  = false
                        }
                    },
                )
            }
        }
    }

    KntScaffold(title = "Financials", onBack = onBack, snackbarHost = { SnackbarHost(snackbarState) }) { pv ->
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
                HeroBgImage(resId = R.drawable.hero_bg_7, modifier = Modifier.fillMaxSize(), darkOverlay = 0.52f)
                Column(Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 12.dp)) {
                    Text(
                        "Revenue & transactions",
                        style = MaterialTheme.typography.labelMedium.copy(color = KntYellow, letterSpacing = 0.5.sp),
                    )
                }
            }

            Column(Modifier.padding(horizontal = 16.dp)) {
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

            Surface(
                shape    = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    Modifier.background(
                        Brush.linearGradient(listOf(KntDark, Color(0xFF0D2E4D)))
                    ).border(BorderStroke(1.dp, KntYellow.copy(alpha = 0.3f)), RoundedCornerShape(18.dp))
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Total Revenue", style = MaterialTheme.typography.labelMedium, color = KntMuted)
                        Spacer(Modifier.height(4.dp))
                        GradientText(
                            text   = fin.totalRevenue,
                            style  = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                            colors = listOf(KntYellow, KntOrange),
                        )
                        Text(periods[selected], style = MaterialTheme.typography.bodySmall, color = KntMuted)
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column {
                                Text("Collected", style = MaterialTheme.typography.labelSmall, color = KntMuted)
                                Text(fin.collected, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = StatusGreen)
                            }
                            Column {
                                Text("Outstanding", style = MaterialTheme.typography.labelSmall, color = KntMuted)
                                Text(fin.outstanding, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = KntOrange)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            SectionHeader(title = "Revenue by Route")
            Spacer(Modifier.height(12.dp))

            fin.routes.forEach { route ->
                Surface(
                    shape    = RoundedCornerShape(10.dp),
                    color    = c.surface2,
                    border   = BorderStroke(1.dp, c.borderColor),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row {
                            Text(route.route, style = MaterialTheme.typography.bodySmall, color = c.textBright, modifier = Modifier.weight(1f))
                            Text("R${String.format("%.0f", route.amount)}", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = KntYellow)
                        }
                        Spacer(Modifier.height(6.dp))
                        val ratio = (route.amount / route.maxAmount).toFloat()
                        Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(c.surface1)) {
                            Box(Modifier.fillMaxWidth(ratio).fillMaxHeight().clip(RoundedCornerShape(3.dp))
                                .background(Brush.horizontalGradient(listOf(KntYellow, KntOrange))))
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            SectionHeader(title = "Recent Transactions")
            Spacer(Modifier.height(12.dp))

            fin.transactions.forEach { tx ->
                KntCard(onClick = {}) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp))
                                .background(if (tx.paid) StatusGreen.copy(0.12f) else KntOrange.copy(0.12f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                if (tx.paid) Icons.Rounded.CheckCircle else Icons.Rounded.Schedule,
                                null,
                                tint     = if (tx.paid) StatusGreen else KntOrange,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(tx.commuter, style = MaterialTheme.typography.titleSmall, color = c.textBright)
                            Text(tx.route,    style = MaterialTheme.typography.bodySmall,  color = c.textMuted)
                            Text(tx.date,     style = MaterialTheme.typography.labelSmall, color = c.textDim)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(tx.amount, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = KntYellow)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (tx.paid) StatusGreen.copy(0.12f) else KntOrange.copy(0.12f),
                            ) {
                                Text(
                                    if (tx.paid) "Paid" else "Pending",
                                    style    = MaterialTheme.typography.labelSmall,
                                    color    = if (tx.paid) StatusGreen else KntOrange,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(16.dp))

            KntPrimaryButton(
                text    = "Export Report",
                onClick = { showExportSheet = true },
                icon    = Icons.Rounded.Share,
            )

            Spacer(Modifier.height(32.dp))
            } // close inner padding Column
        }
    }
}
