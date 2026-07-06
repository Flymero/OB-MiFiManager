package com.flymero.mifimanager.ui.device

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.flymero.mifimanager.data.model.DhcpInfo
import com.flymero.mifimanager.data.model.HomepageInfo
import com.flymero.mifimanager.data.model.PlanEquipment
import com.flymero.mifimanager.data.model.PlanSimCard
import com.flymero.mifimanager.data.model.SimCard
import com.flymero.mifimanager.data.model.SimInfo
import com.flymero.mifimanager.data.model.WanInfo
import com.flymero.mifimanager.data.model.WlanMacFiltersInfo
import com.flymero.mifimanager.navigation.LocalGlobalSnackbar
import com.flymero.mifimanager.ui.components.InfoRow
import com.flymero.mifimanager.ui.components.KeyValueRow
import com.flymero.mifimanager.ui.theme.LocalThemeControl
import com.flymero.mifimanager.ui.util.formatCarrierName

private data class NetworkModeOption(val value: String, val label: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceScreen(
    onLogout: () -> Unit = {},
    viewModel: DeviceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = LocalGlobalSnackbar.current
    val context = LocalContext.current
    var showRestartDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showNetworkModeSheet by remember { mutableStateOf(false) }
    var showAddMacDialog by remember { mutableStateOf(false) }
    var showAddStaticIpDialog by remember { mutableStateOf(false) }
    var deviceInfoExpanded by remember { mutableStateOf(false) }
    var dnsExpanded by remember { mutableStateOf(false) }
    var staticIpExpanded by remember { mutableStateOf(false) }

    LifecycleResumeEffect(Unit) {
        viewModel.onResume()
        onPauseOrDispose { }
    }

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
    val networkOptions = networkModeOptions(wan.nwModeDefault)

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val themeControl = LocalThemeControl.current
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
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = viewModel::refresh,
                        enabled = !state.isRefreshing && !state.operationInProgress
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                    IconButton(onClick = themeControl.toggle) {
                        Icon(
                            imageVector = if (themeControl.isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (themeControl.isDark) "切换到浅色模式" else "切换到深色模式"
                        )
                    }
                }
            }

            RouterStatusBanner(state = state)

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
                homepageNetworkName = homepage.networkName,
                isPlanLoading = state.isPlanLoading,
                onSwitchSim = viewModel::switchSim
            )

            NetworkCard(
                wan = wan,
                apn = apn.apn,
                onOpenModeSheet = { showNetworkModeSheet = true }
            )

            DhcpCard(
                dhcp = dhcp,
                dnsExpanded = dnsExpanded,
                onToggleDns = { dnsExpanded = !dnsExpanded },
                onSaveDns = viewModel::saveDns,
                staticIpExpanded = staticIpExpanded,
                onToggleStaticIp = { staticIpExpanded = !staticIpExpanded },
                onAddStaticIp = { showAddStaticIpDialog = true },
                onDeleteStaticIp = viewModel::deleteStaticIp
            )

            MacBlacklistCard(
                macFilters = state.macFiltersInfo,
                isSyncing = state.isMacFilterSyncing,
                onToggleEnabled = viewModel::setMacBlacklistEnabled,
                onAddMac = { showAddMacDialog = true },
                onRemoveMac = viewModel::removeMacFromBlacklist
            )

            ActionsCard(
                onChangePassword = { showPasswordDialog = true },
                onRestart = { showRestartDialog = true },
                onLogout = viewModel::logout
            )

            DangerZoneCard(onRestoreFactory = { showResetDialog = true })

            Spacer(modifier = Modifier.height(24.dp))
        }
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

    if (showAddMacDialog) {
        var macAddress by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddMacDialog = false },
            title = { Text("添加黑名单 MAC") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = macAddress,
                        onValueChange = { macAddress = it.uppercase().replace('，', ',') },
                        label = { Text("MAC 地址") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text(
                        text = "示例：AA:BB:CC:DD:EE:FF",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (state.macFiltersInfo.currentDeviceMac.isNotBlank()) {
                        Text(
                            text = "当前设备 MAC：${state.macFiltersInfo.currentDeviceMac}，不会允许加入黑名单",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAddMacDialog = false
                        viewModel.addMacToBlacklist(macAddress)
                    },
                    enabled = isValidMacAddress(macAddress)
                ) { Text("添加") }
            },
            dismissButton = { TextButton(onClick = { showAddMacDialog = false }) { Text("取消") } }
        )
    }

    if (showAddStaticIpDialog) {
        var staticMac by remember { mutableStateOf("") }
        var staticIp by remember { mutableStateOf("192.168.1.") }
        AlertDialog(
            onDismissRequest = { showAddStaticIpDialog = false },
            title = { Text("添加静态 IP 绑定") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = staticMac,
                        onValueChange = { staticMac = it.uppercase() },
                        label = { Text("MAC 地址") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = staticIp,
                        onValueChange = { staticIp = it },
                        label = { Text("IP 地址") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAddStaticIpDialog = false
                        viewModel.addStaticIp(staticMac, staticIp)
                    },
                    enabled = isValidMacAddress(staticMac) && staticIp.count { it == '.' } == 3
                ) { Text("添加") }
            },
            dismissButton = { TextButton(onClick = { showAddStaticIpDialog = false }) { Text("取消") } }
        )
    }
}

