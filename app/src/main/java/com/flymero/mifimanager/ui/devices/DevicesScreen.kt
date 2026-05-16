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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.flymero.mifimanager.ui.components.DeviceListItem
import com.flymero.mifimanager.ui.theme.ErrorLight
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
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    when (page) {
                        0 -> {
                            items(onlineDevices) { device ->
                                ClientDeviceItem(device, "在线", Success, false, context, viewModel)
                            }
                            items(blockedDevices) { device ->
                                ClientDeviceItem(device, "已屏蔽", ErrorLight, true, context, viewModel)
                            }
                            items(offlineDevices) { device ->
                                ClientDeviceItem(device, "离线", MaterialTheme.colorScheme.onSurfaceVariant, false, context, viewModel)
                            }
                        }
                        1 -> items(onlineDevices) { device ->
                            ClientDeviceItem(device, "在线", Success, false, context, viewModel)
                        }
                        2 -> items(blockedDevices) { device ->
                            ClientDeviceItem(device, "已屏蔽", ErrorLight, true, context, viewModel)
                        }
                        3 -> items(offlineDevices) { device ->
                            ClientDeviceItem(device, "离线", MaterialTheme.colorScheme.onSurfaceVariant, false, context, viewModel)
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
    viewModel: DevicesViewModel
) {
    DeviceListItem(
        name = device.decodedName(),
        mac = device.mac,
        ip = device.ip,
        statusText = statusText,
        statusColor = statusColor,
        isBlocked = isBlocked,
        menuItems = buildList {
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
