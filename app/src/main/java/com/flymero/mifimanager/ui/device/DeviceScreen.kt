package com.flymero.mifimanager.ui.device

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.material3.OutlinedTextField
import com.flymero.mifimanager.ui.components.CardTitle
import com.flymero.mifimanager.ui.components.KeyValueRow
import com.flymero.mifimanager.ui.components.SectionCard
import com.flymero.mifimanager.ui.components.SectionDivider

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
            Text(text = "管理", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)

            SectionCard {
                CardTitle("设备信息")
                KeyValueRow("设备型号", homepage.deviceName.ifEmpty { homepage.deviceModel })
                SectionDivider()
                KeyValueRow("固件版本", firmware.versionNum.ifEmpty { homepage.swVersion })
                SectionDivider()
                KeyValueRow("IMEI", homepage.imei)
            }

            SectionCard {
                CardTitle("SIM 卡管理")
                simInfo.simList.forEachIndexed { index, sim ->
                    val planSim = planEquipment?.cardList?.find { it.iccid == sim.simIccid }
                    val stateText = when {
                        !sim.isPresent() -> "未插入"
                        simInfo.switchMode == "1" && sim.simId == simInfo.soleSimId -> "当前使用"
                        planSim?.isInUse() == true -> "当前使用"
                        else -> "可切换"
                    }
                    KeyValueRow("${sim.simName}（${planSim?.operatorText ?: sim.carrierName()}）", stateText)
                    if (index != simInfo.simList.lastIndex) SectionDivider()
                }
            }

            SectionCard {
                CardTitle("移动网络")
                KeyValueRow("当前模式", wan.networkModeName())
                SectionDivider()
                KeyValueRow("APN", apn.apn)
                SectionDivider()
                KeyValueRow("MTU", wan.mtu)
            }

            SectionCard {
                CardTitle("DHCP 设置")
                KeyValueRow("IP 地址", dhcp.ip)
                SectionDivider()
                KeyValueRow("子网掩码", dhcp.mask)
                SectionDivider()
                KeyValueRow("租约时间", "${(dhcp.leaseTime.toLongOrNull() ?: 0) / 3600} 小时")
            }

            SectionCard {
                CardTitle("操作")
                KeyValueRow("修改管理密码", "进入", showChevron = true, onClick = { showPasswordDialog = true })
                SectionDivider()
                KeyValueRow("重启设备", "执行", showChevron = true, onClick = { showRestartDialog = true })
                SectionDivider()
                KeyValueRow(
                    "恢复出厂设置",
                    "危险操作",
                    showChevron = true,
                    valueColor = MaterialTheme.colorScheme.error,
                    onClick = { showResetDialog = true }
                )
                SectionDivider()
                KeyValueRow("退出登录", "退出", showChevron = true, onClick = { viewModel.logout() })
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
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
