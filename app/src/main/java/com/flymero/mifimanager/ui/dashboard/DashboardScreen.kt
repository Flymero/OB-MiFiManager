package com.flymero.mifimanager.ui.dashboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flymero.mifimanager.data.model.PlanInfo
import com.flymero.mifimanager.ui.components.CardTitle
import com.flymero.mifimanager.ui.components.KeyValueRow
import com.flymero.mifimanager.ui.components.SectionCard
import com.flymero.mifimanager.ui.components.SectionDivider
import com.flymero.mifimanager.ui.components.StatusChip
import com.flymero.mifimanager.ui.theme.BatteryLow
import com.flymero.mifimanager.ui.theme.BatteryMedium
import com.flymero.mifimanager.ui.theme.SignalBad
import com.flymero.mifimanager.ui.theme.SignalExcellent
import com.flymero.mifimanager.ui.theme.SignalFair
import com.flymero.mifimanager.ui.theme.SignalGood
import com.flymero.mifimanager.ui.theme.SignalPoor
import com.flymero.mifimanager.ui.theme.SpeedDownload
import com.flymero.mifimanager.ui.theme.SpeedUpload
import com.flymero.mifimanager.ui.theme.Success
import com.flymero.mifimanager.ui.theme.SuccessContainer
import com.flymero.mifimanager.ui.theme.Warning
import com.flymero.mifimanager.ui.theme.WarningContainer
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val status = state.statusInfo
    val homepage = state.homepageInfo
    val stats = state.statisticsInfo
    val plan = state.planInfo
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState()

    var showPlanDetail by rememberSaveable { mutableStateOf(false) }
    var showPlanHint by rememberSaveable { mutableStateOf(plan != null) }

    LaunchedEffect(state.refreshMessage) {
        state.refreshMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearRefreshMessage()
        }
    }

    if (state.isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val signalQuality = status.signalQuality.toIntOrNull() ?: 0
    val signalText = signalLabel(signalQuality)
    val signalColor = when (signalQuality) {
        5 -> SignalExcellent
        4 -> SignalGood
        3 -> SignalFair
        2 -> SignalPoor
        else -> SignalBad
    }
    val batteryPercent = status.batteryPercent.toIntOrNull() ?: 0
    val batteryColor = when {
        batteryPercent <= 20 -> BatteryLow
        batteryPercent <= 50 -> BatteryMedium
        else -> Success
    }
    val connectionChipText = when {
        !state.routerReachable && state.lastReachableAtLeastOnce -> "不可达"
        homepage.connectDisconnect == "cellular" -> "已连接"
        else -> "未连接"
    }
    val connectionChipColor = when {
        !state.routerReachable && state.lastReachableAtLeastOnce -> Warning
        homepage.connectDisconnect == "cellular" -> Success
        else -> Warning
    }
    val connectionChipContainer = when {
        !state.routerReachable && state.lastReachableAtLeastOnce -> WarningContainer
        homepage.connectDisconnect == "cellular" -> SuccessContainer
        else -> WarningContainer
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = homepage.decodedSsid().ifEmpty { homepage.deviceName.ifEmpty { "MiFi" } },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${homepage.networkName} · ${status.networkType()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        StatusChip(
                            text = connectionChipText,
                            color = connectionChipColor,
                            containerColor = connectionChipContainer
                        )
                    }
                }
            }

            SectionCard {
                CardTitle("设备状态")
                val batteryIcon = when {
                    status.batteryCharging == "1" -> Icons.Default.BatteryChargingFull
                    batteryPercent <= 20 -> Icons.Default.BatteryAlert
                    else -> Icons.Default.BatteryFull
                }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DashboardMetricCard(
                            title = "信号强度",
                            primary = signalText,
                            secondary = "${status.rssi} dBm",
                            footnote = "${status.signalQuality} 格",
                            icon = Icons.Default.SignalCellularAlt,
                            accentColor = signalColor,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardMetricCard(
                            title = "电量",
                            primary = "$batteryPercent%",
                            secondary = if (status.batteryCharging == "1") "充电中" else if (batteryPercent <= 20) "低电量" else "状态正常",
                            icon = batteryIcon,
                            accentColor = batteryColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DashboardMetricCard(
                            title = "运营商",
                            primary = homepage.networkName.ifEmpty { "未知" },
                            icon = Icons.Default.Language,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardMetricCard(
                            title = "在线设备",
                            primary = "${status.wifiClientsNum} 台",
                            icon = Icons.Default.Devices,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DashboardMetricCard(
                            title = "下载速率",
                            primary = status.formattedSpeed(status.rxSpeed),
                            icon = Icons.Default.ArrowDownward,
                            accentColor = SpeedDownload,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardMetricCard(
                            title = "上传速率",
                            primary = status.formattedSpeed(status.txSpeed),
                            icon = Icons.Default.ArrowUpward,
                            accentColor = SpeedUpload,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (plan != null) {
                SectionCard(
                    onClick = {
                        showPlanHint = false
                        showPlanDetail = true
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = plan.packageName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "到期时间：${plan.expiretime}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "查看详情",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    LinearProgressIndicator(
                        progress = { plan.usagePercent() / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outlineVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "已用 ${plan.usedFormatted()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "剩余 ${plan.remainFormatted()} / ${plan.totalFormatted()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (showPlanHint) {
                        Text(
                            text = "点击套餐卡查看详细用量",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            SectionCard {
                CardTitle("用量统计")
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DashboardStatCard(
                            title = "本次下载",
                            value = stats.formattedCurrentTraffic().first,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardStatCard(
                            title = "本次上传",
                            value = stats.formattedCurrentTraffic().second,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DashboardStatCard(
                            title = "累计下载",
                            value = stats.formattedTotalTraffic().first,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardStatCard(
                            title = "累计上传",
                            value = stats.formattedTotalTraffic().second,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            SectionCard {
                CardTitle("网络连接")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "蜂窝网络",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = when {
                                state.cellularConnecting -> "切换中..."
                                !state.routerReachable && state.lastReachableAtLeastOnce -> "路由器不可达"
                                homepage.connectDisconnect == "cellular" -> "已连接"
                                else -> "已断开"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.routerReachable && homepage.connectDisconnect == "cellular",
                        onCheckedChange = { viewModel.toggleCellular(it) },
                        enabled = !state.cellularConnecting && state.routerReachable
                    )
                }
                SectionDivider()
                KeyValueRow(label = "频段", value = state.bandSummary)
                SectionDivider()
                KeyValueRow(label = "WAN IP", value = homepage.wanIp.ifEmpty { "--" })
                SectionDivider()
                KeyValueRow(label = "LAN IP", value = homepage.lanIp.ifEmpty { "--" })
                SectionDivider()
                KeyValueRow(label = "运行时间", value = formatUptime(state.localUptimeSeconds), valueColor = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }

    if (showPlanDetail && plan != null) {
        ModalBottomSheet(
            onDismissRequest = { showPlanDetail = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            PackageDetailSheet(
                plan = plan,
                context = context,
                isRefreshing = state.isPlanRefreshing,
                onRefresh = viewModel::refreshPlanManually,
                onClose = { showPlanDetail = false }
            )
        }
    }
}

@Composable
private fun DashboardMetricCard(
    title: String,
    primary: String,
    modifier: Modifier = Modifier,
    secondary: String? = null,
    footnote: String? = null,
    accentColor: Color = MaterialTheme.colorScheme.onSurface,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Surface(
        modifier = modifier.height(122.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                icon?.let {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(accentColor.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = primary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                secondary?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (!footnote.isNullOrBlank()) {
                Text(
                    text = footnote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun DashboardStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


@Composable
private fun PackageDetailSheet(
    plan: PlanInfo,
    context: Context,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onClose: () -> Unit
) {
    val daysLeft = plan.daysUntilExpire()
    val dailyBudget = plan.dailyBudget()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "套餐详情",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            TextButton(onClick = onRefresh, enabled = !isRefreshing) {
                Text(if (isRefreshing) "刷新中..." else "刷新")
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = plan.packageName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "到期时间：${plan.expiretime}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "${"%.2f".format(plan.usagePercent())}%",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "已使用",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "剩余 ${plan.remainFormatted()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "总流量 ${plan.totalFormatted()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                LinearProgressIndicator(
                    progress = { plan.usagePercent() / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "已用 ${plan.usedFormatted()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "剩余 ${plan.remainFormatted()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                KeyValueRow(label = "总流量", value = plan.totalFormatted())
                SectionDivider()
                KeyValueRow(label = "已使用", value = plan.usedFormatted())
                SectionDivider()
                KeyValueRow(label = "剩余流量", value = plan.remainFormatted())
                daysLeft?.let {
                    SectionDivider()
                    KeyValueRow(label = "距离到期", value = "$it 天")
                }
                dailyBudget?.let {
                    SectionDivider()
                    KeyValueRow(label = "预计每日可用", value = it)
                }
                SectionDivider()
                KeyValueRow(label = "使用进度", value = "${"%.2f".format(plan.usagePercent())}%")
                SectionDivider()
                KeyValueRow(label = "到期时间", value = plan.expiretime)
                plan.equipment?.takeIf { it.devNo.isNotBlank() }?.let { equipment ->
                    SectionDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "充值号",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = equipment.devNo,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("recharge_no", equipment.devNo))
                            }) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "复制",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        val planRows = buildList {
            plan.balance.takeIf { it.isNotBlank() }?.let { add("账户余额" to "¥$it") }
            plan.operator.takeIf { it.isNotBlank() }?.let { add("运营商" to it) }
            plan.realnameStatus.takeIf { it.isNotBlank() }?.let { add("实名状态" to it) }
            plan.paymentTypeText.takeIf { it.isNotBlank() }?.let { add("支付方式" to it) }
        }
        if (planRows.isNotEmpty()) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    CardTitle("套餐信息")
                    planRows.forEachIndexed { index, (label, value) ->
                        if (index > 0) SectionDivider()
                        KeyValueRow(label = label, value = value)
                    }
                }
            }
        }

        plan.equipment?.let { equipment ->
            val equipmentRows = buildList {
                add("设备状态" to if (equipment.deviceStatus == 1) "在线" else "离线")
                equipment.hotspotName.takeIf { it.isNotBlank() }?.let { add("热点名称" to it) }
                equipment.hotspotPassword.takeIf { it.isNotBlank() }?.let { add("热点密码" to it) }
            }
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    CardTitle("设备与 SIM")
                    equipmentRows.forEachIndexed { index, (label, value) ->
                        if (index > 0) SectionDivider()
                        KeyValueRow(label = label, value = value)
                    }
                    equipment.cardList.forEach { sim ->
                        val label = buildString {
                            append(sim.operatorText.ifBlank { "SIM 卡" })
                            if (sim.isInUse()) append("（当前）")
                        }
                        val value = sim.realnameStatusText.ifBlank { "--" }
                        SectionDivider()
                        KeyValueRow(label = label, value = value)
                    }
                }
            }
        }

        TextButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("知道了", color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

private fun signalLabel(level: Int): String = when (level) {
    5 -> "优秀"
    4 -> "良好"
    3 -> "一般"
    2 -> "较弱"
    else -> "较差"
}
private fun PlanInfo.daysUntilExpire(): Int? {
    val parts = expiretime.split("-")
    if (parts.size != 3) return null
    return runCatching {
        val target = java.time.LocalDate.of(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        val now = java.time.LocalDate.now()
        val days = java.time.temporal.ChronoUnit.DAYS.between(now, target).toInt()
        days.coerceAtLeast(0)
    }.getOrNull()
}

private fun PlanInfo.dailyBudget(): String? {
    val days = daysUntilExpire() ?: return null
    if (days == 0) return remainFormatted()
    val remainPerDayMb = remainAmount / days
    return when {
        remainPerDayMb >= 1024 -> "%.2f GB/天".format(remainPerDayMb / 1024)
        else -> "%.0f MB/天".format(ceil(remainPerDayMb))
    }
}

private fun formatUptime(totalSeconds: Long): String {
    val days = totalSeconds / 86400
    val hours = (totalSeconds % 86400) / 3600
    val minutes = (totalSeconds % 3600) / 60
    val secs = totalSeconds % 60
    return buildString {
        if (days > 0) append("${days}天 ")
        if (hours > 0) append("${hours}时 ")
        append("${minutes}分 ${secs}秒")
    }
}
