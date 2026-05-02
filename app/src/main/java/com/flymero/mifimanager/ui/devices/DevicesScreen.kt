package com.flymero.mifimanager.ui.devices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flymero.mifimanager.ui.theme.SignalBad
import com.flymero.mifimanager.ui.theme.SignalExcellent
import com.flymero.mifimanager.ui.theme.BatteryLow

@Composable
fun DevicesScreen(
    onNavigateToAuth: () -> Unit = {},
    viewModel: DevicesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "设备管理",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Row {
                IconButton(onClick = onNavigateToAuth) {
                    Icon(Icons.Default.Security, contentDescription = "上网认证")
                }
                IconButton(onClick = { viewModel.refresh() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "刷新")
                }
            }
        }

        Text(
            text = "在线 ${onlineDevices.size} | 屏蔽 ${blockedDevices.size} | 历史 ${allClients.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Online devices
            if (onlineDevices.isNotEmpty()) {
                item {
                    Text(
                        text = "在线设备",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                items(onlineDevices) { device ->
                    DeviceCard(
                        name = device.decodedName(),
                        mac = device.mac,
                        ip = device.ip,
                        statusColor = SignalExcellent,
                        statusText = "在线",
                        showBlockButton = true,
                        isBlocked = false,
                        onBlock = { viewModel.blockDevice(device.mac) },
                        onUnblock = {}
                    )
                }
            }

            // Blocked devices
            if (blockedDevices.isNotEmpty()) {
                item {
                    Text(
                        text = "已屏蔽设备",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                }
                items(blockedDevices) { device ->
                    DeviceCard(
                        name = device.decodedName(),
                        mac = device.mac,
                        ip = device.ip,
                        statusColor = BatteryLow,
                        statusText = "已屏蔽",
                        showBlockButton = true,
                        isBlocked = true,
                        onBlock = {},
                        onUnblock = { viewModel.unblockDevice(device.mac) }
                    )
                }
            }

            // Offline devices
            if (offlineDevices.isNotEmpty()) {
                item {
                    Text(
                        text = "离线设备",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                }
                items(offlineDevices) { device ->
                    DeviceCard(
                        name = device.decodedName(),
                        mac = device.mac,
                        ip = device.ip,
                        statusColor = SignalBad,
                        statusText = "离线",
                        showBlockButton = true,
                        isBlocked = false,
                        onBlock = { viewModel.blockDevice(device.mac) },
                        onUnblock = {}
                    )
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}

@Composable
private fun DeviceCard(
    name: String,
    mac: String,
    ip: String,
    statusColor: androidx.compose.ui.graphics.Color,
    statusText: String,
    showBlockButton: Boolean,
    isBlocked: Boolean,
    onBlock: () -> Unit,
    onUnblock: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isBlocked)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.PhoneAndroid,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                if (ip.isNotEmpty() && ip != "NA") {
                    Text(
                        text = ip,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = mac,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Status indicator
            Icon(
                Icons.Default.Circle,
                contentDescription = statusText,
                tint = statusColor,
                modifier = Modifier.size(12.dp)
            )

            // Block/Unblock button
            if (showBlockButton) {
                if (isBlocked) {
                    IconButton(
                        onClick = onUnblock,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.LockOpen,
                            contentDescription = "解除屏蔽",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = onBlock,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Block,
                            contentDescription = "屏蔽",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
