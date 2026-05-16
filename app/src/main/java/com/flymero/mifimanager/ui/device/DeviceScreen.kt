package com.flymero.mifimanager.ui.device

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import com.flymero.mifimanager.data.model.DhcpInfo
import com.flymero.mifimanager.data.model.HomepageInfo
import com.flymero.mifimanager.data.model.SimCard
import com.flymero.mifimanager.data.model.SimInfo
import com.flymero.mifimanager.data.model.WanInfo
import com.flymero.mifimanager.ui.components.CardTitle
import com.flymero.mifimanager.ui.components.KeyValueRow
import com.flymero.mifimanager.ui.components.SectionCard
import com.flymero.mifimanager.ui.components.SectionDivider
import com.flymero.mifimanager.ui.components.SettingsActionRow
import com.flymero.mifimanager.ui.theme.ErrorContainerLight

private data class NetworkModeOption(val value: String, val label: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceScreen(
    onLogout: () -> Unit = {},
    viewModel: DeviceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showResetSheet by remember { mutableStateOf(false) }
    var showRestartSheet by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showNetworkModeSheet by remember { mutableStateOf(false) }
    var deviceInfoExpanded by remember { mutableStateOf(false) }
    var dhcpExpanded by remember { mutableStateOf(false) }

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
    val networkOptions = networkModeOptions()
    val canSwitchNetworkMode = networkOptions.size > 1

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "管理",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )

            SimManagementCard(
                simInfo = simInfo,
                homepage = homepage,
                planEquipment = planEquipment,
                onSwitchSim = viewModel::switchSim
            )

            MobileNetworkCard(
                wan = wan,
                apn = apn.apn,
                canSwitchNetworkMode = canSwitchNetworkMode,
                onOpenNetworkModeSheet = { showNetworkModeSheet = true }
            )

            DhcpCard(
                dhcp = dhcp,
                expanded = dhcpExpanded,
                onToggleExpanded = { dhcpExpanded = !dhcpExpanded }
            )

            OperationsCard(
                onChangePassword = { showPasswordDialog = true },
                onRestart = { showRestartSheet = true },
                onLogout = viewModel::logout
            )

            DeviceInfoCard(
                homepage = homepage,
                firmwareVersion = firmware.versionNum.ifEmpty { homepage.swVersion },
                hardwareVersion = firmware.hwVersion.ifEmpty { homepage.hwVersion },
                firmwareDate = firmware.versionDate,
                expanded = deviceInfoExpanded,
                context = context,
                onToggleExpanded = { deviceInfoExpanded = !deviceInfoExpanded },
                onCopy = { label, value ->
                    copyToClipboard(context, label, value)
                    viewModel.clearResult()
                }
            )

            DangerZoneCard(onRestoreFactory = { showResetSheet = true })

            Spacer(modifier = Modifier.height(24.dp))
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }

    if (showNetworkModeSheet) {
        ModalBottomSheet(onDismissRequest = { showNetworkModeSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "选择网络模式",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "当前：${wan.networkModeName()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                networkOptions.forEachIndexed { index, option ->
                    SettingsActionRow(
                        label = option.label,
                        value = if (option.value == wan.nwMode) "当前" else null,
                        onClick = {
                            showNetworkModeSheet = false
                            if (option.value != wan.nwMode) viewModel.setNetworkMode(option.value)
                        }
                    )
                    if (index != networkOptions.lastIndex) SectionDivider()
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showRestartSheet) {
        ConfirmationSheet(
            title = "重启设备",
            message = "确定要重启随身 WiFi 吗？重启期间网络将断开。",
            confirmLabel = "确认重启",
            onDismiss = { showRestartSheet = false },
            onConfirm = {
                showRestartSheet = false
                viewModel.restartDevice()
            }
        )
    }

    if (showResetSheet) {
        ConfirmationSheet(
            title = "恢复出厂设置",
            message = "将清除所有配置并重启设备，此操作不可撤销。",
            confirmLabel = "确认恢复",
            confirmColor = MaterialTheme.colorScheme.error,
            onDismiss = { showResetSheet = false },
            onConfirm = {
                showResetSheet = false
                viewModel.restoreFactory()
            }
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
                    androidx.compose.material3.OutlinedTextField(
                        value = newPwd,
                        onValueChange = { newPwd = it },
                        label = { Text("新密码") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    androidx.compose.material3.OutlinedTextField(
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

@Composable
private fun SimManagementCard(
    simInfo: SimInfo,
    homepage: HomepageInfo,
    planEquipment: com.flymero.mifimanager.data.model.PlanEquipment?,
    onSwitchSim: (String) -> Unit
) {
    val currentMode = if (simInfo.switchMode == "0") "智能选网" else "指定 SIM"
    val switchableSims = simInfo.simList.filter { sim ->
        sim.isPresent() && !sim.isBanned() && !isCurrentSim(simInfo, sim, planEquipment)
    }

    SectionCard {
        CardTitle("SIM 卡管理")
        KeyValueRow("当前模式", currentMode)
        if (simInfo.simList.isNotEmpty()) SectionDivider()
        simInfo.simList.forEachIndexed { index, sim ->
            val operator = planEquipment?.cardList?.find { it.iccid == sim.simIccid }?.operatorText
                ?.takeIf { it.isNotBlank() }
                ?: sim.carrierName()
            KeyValueRow(
                label = sim.simName.ifBlank { "SIM${index + 1}" },
                value = "$operator · ${simStatusText(simInfo, sim, planEquipment)}"
            )
            if (index != simInfo.simList.lastIndex) SectionDivider()
        }
        if (simInfo.simList.isNotEmpty()) SectionDivider()
        switchableSims.forEach { sim ->
            SettingsActionRow(
                label = "切换到 ${sim.simName.ifBlank { "SIM" }}",
                value = null,
                onClick = { onSwitchSim(sim.simId) }
            )
            SectionDivider()
        }
        SettingsActionRow(
            label = "智能选网",
            value = if (simInfo.switchMode == "0") "当前" else null,
            onClick = { if (simInfo.switchMode != "0") onSwitchSim("4") }
        )
    }
}

@Composable
private fun MobileNetworkCard(
    wan: WanInfo,
    apn: String,
    canSwitchNetworkMode: Boolean,
    onOpenNetworkModeSheet: () -> Unit
) {
    SectionCard {
        CardTitle("移动网络")
        if (canSwitchNetworkMode) {
            SettingsActionRow(
                label = "网络模式",
                value = wan.networkModeName(),
                onClick = onOpenNetworkModeSheet
            )
        } else {
            KeyValueRow("网络模式", wan.networkModeName())
        }
        SectionDivider()
        KeyValueRow("APN", apn.ifBlank { "--" })
        SectionDivider()
        KeyValueRow("MTU", wan.mtu.ifBlank { "--" })
    }
}

@Composable
private fun DhcpCard(
    dhcp: DhcpInfo,
    expanded: Boolean,
    onToggleExpanded: () -> Unit
) {
    SectionCard {
        CardTitle(
            title = "DHCP 设置",
            trailing = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable(onClick = onToggleExpanded)
                )
            }
        )
        KeyValueRow("状态", if (dhcp.status == "1") "启用" else "禁用")
        SectionDivider()
        KeyValueRow("地址池", "${dhcp.start.ifBlank { "--" }} - ${dhcp.end.ifBlank { "--" }}")
        AnimatedVisibility(expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionDivider()
                KeyValueRow("IP 地址", dhcp.ip.ifBlank { "--" })
                SectionDivider()
                KeyValueRow("子网掩码", dhcp.mask.ifBlank { "--" })
                SectionDivider()
                KeyValueRow("租约时间", "${(dhcp.leaseTime.toLongOrNull() ?: 0) / 3600} 小时")
            }
        }
    }
}

@Composable
private fun OperationsCard(
    onChangePassword: () -> Unit,
    onRestart: () -> Unit,
    onLogout: () -> Unit
) {
    SectionCard {
        CardTitle("操作")
        SettingsActionRow(label = "修改管理密码", onClick = onChangePassword)
        SectionDivider()
        SettingsActionRow(label = "重启设备", onClick = onRestart)
        SectionDivider()
        SettingsActionRow(label = "退出登录", onClick = onLogout)
    }
}

@Composable
private fun DeviceInfoCard(
    homepage: HomepageInfo,
    firmwareVersion: String,
    hardwareVersion: String,
    firmwareDate: String,
    expanded: Boolean,
    context: Context,
    onToggleExpanded: () -> Unit,
    onCopy: (String, String) -> Unit
) {
    SectionCard {
        CardTitle(
            title = "设备信息",
            trailing = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable(onClick = onToggleExpanded)
                )
            }
        )
        SummaryCopyRow("设备型号", homepage.deviceName.ifEmpty { homepage.deviceModel }, context, onCopy)
        SectionDivider()
        SummaryCopyRow("固件版本", firmwareVersion.ifBlank { "--" }, context, onCopy)
        SectionDivider()
        SummaryCopyRow("IMEI", homepage.imei.ifBlank { "--" }, context, onCopy)
        SectionDivider()
        SummaryCopyRow("MAC 地址", homepage.mac.ifBlank { "--" }, context, onCopy)
        AnimatedVisibility(expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionDivider()
                SummaryCopyRow("硬件版本", hardwareVersion.ifBlank { "--" }, context, onCopy)
                SectionDivider()
                SummaryCopyRow("发布日期", firmwareDate.ifBlank { "--" }, context, onCopy)
                SectionDivider()
                SummaryCopyRow("序列号", homepage.serialNumber.ifBlank { "--" }, context, onCopy)
                SectionDivider()
                SummaryCopyRow("IMSI", homepage.imsi.ifBlank { "--" }, context, onCopy)
                SectionDivider()
                SummaryCopyRow("ICCID", homepage.iccid.ifBlank { "--" }, context, onCopy)
            }
        }
    }
}

@Composable
private fun DangerZoneCard(onRestoreFactory: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = ErrorContainerLight.copy(alpha = 0.7f),
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
                text = "将清除所有配置并重启设备。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SettingsActionRow(
                label = "恢复出厂设置",
                valueColor = MaterialTheme.colorScheme.error,
                onClick = onRestoreFactory
            )
        }
    }
}

