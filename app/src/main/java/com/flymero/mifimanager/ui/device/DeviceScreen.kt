package com.flymero.mifimanager.ui.device

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flymero.mifimanager.ui.components.CardTitle
import com.flymero.mifimanager.ui.components.KeyValueRow
import com.flymero.mifimanager.ui.components.SectionCard
import com.flymero.mifimanager.ui.components.SectionDivider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceScreen(
    onLogout: () -> Unit = {},
    viewModel: DeviceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showRestartDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var networkModeExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.actionResult) {
        state.actionResult?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearResult()
        }
    }

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) onLogout()
    }

    if (state.isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) { CircularProgressIndicator() }
        return
    }

    val homepage = state.homepageInfo
    val wan = state.wanInfo
    val dhcp = state.dhcpInfo
    val apn = state.apnInfo
    val simInfo = state.simInfo
    val firmware = state.firmwareInfo
    val planEquipment = state.planEquipment

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "管理",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )

            SectionCard {
                CardTitle("设备信息")
                KeyValueRow("设备型号", homepage.deviceName.ifEmpty { homepage.deviceModel })
                SectionDivider()
                KeyValueRow("硬件版本", firmware.hwVersion.ifEmpty { homepage.hwVersion })
                SectionDivider()
                KeyValueRow("固件版本", firmware.versionNum.ifEmpty { homepage.swVersion })
                SectionDivider()
                KeyValueRow("发布日期", firmware.versionDate)
                SectionDivider()
                KeyValueRow("序列号", homepage.serialNumber)
                SectionDivider()
                KeyValueRow("IMEI", homepage.imei)
                SectionDivider()
                KeyValueRow("IMSI", homepage.imsi)
                SectionDivider()
                KeyValueRow("ICCID", homepage.iccid)
                SectionDivider()
                KeyValueRow("MAC 地址", homepage.mac)
            }

            SectionCard {
                CardTitle("SIM 卡管理")
                KeyValueRow("当前模式", if (simInfo.switchMode == "0") "智能选网" else "指定 SIM")
                if (simInfo.simList.isNotEmpty()) SectionDivider()
                simInfo.simList.forEachIndexed { index, sim ->
                    val planSim = planEquipment?.cardList?.find { it.iccid == sim.simIccid }
                    val isCurrent = simInfo.switchMode == "1" && sim.simId == simInfo.soleSimId || planSim?.isInUse() == true
                    val stateText = when {
                        !sim.isPresent() -> "未插入"
                        isCurrent -> "当前使用"
                        sim.isBanned() -> "已禁用"
                        else -> "可切换"
                    }
                    KeyValueRow("${sim.simName}（${planSim?.operatorText ?: sim.carrierName()}）", stateText)
                    if (sim.isPresent()) {
                        SectionDivider()
                        KeyValueRow("ICCID", sim.simIccid)
                        if (!planSim?.realnameStatusText.isNullOrBlank()) {
                            SectionDivider()
                            KeyValueRow("实名状态", planSim?.realnameStatusText.orEmpty())
                        }
                        if (!isCurrent && !sim.isBanned()) {
                            SectionDivider()
                            KeyValueRow("切换到 ${sim.simName}", "切换", showChevron = true, onClick = { viewModel.switchSim(sim.simId) })
                        }
                    }
                    if (index != simInfo.simList.lastIndex) SectionDivider()
                }
                SectionDivider()
                KeyValueRow(
                    label = "智能选网",
                    value = if (simInfo.switchMode == "0") "当前" else "切换",
                    showChevron = simInfo.switchMode != "0",
                    onClick = { if (simInfo.switchMode != "0") viewModel.switchSim("4") }
                )
            }

            SectionCard {
                CardTitle("移动网络")
                KeyValueRow("当前模式", wan.networkModeName())
                SectionDivider()
                KeyValueRow("APN", apn.apn)
                SectionDivider()
                KeyValueRow("MTU", wan.mtu)
                SectionDivider()
                ExposedDropdownMenuBox(
                    expanded = networkModeExpanded,
                    onExpandedChange = { networkModeExpanded = it }
                ) {
                    OutlinedTextField(
                        value = wan.networkModeName(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("切换网络模式") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(networkModeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = networkModeExpanded,
                        onDismissRequest = { networkModeExpanded = false }
                    ) {
                        listOf("3" to "自动", "2" to "仅 4G", "1" to "仅 3G", "0" to "仅 2G").forEach { (value, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    networkModeExpanded = false
                                    viewModel.setNetworkMode(value)
                                }
                            )
                        }
                    }
                }
            }

            SectionCard {
                CardTitle("DHCP 设置")
                KeyValueRow("状态", if (dhcp.status == "1") "启用" else "禁用")
                SectionDivider()
                KeyValueRow("IP 地址", dhcp.ip)
                SectionDivider()
                KeyValueRow("子网掩码", dhcp.mask)
                SectionDivider()
                KeyValueRow("起始地址", dhcp.start)
                SectionDivider()
                KeyValueRow("结束地址", dhcp.end)
                SectionDivider()
                KeyValueRow("租约时间", "${(dhcp.leaseTime.toLongOrNull() ?: 0) / 3600} 小时")
            }

            SectionCard {
                CardTitle("操作")
                KeyValueRow("修改管理密码", "进入", showChevron = true, onClick = { showPasswordDialog = true })
                SectionDivider()
                KeyValueRow("重启设备", "执行", showChevron = true, onClick = { showRestartDialog = true })
                SectionDivider()
                KeyValueRow("退出登录", "退出", showChevron = true, onClick = { viewModel.logout() })
            }

            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.24f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "危险操作",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "恢复出厂设置将清除所有配置并重启设备。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    KeyValueRow(
                        label = "恢复出厂设置",
                        value = "执行",
                        showChevron = true,
                        valueColor = MaterialTheme.colorScheme.error,
                        onClick = { showResetDialog = true }
                    )
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }

    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text("重启设备") },
            text = { Text("确定要重启随身 WiFi 吗？重启期间网络将断开。") },
            confirmButton = {
                TextButton(onClick = {
                    showRestartDialog = false
                    viewModel.restartDevice()
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showRestartDialog = false }) { Text("取消") } }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("恢复出厂设置") },
            text = { Text("此操作不可撤销，确定继续吗？") },
            confirmButton = {
                TextButton(onClick = {
                    showResetDialog = false
                    viewModel.restoreFactory()
                }) { Text("确定", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showResetDialog = false }) { Text("取消") } }
        )
    }

    if (showPasswordDialog) {
        var newPwd by remember { mutableStateOf("") }
        var confirmPwd by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("修改管理密码") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newPwd,
                        onValueChange = { newPwd = it },
                        label = { Text("新密码") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = confirmPwd,
                        onValueChange = { confirmPwd = it },
                        label = { Text("确认新密码") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPasswordDialog = false
                        viewModel.changePassword(newPwd)
                    },
                    enabled = newPwd.isNotEmpty() && newPwd == confirmPwd
                ) { Text("保存") }
            },
            dismissButton = { TextButton(onClick = { showPasswordDialog = false }) { Text("取消") } }
        )
    }
}
