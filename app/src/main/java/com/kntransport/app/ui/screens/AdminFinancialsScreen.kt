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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kntransport.app.R
import com.kntransport.app.network.ApiResult
import com.kntransport.app.network.FinancialReportDto
import com.kntransport.app.ui.components.*
import com.kntransport.app.ui.theme.*
import com.kntransport.app.util.FinancialReportData
import com.kntransport.app.util.ReportExporter
import com.kntransport.app.util.RouteEntry
import com.kntransport.app.util.TransactionEntry
import com.kntransport.app.viewmodel.AdminViewModel
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFinancialsScreen(
    onBack   : () -> Unit,
    viewModel: AdminViewModel = viewModel(),
) {
    val c               = LocalAppColors.current
    val context         = LocalContext.current
    val reportState     by viewModel.financialReport.collectAsState()
    val snackbarState   = remember { SnackbarHostState() }
    val scope           = rememberCoroutineScope()
    var showExportSheet by remember { mutableStateOf(false) }
    var exporting       by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadFinancialReport() }

    fun buildReportData(report: FinancialReportDto) = FinancialReportData(
        period       = "Full Report",
        totalRevenue = "R${String.format("%.0f", report.grossRevenue)}",
        collected    = "R${String.format("%.0f", report.grossRevenue)}",
        outstanding  = "R${String.format("%.0f", report.lineItems.filter { !it.accepted }.sumOf { it.amount })}",
        routes       = emptyList(),
        transactions = report.lineItems.take(20).map {
            TransactionEntry(
                commuter = it.clientName,
                route    = it.referenceType,
                amount   = "R${String.format("%.2f", it.amount)}",
                date     = it.date.take(10),
                paid     = it.accepted,
            )
        },
    )

    if (showExportSheet && reportState is ApiResult.Success) {
        val report = (reportState as ApiResult.Success<FinancialReportDto>).data
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
                    "Choose a format to export the financial report.",
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
                            withContext(Dispatchers.IO) { ReportExporter.sharePdf(context, buildReportData(report)) }
                            exporting = false; showExportSheet = false
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
                            withContext(Dispatchers.IO) { ReportExporter.shareCsv(context, buildReportData(report)) }
                            exporting = false; showExportSheet = false
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
                            withContext(Dispatchers.IO) { ReportExporter.shareViaEmail(context, buildReportData(report)) }
                            exporting = false; showExportSheet = false
                        }
                    },
                )
            }
        }
    }

    KntScaffold(title = "Financials", onBack = onBack, snackbarHost = { SnackbarHost(snackbarState) }) { pv ->
        when (val state = reportState) {
            is ApiResult.Loading -> Box(Modifier.fillMaxSize().padding(pv), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is ApiResult.Error -> Box(Modifier.fillMaxSize().padding(pv), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Rounded.ErrorOutline, null, tint = c.textDim, modifier = Modifier.size(44.dp))
                    Text(state.message, style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                    KntPrimaryButton(text = "Retry", onClick = { viewModel.loadFinancialReport() }, modifier = Modifier.fillMaxWidth(0.5f))
                }
            }
            is ApiResult.Success -> {
                val report = state.data
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
                                        text   = "R${String.format("%.0f", report.grossRevenue)}",
                                        style  = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                                        colors = listOf(KntYellow, KntOrange),
                                    )
                                    Text("${report.invoiceCount} invoices", style = MaterialTheme.typography.bodySmall, color = KntMuted)
                                    Spacer(Modifier.height(16.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Column {
                                            Text("Avg Invoice", style = MaterialTheme.typography.labelSmall, color = KntMuted)
                                            Text("R${String.format("%.0f", report.averageInvoiceValue)}",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = StatusGreen)
                                        }
                                        Column {
                                            Text("Monthly", style = MaterialTheme.typography.labelSmall, color = KntMuted)
                                            Text("R${String.format("%.0f", report.monthlyRevenue)}",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = KntOrange)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                        SectionHeader(title = "Payment Cycle Breakdown")
                        Spacer(Modifier.height(12.dp))

                        val cycles = listOf(
                            Triple("Once-off",    report.onceOffRevenue,     c.blue),
                            Triple("Weekly",      report.weeklyRevenue,      c.yellow),
                            Triple("Fortnightly", report.fortnightlyRevenue, KntOrange),
                            Triple("Monthly",     report.monthlyRevenue,     StatusGreen),
                        ).filter { it.second > 0 }

                        val maxCycleRevenue = cycles.maxOfOrNull { it.second }?.toFloat() ?: 1f
                        cycles.forEach { (label, amount, color) ->
                            Surface(
                                shape    = RoundedCornerShape(10.dp),
                                color    = c.surface2,
                                border   = BorderStroke(1.dp, c.borderColor),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Row {
                                        Text(label, style = MaterialTheme.typography.bodySmall, color = c.textBright, modifier = Modifier.weight(1f))
                                        Text("R${String.format("%.0f", amount)}", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = KntYellow)
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    val ratio = (amount.toFloat() / maxCycleRevenue).coerceIn(0f, 1f)
                                    Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(c.surface1)) {
                                        Box(Modifier.fillMaxWidth(ratio).fillMaxHeight().clip(RoundedCornerShape(3.dp))
                                            .background(Brush.horizontalGradient(listOf(KntYellow, KntOrange))))
                                    }
                                }
                            }
                        }

                        if (report.lineItems.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            SectionHeader(title = "Recent Transactions")
                            Spacer(Modifier.height(12.dp))

                            report.lineItems.take(20).forEach { item ->
                                KntCard(onClick = {}) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp))
                                                .background(if (item.accepted) StatusGreen.copy(0.12f) else KntOrange.copy(0.12f)),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                if (item.accepted) Icons.Rounded.CheckCircle else Icons.Rounded.Schedule,
                                                null,
                                                tint     = if (item.accepted) StatusGreen else KntOrange,
                                                modifier = Modifier.size(18.dp),
                                            )
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(item.clientName, style = MaterialTheme.typography.titleSmall, color = c.textBright)
                                            Text(item.referenceType, style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                                            Text(item.date.take(10), style = MaterialTheme.typography.labelSmall, color = c.textDim)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("R${String.format("%.2f", item.amount)}",
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = KntYellow)
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (item.accepted) StatusGreen.copy(0.12f) else KntOrange.copy(0.12f),
                                            ) {
                                                Text(
                                                    if (item.accepted) "Accepted" else "Pending",
                                                    style    = MaterialTheme.typography.labelSmall,
                                                    color    = if (item.accepted) StatusGreen else KntOrange,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(10.dp))
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        KntPrimaryButton(
                            text    = "Export Report",
                            onClick = { showExportSheet = true },
                            icon    = Icons.Rounded.Share,
                        )

                        Spacer(Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}
