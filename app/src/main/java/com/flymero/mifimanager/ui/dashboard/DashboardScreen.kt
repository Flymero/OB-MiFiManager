package com.flymero.mifimanager.ui.dashboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flymero.mifimanager.ui.components.GaugeChart
import com.flymero.mifimanager.ui.components.InfoRow
import com.flymero.mifimanager.ui.components.StatusCard
import com.flymero.mifimanager.ui.theme.BatteryFull
import com.flymero.mifimanager.ui.theme.BatteryLow
import com.flymero.mifimanager.ui.theme.BatteryMedium
import com.flymero.mifimanager.ui.theme.SignalBad
import com.flymero.mifimanager.ui.theme.SignalExcellent
import com.flymero.mifimanager.ui.theme.SignalFair
import com.flymero.mifimanager.ui.theme.SignalGood
import com.flymero.mifimanager.ui.theme.SignalPoor

@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val status = state.statusInfo
    val homepage = state.homepageInfo
    val stats = state.statisticsInfo
    val plan = state.planInfo
    val context = LocalContext.current

    var showPlanDetail by remember { mutableStateOf(false) }

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

    val isCellularConnected = homepage.connectDisconnect == "cellular"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Text(
            text = homepage.decodedSsid().ifEmpty { "MiFi" },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${homepage.networkName} | ${status.networkType()}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Plan summary card (clickable)
        if (plan != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPlanDetail = true },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = plan.packageName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "到期: ${plan.expiretime}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { plan.usagePercent() / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "已用 ${plan.usedFormatted()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "剩余 ${plan.remainFormatted()} / ${plan.totalFormatted()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Cellular toggle card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isCellularConnected)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceContainerLow
            ),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.SwapVert,
                        contentDescription = null,
                        tint = if (isCellularConnected)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "蜂窝网络",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (state.cellularConnecting) "切换中..."
                                else if (isCellularConnected) "已连接"
                                else "已断开",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Switch(
                    checked = isCellularConnected,
                    onCheckedChange = { viewModel.toggleCellular(it) },
                    enabled = !state.cellularConnecting,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }

        // Signal & Battery gauges
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val signalQuality = status.signalQuality.toIntOrNull() ?: 0
                val signalColor = when (signalQuality) {
                    5 -> SignalExcellent
                    4 -> SignalGood
                    3 -> SignalFair
                    2 -> SignalPoor
                    else -> SignalBad
                }
                GaugeChart(
                    value = signalQuality.toFloat(),
                    maxValue = 5f,
                    label = "信号",
                    color = signalColor
                )

                val batteryPercent = status.batteryPercent.toIntOrNull() ?: 0
                val batteryColor = when {
                    batteryPercent > 60 -> BatteryFull
                    batteryPercent > 20 -> BatteryMedium
                    else -> BatteryLow
                }
                GaugeChart(
                    value = batteryPercent.toFloat(),
                    maxValue = 100f,
                    label = if (status.batteryCharging == "1") "充电中" else "电量",
                    unit = "%",
                    color = batteryColor
                )
            }
        }

        // Speed
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusCard(
                title = "下载速率",
                value = status.formattedSpeed(status.rxSpeed),
                icon = Icons.Default.ArrowDownward,
                modifier = Modifier.weight(1f)
            )
            StatusCard(
                title = "上传速率",
                value = status.formattedSpeed(status.txSpeed),
                icon = Icons.Default.ArrowUpward,
                modifier = Modifier.weight(1f)
            )
        }

        // Traffic
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusCard(
                title = "本次下载",
                value = stats.formattedCurrentTraffic().first,
                icon = Icons.Default.CloudDownload,
                modifier = Modifier.weight(1f)
            )
            StatusCard(
                title = "本次上传",
                value = stats.formattedCurrentTraffic().second,
                icon = Icons.Default.CloudUpload,
                modifier = Modifier.weight(1f)
            )
        }

        // Total traffic
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusCard(
                title = "累计下载",
                value = stats.formattedTotalTraffic().first,
                icon = Icons.Default.Storage,
                modifier = Modifier.weight(1f)
            )
            StatusCard(
                title = "累计上传",
                value = stats.formattedTotalTraffic().second,
                icon = Icons.Default.Storage,
                modifier = Modifier.weight(1f)
            )
        }

        // Devices & Uptime
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusCard(
                title = "在线设备",
                value = "${status.wifiClientsNum} 台",
                icon = Icons.Default.Devices,
                modifier = Modifier.weight(1f)
            )
            StatusCard(
                title = "运行时间",
                value = formatUptime(state.localUptimeSeconds),
                icon = Icons.Default.AccessTime,
                modifier = Modifier.weight(1f)
            )
        }

        // Signal info
        StatusCard(
            title = "信号强度",
            value = "${status.rssi} dBm",
            icon = Icons.Default.SignalCellularAlt,
            subtitle = "网络: ${status.networkType()} | RSSI: ${status.rssi}",
            modifier = Modifier.fillMaxWidth()
        )

        // Network
        StatusCard(
            title = "网络连接",
            value = homepage.networkName,
            icon = Icons.Default.NetworkCell,
            subtitle = "WAN IP: ${homepage.wanIp} | LAN IP: ${homepage.lanIp}",
            modifier = Modifier.fillMaxWidth()
        )
    }

    // Plan detail dialog
    if (showPlanDetail && plan != null) {
        AlertDialog(
            onDismissRequest = { showPlanDetail = false },
            title = { Text("套餐详情") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    InfoRow("套餐名称", plan.packageName)
                    InfoRow("总流量", plan.totalFormatted())
                    InfoRow("已使用", plan.usedFormatted())
                    InfoRow("剩余流量", plan.remainFormatted())
                    InfoRow("使用比例", "${plan.usagePercent().toInt()}%")
                    InfoRow("到期时间", plan.expiretime)
                    InfoRow("账户余额", "¥${plan.balance}")
                    InfoRow("运营商", plan.operator)
                    InfoRow("实名状态", plan.realnameStatus)
                    InfoRow("支付方式", plan.paymentTypeText)
                    plan.equipment?.let { equip ->
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        // Recharge number with copy button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "充值号: ${equip.devNo}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("recharge_no", equip.devNo))
                                    Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "复制",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        InfoRow("设备状态", if (equip.deviceStatus == 1) "在线" else "离线")
                        InfoRow("热点名称", equip.hotspotName)
                        InfoRow("热点密码", equip.hotspotPassword)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text("SIM 卡", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        equip.cardList.forEach { sim ->
                            InfoRow(
                                "${sim.operatorText} ${if (sim.isInUse()) "(当前)" else ""}",
                                sim.realnameStatusText
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPlanDetail = false }) { Text("关闭") }
            }
        )
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