@Composable
private fun SummaryCopyRow(
    label: String,
    value: String,
    context: Context,
    onCopy: (String, String) -> Unit
) {
    KeyValueRow(
        label = label,
        value = value,
        onCopy = { if (value.isNotBlank() && value != "--") onCopy(label, value) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfirmationSheet(
    title: String,
    message: String,
    confirmLabel: String,
    confirmColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider()
            TextButton(onClick = onConfirm, modifier = Modifier.align(Alignment.End)) {
                Text(confirmLabel, color = confirmColor)
            }
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                Text("取消")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun networkModeOptions(): List<NetworkModeOption> = listOf(
    NetworkModeOption("3", "自动"),
    NetworkModeOption("2", "仅 4G"),
    NetworkModeOption("1", "仅 3G"),
    NetworkModeOption("0", "仅 2G")
)

private fun isCurrentSim(
    simInfo: SimInfo,
    sim: SimCard,
    planEquipment: com.flymero.mifimanager.data.model.PlanEquipment?
): Boolean {
    val planSim = planEquipment?.cardList?.find { it.iccid == sim.simIccid }
    return simInfo.switchMode == "1" && sim.simId == simInfo.soleSimId || planSim?.isInUse() == true
}

private fun simStatusText(
    simInfo: SimInfo,
    sim: SimCard,
    planEquipment: com.flymero.mifimanager.data.model.PlanEquipment?
): String = when {
    !sim.isPresent() -> "未插入"
    isCurrentSim(simInfo, sim, planEquipment) -> "当前使用"
    sim.isBanned() -> "已禁用"
    else -> "可切换"
}

private fun copyToClipboard(context: Context, label: String, value: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
}
