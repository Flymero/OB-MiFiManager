package com.flymero.mifimanager.ui.devices

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flymero.mifimanager.data.model.ClientDevice
import com.flymero.mifimanager.data.model.ConnectedDevice
import com.flymero.mifimanager.ui.components.CardTitle
import com.flymero.mifimanager.ui.components.DeviceListItem
import com.flymero.mifimanager.ui.components.InfoRow
import com.flymero.mifimanager.ui.components.KeyValueRow
import com.flymero.mifimanager.ui.components.SectionCard
import com.flymero.mifimanager.ui.theme.ErrorLight
import com.flymero.mifimanager.ui.theme.Success
import kotlinx.coroutines.launch

@Composable
fun DevicesScreen(
    onNavigateToAuth: () -> Unit = {},
    viewModel: DevicesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedDevice by remember { mutableStateOf<ClientDevice?>(null) }
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.actionResult) {
        state.actionResult?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearResult()
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        selectedTab = pagerState.currentPage
    }

    if (state.isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) { CircularProgressIndicator() }
        return
    }

    val allClients = state.deviceInfo.clientList
    val onlineDevices = allClients.filter { it.status == "1" }
    val blockedDevices = allClients.filter { it.status == "2" }
    val offlineDevices = allClients.filter { it.status == "0" }
    val historyDevices = state.deviceInfo.knownDevicesList
        .filter { history -> allClients.none { it.mac.equals(history.mac, ignoreCase = true) } }

    val tabTitles = listOf(
        "全部 ${allClients.size}",
        "在线设备 ${onlineDevices.size}",
        "已屏蔽 ${blockedDevices.size}",
        "离线设备 ${offlineDevices.size}"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "设备管理", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onNavigateToAuth,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
                        )
                    ) {
                        Icon(Icons.Default.Security, contentDescription = "上网认证")
                    }
                    IconButton(
                        onClick = { viewModel.refresh() },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabTitles.forEachIndexed { index, title ->
                    DeviceCategoryTab(
                        title = title,
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            scope.launch { pagerState.animateScrollToPage(index) }
                        }
                    )
                }
            }

            val sectionTitle = when (selectedTab) {
                0 -> "全部设备"
                1 -> "在线设备"
                2 -> "已屏蔽设备"
                else -> "离线设备"
            }

            Text(
                text = sectionTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (page) {
                        0 -> {
                            items(onlineDevices) { device ->
                                ClientDeviceItem(device, "在线", Success, false, context, viewModel) {
                                    selectedDevice = device
                                }
                            }
                            items(blockedDevices) { device ->
                                ClientDeviceItem(device, "已屏蔽", ErrorLight, true, context, viewModel) {
                                    selectedDevice = device
                                }
                            }
                            items(offlineDevices) { device ->
                                ClientDeviceItem(device, "离线", MaterialTheme.colorScheme.onSurfaceVariant, false, context, viewModel) {
                                    selectedDevice = device
                                }
                            }
                        }
                        1 -> items(onlineDevices) { device ->
                            ClientDeviceItem(device, "在线", Success, false, context, viewModel) {
                                selectedDevice = device
                            }
                        }
                        2 -> items(blockedDevices) { device ->
                            ClientDeviceItem(device, "已屏蔽", ErrorLight, true, context, viewModel) {
                                selectedDevice = device
                            }
                        }
                        3 -> items(offlineDevices) { device ->
                            ClientDeviceItem(device, "离线", MaterialTheme.colorScheme.onSurfaceVariant, false, context, viewModel) {
                                selectedDevice = device
                            }
                        }
                    }

                    if (page == 0 && historyDevices.isNotEmpty()) {
                        item {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
                            ) {
                                Text(
                                    text = "历史设备",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "以下设备当前未在线，仅保留历史记录。",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        items(historyDevices) { device ->
                            HistoryDeviceItem(device)
                        }
                    }
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
    }

    selectedDevice?.let { device ->
        ModalBottomSheet(onDismissRequest = { selectedDevice = null }) {
            DeviceTrafficDetailSheet(device = device, context = context)
        }
    }
}

@Composable
private fun DeviceCategoryTab(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        Box(
            modifier = Modifier
                .height(3.dp)
                .fillMaxWidth()
                .background(
                    color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = CircleShape
                )
        ) {
            if (!selected) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Transparent, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun ClientDeviceItem(
    device: ClientDevice,
    statusText: String,
    statusColor: androidx.compose.ui.graphics.Color,
    isBlocked: Boolean,
    context: Context,
    viewModel: DevicesViewModel,
    onOpenDetails: () -> Unit
) {
    DeviceListItem(
        name = device.decodedName(),
        mac = device.mac,
        ip = device.ip,
        statusText = statusText,
        statusColor = statusColor,
        isBlocked = isBlocked,
        onClick = onOpenDetails,
        menuItems = buildList {
            add("查看详情" to onOpenDetails)
            add("复制 MAC" to {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("mac", device.mac))
                viewModel.showMessage("已复制 MAC 地址")
            })
            if (isBlocked) {
                add("解除屏蔽" to { viewModel.unblockDevice(device.mac) })
            } else {
                add("屏蔽设备" to { viewModel.blockDevice(device.mac) })
            }
        }
    )
}

@Composable
private fun HistoryDeviceItem(device: ConnectedDevice) {
    DeviceListItem(
        name = device.decodedName(),
        mac = device.mac,
        ip = device.ipAddress,
        statusText = "历史",
        statusColor = MaterialTheme.colorScheme.onSurfaceVariant,
        isBlocked = false,
        menuItems = emptyList()
    )
}

@Composable
private fun DeviceTrafficDetailSheet(
    device: ClientDevice,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionCard {
            CardTitle(title = device.decodedName())
            KeyValueRow(label = "状态", value = device.statusText())
            HorizontalDivider()
            KeyValueRow(label = "MAC", value = device.mac, onCopy = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("mac", device.mac))
            }, valueMaxLines = 2)
            HorizontalDivider()
            InfoRow(label = "IP", value = device.ip.takeUnless { it.isBlank() || it == "NA" } ?: "--")
            HorizontalDivider()
            InfoRow(label = "连接方式", value = device.connType.ifBlank { "--" })
            HorizontalDivider()
            InfoRow(label = "总连接时长", value = device.formattedConnectionDuration())
            HorizontalDivider()
            InfoRow(label = "最近记录时间", value = device.timeAdded.ifBlank { "--" })
        }

        SectionCard {
            CardTitle(title = "累计流量")
            TrafficInfoRow(label = "累计接收", formattedValue = device.formattedRx(), rawValue = device.rx, suspicious = device.isTrafficValueSuspicious(device.rx))
            HorizontalDivider()
            TrafficInfoRow(label = "累计发送", formattedValue = device.formattedTx(), rawValue = device.tx, suspicious = device.isTrafficValueSuspicious(device.tx))
        }

        SectionCard {
            CardTitle(title = "周期流量")
            TrafficInfoRow(label = "本月接收", formattedValue = device.formattedRxMonth(), rawValue = device.rxMonth, suspicious = device.isTrafficValueSuspicious(device.rxMonth))
            HorizontalDivider()
            TrafficInfoRow(label = "本月发送", formattedValue = device.formattedTxMonth(), rawValue = device.txMonth, suspicious = device.isTrafficValueSuspicious(device.txMonth))
            HorizontalDivider()
            TrafficInfoRow(label = "近 3 天接收", formattedValue = device.formattedRxLast3Days(), rawValue = device.rxLast3Days, suspicious = device.isTrafficValueSuspicious(device.rxLast3Days))
            HorizontalDivider()
            TrafficInfoRow(label = "近 3 天发送", formattedValue = device.formattedTxLast3Days(), rawValue = device.txLast3Days, suspicious = device.isTrafficValueSuspicious(device.txLast3Days))
        }

        Text(
            text = "这些数据来自路由器单设备统计，若数值异常通常是固件上报问题。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun TrafficInfoRow(
    label: String,
    formattedValue: String,
    rawValue: String,
    suspicious: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        KeyValueRow(label = label, value = formattedValue)
        if (suspicious) {
            Text(
                text = "原始值：$rawValue，设备上报值可能异常",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
