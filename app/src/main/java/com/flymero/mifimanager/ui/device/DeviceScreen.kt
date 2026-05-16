package com.flymero.mifimanager.ui.device

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flymero.mifimanager.data.model.DhcpInfo
import com.flymero.mifimanager.data.model.HomepageInfo
import com.flymero.mifimanager.data.model.PlanEquipment
import com.flymero.mifimanager.data.model.SimCard
import com.flymero.mifimanager.data.model.SimInfo
import com.flymero.mifimanager.data.model.WanInfo
import com.flymero.mifimanager.ui.components.InfoRow
import com.flymero.mifimanager.ui.components.KeyValueRow
import com.flymero.mifimanager.ui.theme.ErrorContainerLight
import com.flymero.mifimanager.ui.theme.SurfaceContainerLowLight

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
    var showRestartDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showNetworkModeSheet by remember { mutableStateOf(false) }
    var deviceInfoExpanded by remember { mutableStateOf(false) }

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

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "设备管理",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            DeviceInfoCard(
                homepage = homepage,
                firmwareVersion = firmware.versionNum.ifEmpty { homepage.swVersion },
                hardwareVersion = firmware.hwVersion.ifEmpty { homepage.hwVersion },
                firmwareDate = firmware.versionDate,
                expanded = deviceInfoExpanded,
                context = context,
                onToggleExpanded = { deviceInfoExpanded = !deviceInfoExpanded }
            )

            SimManagementCard(
                simInfo = simInfo,
                planEquipment = planEquipment,
                isPlanLoading = state.isPlanLoading,
                onSwitchSim = viewModel::switchSim
            )

            NetworkCard(
                wan = wan,
                apn = apn.apn,
                onOpenModeSheet = { showNetworkModeSheet = true }
            )

            DhcpCard(dhcp = dhcp)

            ActionsCard(
                onChangePassword = { showPasswordDialog = true },
                onRestart = { showRestartDialog = true },
                onLogout = viewModel::logout
            )

            DangerZoneCard(onRestoreFactory = { showResetDialog = true })

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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "切换网络模式",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "当前：${wan.networkModeName()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                networkOptions.forEachIndexed { index, option ->
                    TextButton(
                        onClick = {
                            showNetworkModeSheet = false
                            if (option.value != wan.nwMode) viewModel.setNetworkMode(option.value)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(option.label)
                            if (option.value == wan.nwMode) {
                                Text(
                                    text = "当前",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    if (index != networkOptions.lastIndex) HorizontalDivider()
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
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
            text = { Text("将清除所有配置并重启设备，此操作不可撤销。") },
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
                    if (confirmPwd.isNotEmpty() && newPwd != confirmPwd) {
                        Text(
                            "两次密码不一致",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
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
private fun DeviceInfoCard(
    homepage: HomepageInfo,
    firmwareVersion: String,
    hardwareVersion: String,
    firmwareDate: String,
    expanded: Boolean,
    context: Context,
    onToggleExpanded: () -> Unit
) {
    ManagementCard(title = "设备信息") {
        KeyValueRow(
            label = "设备型号",
            value = homepage.deviceName.ifEmpty { homepage.deviceModel }.ifBlank { "--" },
            showChevron = true,
            onClick = onToggleExpanded
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        CopyInfoRow("固件版本", firmwareVersion.ifBlank { "--" }, context)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        CopyInfoRow("IMEI", homepage.imei.ifBlank { "--" }, context)
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        CopyInfoRow("MAC 地址", homepage.mac.ifBlank { "--" }, context)

        AnimatedVisibility(expanded) {
            Column {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                InfoRow("硬件版本", hardwareVersion.ifBlank { "--" })
                InfoRow("发布日期", firmwareDate.ifBlank { "--" })
                CopyInfoRow("序列号", homepage.serialNumber.ifBlank { "--" }, context)
                CopyInfoRow("IMSI", homepage.imsi.ifBlank { "--" }, context)
                CopyInfoRow("ICCID", homepage.iccid.ifBlank { "--" }, context)
            }
        }
    }
}

@Composable
private fun SimManagementCard(
    simInfo: SimInfo,
    planEquipment: PlanEquipment?,
    isPlanLoading: Boolean,
    onSwitchSim: (String) -> Unit
) {
    ManagementCard(title = "SIM 卡管理", icon = { Icon(Icons.Default.SimCard, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }) {
        InfoRow("当前模式", if (simInfo.switchMode == "0") "智能选网" else "指定 SIM")
        if (isPlanLoading) {
            Text(
                text = "运营商与实名信息加载中…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        simInfo.simList.forEachIndexed { index, sim ->
            val planSim = planEquipment?.cardList?.find { it.iccid == sim.simIccid }
            val isCurrent = isCurrentSim(simInfo, sim, planEquipment)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${sim.simName.ifBlank { "SIM${index + 1}" }}（${planSim?.operatorText ?: sim.carrierName()}）",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal
                    )
                    if (sim.isPresent()) {
                        Text(
                            text = "ICCID: ${sim.simIccid}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "未插入",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    planSim?.realnameStatusText?.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = "实名: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                when {
                    !sim.isPresent() -> Text(
                        text = "未插入",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    isCurrent -> Text(
                        text = "当前使用",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    sim.isBanned() -> Text(
                        text = "已禁用",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                    else -> TextButton(onClick = { onSwitchSim(sim.simId) }) {
                        Text("切换")
                    }
                }
            }
            if (index != simInfo.simList.lastIndex) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        TextButton(onClick = { if (simInfo.switchMode != "0") onSwitchSim("4") }) {
            Text(
                text = if (simInfo.switchMode == "0") "✓ 智能选网" else "切换到智能选网",
                fontWeight = if (simInfo.switchMode == "0") FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun NetworkCard(
    wan: WanInfo,
    apn: String,
    onOpenModeSheet: () -> Unit
) {
    ManagementCard(title = "移动网络") {
        InfoRow("当前模式", wan.networkModeName())
        InfoRow("MTU", wan.mtu.ifBlank { "--" })
        InfoRow("APN", apn.ifBlank { "--" })

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onOpenModeSheet, modifier = Modifier.fillMaxWidth()) {
            Text("切换网络模式")
        }
    }
}

@Composable
private fun DhcpCard(dhcp: DhcpInfo) {
    ManagementCard(title = "DHCP 设置") {
        InfoRow("状态", if (dhcp.status == "1") "启用" else "禁用")
        InfoRow("IP 地址", dhcp.ip.ifBlank { "--" })
        InfoRow("子网掩码", dhcp.mask.ifBlank { "--" })
        InfoRow("地址池", "${dhcp.start.ifBlank { "--" }} - ${dhcp.end.ifBlank { "--" }}")
        InfoRow("租约时间", "${(dhcp.leaseTime.toLongOrNull() ?: 0) / 3600} 小时")
    }
}

@Composable
private fun ActionsCard(
    onChangePassword: () -> Unit,
    onRestart: () -> Unit,
    onLogout: () -> Unit
) {
    ManagementCard(title = "操作") {
        OutlinedButton(onClick = onChangePassword, modifier = Modifier.fillMaxWidth()) {
            Text("修改管理密码")
        }

        Button(onClick = onRestart, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.RestartAlt, contentDescription = null)
            Text("  重启设备")
        }

        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Text("  退出登录")
        }
    }
}

@Composable
private fun DangerZoneCard(onRestoreFactory: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = ErrorContainerLight.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.24f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
            Button(
                onClick = onRestoreFactory,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Icon(Icons.Default.PowerSettingsNew, contentDescription = null)
                Text("  恢复出厂设置")
            }
        }
    }
}

@Composable
private fun ManagementCard(
    title: String,
    icon: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceContainerLowLight
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                icon?.invoke()
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            content()
        }
    }
}

@Composable
private fun CopyInfoRow(label: String, value: String, context: Context) {
    KeyValueRow(
        label = label,
        value = value,
        onCopy = { if (value.isNotBlank() && value != "--") copyToClipboard(context, label, value) }
    )
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
    planEquipment: PlanEquipment?
): Boolean {
    val planSim = planEquipment?.cardList?.find { it.iccid == sim.simIccid }
    return simInfo.switchMode == "1" && sim.simId == simInfo.soleSimId || planSim?.isInUse() == true
}

private fun copyToClipboard(context: Context, label: String, value: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
}