package com.flymero.mifimanager.ui.devices

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flymero.mifimanager.data.model.ClientDevice
import com.flymero.mifimanager.data.model.ConnectedDevice
import com.flymero.mifimanager.ui.components.DeviceListItem
import com.flymero.mifimanager.ui.theme.ErrorLight
import com.flymero.mifimanager.ui.theme.Success

@Composable
fun DevicesScreen(
    onNavigateToAuth: () -> Unit = {},
    viewModel: DevicesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(state.actionResult) {
        state.actionResult?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearResult()
        }
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

    val tabTitles = listOf(
        "在线设备 ${onlineDevices.size}",
        "已屏蔽 ${blockedDevices.size}",
        "离线设备 ${offlineDevices.size}",
        "历史设备 ${historyDevices.size}"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "设备管理", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                Row {
                    IconButton(onClick = onNavigateToAuth) {
                        Icon(Icons.Default.Security, contentDescription = "上网认证")
                    }
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                }
            }

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                tabTitles.forEachIndexed { index, title ->
                    SegmentedButton(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = tabTitles.size),
                        label = { Text(title, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                when (selectedTab) {
                    0 -> items(onlineDevices) { device ->
                        ClientDeviceItem(device, "在线", Success, false, context, viewModel)
                    }
                    1 -> items(blockedDevices) { device ->
                        ClientDeviceItem(device, "已屏蔽", ErrorLight, true, context, viewModel)
                    }
                    2 -> items(offlineDevices) { device ->
                        ClientDeviceItem(device, "离线", MaterialTheme.colorScheme.onSurfaceVariant, false, context, viewModel)
                    }
                    3 -> items(historyDevices) { device ->
                        HistoryDeviceItem(device)
                    }
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
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