@Composable
private fun RouterStatusBanner(state: DeviceState) {
    val title: String
    val detail: String
    val containerColor = when {
        state.operationInProgress -> MaterialTheme.colorScheme.primaryContainer
        state.operationMessage != null -> MaterialTheme.colorScheme.secondaryContainer
        !state.routerReachable -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    when {
        state.operationInProgress -> {
            title = state.operationMessage ?: "正在同步"
            detail = if (state.operationAffectsConnection) "网络可能会短暂断开" else "请稍候"
        }
        state.operationMessage != null -> {
            title = state.operationMessage
            detail = if (state.operationAffectsConnection) "等待设备恢复连接" else "状态将在刷新后更新"
        }
        state.isRefreshing -> {
            title = "正在刷新后台状态"
            detail = "设备信息会保留到新的状态返回"
        }
        !state.routerReachable -> {
            title = if (state.hasLoadedRouter) "后台暂时不可达" else "未连接到设备后台"
            detail = "请确认已连接随身 WiFi"
        }
        else -> {
            title = "后台已连接"
            detail = "设备状态可正常读取"
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(98.dp),
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier.size(22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.operationInProgress || state.isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
            ) {
                if (state.operationInProgress || state.isRefreshing) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        }
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
    homepageNetworkName: String,
    isPlanLoading: Boolean,
    onSwitchSim: (String) -> Unit
) {
    ManagementCard(title = "SIM 卡管理", icon = { Icon(Icons.Default.SimCard, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }) {
        InfoRow("当前模式", if (simInfo.switchMode == "0") "智能选网" else "指定 SIM")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Crossfade(targetState = isPlanLoading, label = "sim-plan-loading") { loading ->
                Text(
                    text = if (loading) "运营商与实名信息加载中…" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        simInfo.simList.forEachIndexed { index, sim ->
            val planSim = findPlanSimCard(simInfo, sim, planEquipment)
            val isCurrent = isCurrentSim(simInfo, sim)
            val carrierText = simCarrierText(
                sim = sim,
                planSim = planSim,
                isPlanLoading = isPlanLoading,
                networkCarrierName = homepageNetworkName,
                useNetworkCarrierFallback = isNetworkCarrierFallbackSim(simInfo, sim)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Crossfade(targetState = carrierText, label = "sim-carrier-$index") { carrier ->
                        Text(
                            text = "${sim.simName.ifBlank { "SIM${index + 1}" }}（$carrier）",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Crossfade(
                            targetState = planSim?.realnameStatusText?.takeIf { it.isNotBlank() }.orEmpty(),
                            label = "sim-realname-$index"
                        ) { realname ->
                            Text(
                                text = if (realname.isNotBlank()) "实名: $realname" else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier.width(76.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        !sim.isPresent() -> Text(
                            text = "未插入",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        isCurrent -> Icon(
                            Icons.Default.Check,
                            contentDescription = "当前选择",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
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
            }
            if (index != simInfo.simList.lastIndex) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "智能选网",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (simInfo.switchMode == "0") FontWeight.SemiBold else FontWeight.Normal
            )
            Box(
                modifier = Modifier.width(76.dp),
                contentAlignment = Alignment.Center
            ) {
                if (simInfo.switchMode == "0") {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "当前模式",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    TextButton(onClick = { onSwitchSim("4") }) {
                        Text("切换")
                    }
                }
            }
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
private fun DhcpCard(
    dhcp: DhcpInfo,
    dnsExpanded: Boolean,
    onToggleDns: () -> Unit,
    onSaveDns: (Boolean, String, String) -> Unit,
    staticIpExpanded: Boolean,
    onToggleStaticIp: () -> Unit,
    onAddStaticIp: () -> Unit,
    onDeleteStaticIp: (String) -> Unit
) {
    var dns1 by remember(dhcp.dns1) { mutableStateOf(dhcp.dns1) }
    var dns2 by remember(dhcp.dns2) { mutableStateOf(dhcp.dns2) }

    ManagementCard(title = "DHCP 设置") {
        InfoRow("状态", if (dhcp.status == "1") "启用" else "禁用")
        InfoRow("IP 地址", dhcp.ip.ifBlank { "--" })
        InfoRow("子网掩码", dhcp.mask.ifBlank { "--" })
        InfoRow("地址池", "${dhcp.start.ifBlank { "--" }} - ${dhcp.end.ifBlank { "--" }}")
        InfoRow("租约时间", "${(dhcp.leaseTime.toLongOrNull() ?: 0) / 3600} 小时")

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleDns)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("自定义 DNS", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Icon(
                if (dnsExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        AnimatedVisibility(dnsExpanded) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (dhcp.dnsEnable == "1") "已启用自定义 DNS" else "使用运营商默认 DNS",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = dns1,
                    onValueChange = { dns1 = it },
                    label = { Text("DNS 1") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = dns2,
                    onValueChange = { dns2 = it },
                    label = { Text("DNS 2") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onSaveDns(false, "", "") },
                        modifier = Modifier.weight(1f)
                    ) { Text("恢复默认") }
                    Button(
                        onClick = { onSaveDns(true, dns1, dns2) },
                        modifier = Modifier.weight(1f),
                        enabled = dns1.isNotBlank()
                    ) { Text("保存") }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleStaticIp)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("静态 IP 绑定", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Icon(
                if (staticIpExpanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        AnimatedVisibility(staticIpExpanded) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (dhcp.fixedIpList.isEmpty()) {
                    Text(
                        text = "暂无静态 IP 绑定",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    dhcp.fixedIpList.forEach { entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.mac, style = MaterialTheme.typography.bodySmall)
                                Text(entry.ip, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            }
                            TextButton(onClick = { onDeleteStaticIp(entry.mac) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = null)
                            }
                        }
                        HorizontalDivider()
                    }
                }
                OutlinedButton(onClick = onAddStaticIp, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("  添加绑定")
                }
            }
        }
    }
}

@Composable
private fun MacBlacklistCard(
    macFilters: WlanMacFiltersInfo,
    isSyncing: Boolean,
    onToggleEnabled: (Boolean) -> Unit,
    onAddMac: () -> Unit,
    onRemoveMac: (Int) -> Unit
) {
    ManagementCard(title = "MAC 黑名单管理") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "总开关",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = when {
                        isSyncing -> "重连后正在同步…"
                        macFilters.isEnabled() -> "已启用"
                        else -> "未启用"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = macFilters.isEnabled(),
                onCheckedChange = onToggleEnabled,
                enabled = !isSyncing
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        InfoRow("过滤模式", "黑名单模式")

        if (isSyncing) {
            Text(
                text = "已提交到路由器，重连后会自动刷新这里的状态。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        } else if (!macFilters.isEnabled()) {
            Text(
                text = "当前未生效，开启后才会按黑名单拦截。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "已加入的 MAC 列表",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )

        if (macFilters.blacklistEntries().isEmpty()) {
            Text(
                text = if (isSyncing) "列表同步中…" else "暂无黑名单 MAC",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
        } else {
            macFilters.blacklistEntries().forEachIndexed { index, entry ->
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry.mac.ifBlank { "--" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { onRemoveMac(index) },
                        enabled = !isSyncing
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null)
                        Text(" 删除")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onAddMac,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSyncing
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text("  手动添加")
        }
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
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Text("  退出登录")
        }
    }
}

@Composable
private fun DangerZoneCard(onRestoreFactory: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.92f),
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
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
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
        valueMaxLines = 2,
        onCopy = { if (value.isNotBlank() && value != "--") copyToClipboard(context, label, value) }
    )
}

private fun networkModeOptions(defaultMode: String): List<NetworkModeOption> = when (defaultMode) {
    "2" -> listOf(
        NetworkModeOption("2", "仅 4G")
    )
    "3" -> listOf(
        NetworkModeOption("2", "仅 4G"),
        NetworkModeOption("3", "4G/3G 自动"),
        NetworkModeOption("5", "仅 3G")
    )
    "8" -> listOf(
        NetworkModeOption("2", "仅 4G"),
        NetworkModeOption("8", "4G/2G 自动"),
        NetworkModeOption("6", "仅 2G")
    )
    "4" -> listOf(
        NetworkModeOption("5", "仅 3G"),
        NetworkModeOption("4", "3G/2G 自动"),
        NetworkModeOption("6", "仅 2G")
    )
    "5" -> listOf(
        NetworkModeOption("5", "仅 3G")
    )
    "6" -> listOf(
        NetworkModeOption("6", "仅 2G")
    )
    "7" -> listOf(
        NetworkModeOption("7", "关闭移动网络")
    )
    else -> listOf(
        NetworkModeOption("1", "自动"),
        NetworkModeOption("2", "仅 4G"),
        NetworkModeOption("3", "4G/3G 自动"),
        NetworkModeOption("5", "仅 3G")
    )
}

private val MAC_ADDRESS_REGEX = Regex("^([0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}$")

private fun isValidMacAddress(value: String): Boolean =
    MAC_ADDRESS_REGEX.matches(value.trim())

private fun findPlanSimCard(
    simInfo: SimInfo,
    sim: SimCard,
    planEquipment: PlanEquipment?
): PlanSimCard? {
    val iccidCandidates = buildList {
        add(sim.simIccid)
        if (sim.simId == simInfo.currentSimId || sim.simId == simInfo.soleSimId) {
            add(simInfo.virtualIccid)
        }
    }.map(::normalizeIccid)
        .filter { it.isNotEmpty() }
        .distinct()

    if (iccidCandidates.isEmpty()) return null

    return planEquipment?.cardList?.firstOrNull { planSim ->
        val planIccid = normalizeIccid(planSim.iccid)
        iccidCandidates.any { candidate -> candidate.matchesIccid(planIccid) }
    }
}

private fun simCarrierText(
    sim: SimCard,
    planSim: PlanSimCard?,
    isPlanLoading: Boolean,
    networkCarrierName: String,
    useNetworkCarrierFallback: Boolean
): String {
    if (!sim.isPresent()) return "未插入"

    val planCarrier = formatCarrierName(planSim?.operatorText.orEmpty())
    if (planCarrier.isNotBlank()) return planCarrier

    val networkCarrier = if (useNetworkCarrierFallback) {
        formatCarrierName(networkCarrierName)
    } else {
        ""
    }
    if (networkCarrier.isNotBlank()) return networkCarrier

    if (isPlanLoading && planSim == null) return "识别中"

    return "未知运营商"
}

private fun normalizeIccid(value: String): String =
    value.filter { it.isDigit() }

private fun String.matchesIccid(other: String): Boolean {
    if (isEmpty() || other.isEmpty()) return false
    if (this == other) return true

    val shorterLength = minOf(length, other.length)
    return shorterLength >= 12 && (endsWith(other) || other.endsWith(this))
}

private fun isNetworkCarrierFallbackSim(
    simInfo: SimInfo,
    sim: SimCard
): Boolean {
    if (!sim.isPresent()) return false
    return sim.simId == simInfo.currentSimId || sim.simId == simInfo.soleSimId
}

private fun isCurrentSim(
    simInfo: SimInfo,
    sim: SimCard
): Boolean {
    return simInfo.switchMode == "1" && sim.simId == simInfo.soleSimId
}

private fun copyToClipboard(context: Context, label: String, value: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
    Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
}
