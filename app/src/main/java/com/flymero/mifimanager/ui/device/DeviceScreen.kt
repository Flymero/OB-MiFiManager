package com.flymero.mifimanager.ui.device

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Logout
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.flymero.mifimanager.data.model.ApnProfile
import com.flymero.mifimanager.data.model.ApnProfileInfo
import com.flymero.mifimanager.data.model.DhcpInfo
import com.flymero.mifimanager.data.model.HomepageInfo
import com.flymero.mifimanager.data.model.PlanEquipment
import com.flymero.mifimanager.data.model.SimCard
import com.flymero.mifimanager.data.model.SimInfo
import com.flymero.mifimanager.data.model.WanInfo
import com.flymero.mifimanager.data.model.WlanMacFiltersInfo
import com.flymero.mifimanager.navigation.LocalGlobalSnackbar
import com.flymero.mifimanager.ui.components.InfoRow
import com.flymero.mifimanager.ui.components.KeyValueRow
import com.flymero.mifimanager.ui.theme.LocalThemeControl

private enum class DeviceSection(val title: String) {
    Overview("概览"),
    SimNetwork("SIM/网络"),
    Lan("局域网"),
    Security("安全"),
    System("系统")
}

private data class NetworkModeOption(val value: String, val label: String)
private data class SelectOption(val value: String, val label: String)

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
    var showApnEditor by remember { mutableStateOf(false) }
    var editingApn by remember { mutableStateOf<ApnProfile?>(null) }
    var selectedSection by remember { mutableStateOf(DeviceSection.Overview) }
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
        ) {
            CircularProgressIndicator()
        }
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
    val controlsEnabled = !state.operationInProgress

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                    enabled = !state.isRefreshing && controlsEnabled
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
        DeviceSectionTabs(selected = selectedSection, onSelected = { selectedSection = it })

        when (selectedSection) {
            DeviceSection.Overview -> {
                DeviceInfoCard(
                    homepage = homepage,
                    firmwareVersion = firmware.versionNum.ifEmpty { homepage.swVersion },
                    hardwareVersion = firmware.hwVersion.ifEmpty { homepage.hwVersion },
                    firmwareDate = firmware.versionDate,
                    expanded = deviceInfoExpanded,
                    context = context,
                    onToggleExpanded = { deviceInfoExpanded = !deviceInfoExpanded }
                )
                ConnectionSummaryCard(
                    wan = wan,
                    dhcp = dhcp,
                    apnInfo = apn,
                    simInfo = simInfo
                )
            }

            DeviceSection.SimNetwork -> {
                SimManagementCard(
                    simInfo = simInfo,
                    planEquipment = planEquipment,
                    isPlanLoading = state.isPlanLoading,
                    enabled = controlsEnabled,
                    onSwitchSim = viewModel::switchSim
                )
                NetworkCard(
                    wan = wan,
                    apn = apn.apn,
                    enabled = controlsEnabled,
                    onOpenModeSheet = { showNetworkModeSheet = true }
                )
                ApnManagementCard(
                    apnInfo = apn,
                    enabled = controlsEnabled,
                    onAdd = {
                        editingApn = null
                        showApnEditor = true
                    },
                    onEdit = {
                        editingApn = it
                        showApnEditor = true
                    },
                    onSetDefault = viewModel::setDefaultApn,
                    onDelete = viewModel::deleteApnProfile
                )
            }

            DeviceSection.Lan -> {
                DhcpCard(
                    dhcp = dhcp,
                    enabled = controlsEnabled,
                    dnsExpanded = dnsExpanded,
                    onToggleDns = { dnsExpanded = !dnsExpanded },
                    onSaveDns = viewModel::saveDns,
                    staticIpExpanded = staticIpExpanded,
                    onToggleStaticIp = { staticIpExpanded = !staticIpExpanded },
                    onAddStaticIp = { showAddStaticIpDialog = true },
                    onDeleteStaticIp = viewModel::deleteStaticIp
                )
            }

            DeviceSection.Security -> {
                MacBlacklistCard(
                    macFilters = state.macFiltersInfo,
                    isSyncing = state.isMacFilterSyncing,
                    enabled = controlsEnabled,
                    onToggleEnabled = viewModel::setMacBlacklistEnabled,
                    onAddMac = { showAddMacDialog = true },
                    onRemoveMac = viewModel::removeMacFromBlacklist
                )
            }

            DeviceSection.System -> {
                ActionsCard(
                    enabled = controlsEnabled,
                    onChangePassword = { showPasswordDialog = true },
                    onRestart = { showRestartDialog = true },
                    onLogout = viewModel::logout
                )
                DangerZoneCard(
                    enabled = controlsEnabled,
                    onRestoreFactory = { showResetDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
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
                        enabled = controlsEnabled,
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

    if (showApnEditor) {
        ApnEditorSheet(
            profile = editingApn,
            existingProfiles = apn.profileList,
            enabled = controlsEnabled,
            onDismiss = { showApnEditor = false },
            onSave = { profileName, apnValue, ipType, authType, username, password ->
                showApnEditor = false
                viewModel.saveApnProfile(profileName, apnValue, ipType, authType, username, password)
            }
        )
    }

    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text("重启设备") },
            text = { Text("确定要重启随身 WiFi 吗？重启期间网络将断开。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestartDialog = false
                        viewModel.restartDevice()
                    },
                    enabled = controlsEnabled
                ) { Text("确定") }
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
                TextButton(
                    onClick = {
                        showResetDialog = false
                        viewModel.restoreFactory()
                    },
                    enabled = controlsEnabled
                ) {
                    Text("确定", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showResetDialog = false }) { Text("取消") } }
        )
    }

    if (showPasswordDialog) {
        var newPwd by remember { mutableStateOf("") }
        var confirmPwd by remember { mutableStateOf("") }
        val passwordError = adminPasswordError(newPwd, confirmPwd)
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
                        isError = passwordError != null && newPwd.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = confirmPwd,
                        onValueChange = { confirmPwd = it },
                        label = { Text("确认新密码") },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = passwordError != null && confirmPwd.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    if (passwordError != null && (newPwd.isNotEmpty() || confirmPwd.isNotEmpty())) {
                        Text(
                            passwordError,
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
                    enabled = controlsEnabled && passwordError == null
                ) { Text("保存") }
            },
            dismissButton = { TextButton(onClick = { showPasswordDialog = false }) { Text("取消") } }
        )
    }

    if (showAddMacDialog) {
        var macAddress by remember { mutableStateOf("") }
        val macError = macBlacklistInputError(macAddress, state.macFiltersInfo)
        AlertDialog(
            onDismissRequest = { showAddMacDialog = false },
            title = { Text("添加黑名单 MAC") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = macAddress,
                        onValueChange = { macAddress = normalizeMacInput(it) },
                        label = { Text("MAC 地址") },
                        isError = macError != null && macAddress.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    if (macError != null && macAddress.isNotBlank()) {
                        Text(
                            text = macError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (state.macFiltersInfo.currentDeviceMac.isNotBlank()) {
                        Text(
                            text = "当前设备：${state.macFiltersInfo.currentDeviceMac}",
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
                    enabled = controlsEnabled && macError == null
                ) { Text("添加") }
            },
            dismissButton = { TextButton(onClick = { showAddMacDialog = false }) { Text("取消") } }
        )
    }

    if (showAddStaticIpDialog) {
        var staticMac by remember { mutableStateOf("") }
        var staticIp by remember(dhcp.ip) { mutableStateOf(defaultStaticIp(dhcp.ip)) }
        val macError = staticIpMacError(staticMac, dhcp)
        val ipError = staticIpAddressError(staticIp, dhcp)
        AlertDialog(
            onDismissRequest = { showAddStaticIpDialog = false },
            title = { Text("添加静态 IP 绑定") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = staticMac,
                        onValueChange = { staticMac = normalizeMacInput(it) },
                        label = { Text("MAC 地址") },
                        isError = macError != null && staticMac.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = staticIp,
                        onValueChange = { staticIp = it.trim() },
                        label = { Text("IP 地址") },
                        isError = ipError != null && staticIp.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    listOfNotNull(
                        if (staticMac.isNotBlank()) macError else null,
                        if (staticIp.isNotBlank()) ipError else null
                    ).firstOrNull()?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAddStaticIpDialog = false
                        viewModel.addStaticIp(normalizeMacAddress(staticMac), staticIp)
                    },
                    enabled = controlsEnabled && macError == null && ipError == null
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
        modifier = Modifier.fillMaxWidth(),
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
                Column(modifier = Modifier.weight(1f)) {
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
                if (state.operationInProgress || state.isRefreshing) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                }
            }
            if (state.operationInProgress || state.isRefreshing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun DeviceSectionTabs(
    selected: DeviceSection,
    onSelected: (DeviceSection) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DeviceSection.entries.forEach { section ->
            val isSelected = section == selected
            Surface(
                modifier = Modifier.clickable { onSelected(section) },
                shape = MaterialTheme.shapes.medium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                border = BorderStroke(
                    1.dp,
                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Text(
                    text = section.title,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1
                )
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
private fun ConnectionSummaryCard(
    wan: WanInfo,
    dhcp: DhcpInfo,
    apnInfo: ApnProfileInfo,
    simInfo: SimInfo
) {
    ManagementCard(title = "连接摘要") {
        InfoRow("SIM", currentSimLabel(simInfo))
        InfoRow("网络模式", wan.networkModeName())
        InfoRow("APN", apnInfo.apn.ifBlank { "--" })
        InfoRow("路由地址", dhcp.ip.ifBlank { "--" })
        InfoRow("DHCP", if (dhcp.status == "1") "启用" else "禁用")
    }
}

@Composable
private fun SimManagementCard(
    simInfo: SimInfo,
    planEquipment: PlanEquipment?,
    isPlanLoading: Boolean,
    enabled: Boolean,
    onSwitchSim: (String) -> Unit
) {
    ManagementCard(
        title = "SIM 卡管理",
        icon = { Icon(Icons.Default.SimCard, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
    ) {
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
            val isCurrent = isCurrentSim(simInfo, sim)
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
                    else -> TextButton(
                        onClick = { onSwitchSim(sim.simId) },
                        enabled = enabled
                    ) {
                        Text("切换")
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
                .then(
                    if (enabled && simInfo.switchMode != "0") Modifier.clickable { onSwitchSim("4") } else Modifier
                )
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "智能选网",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (simInfo.switchMode == "0") FontWeight.SemiBold else FontWeight.Normal
            )
            if (simInfo.switchMode == "0") {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "当前模式",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun NetworkCard(
    wan: WanInfo,
    apn: String,
    enabled: Boolean,
    onOpenModeSheet: () -> Unit
) {
    ManagementCard(title = "移动网络") {
        InfoRow("当前模式", wan.networkModeName())
        InfoRow("MTU", wan.mtu.ifBlank { "--" })
        InfoRow("APN", apn.ifBlank { "--" })

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onOpenModeSheet,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        ) {
            Text("切换网络模式")
        }
    }
}

@Composable
private fun ApnManagementCard(
    apnInfo: ApnProfileInfo,
    enabled: Boolean,
    onAdd: () -> Unit,
    onEdit: (ApnProfile) -> Unit,
    onSetDefault: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    ManagementCard(title = "APN 管理") {
        InfoRow("当前 APN", apnInfo.apn.ifBlank { "--" })
        InfoRow("自动 APN", if (apnInfo.autoApn == "1") "开启" else "关闭")

        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onAdd,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text("  新增 APN")
        }

        if (apnInfo.profileList.isEmpty()) {
            Text(
                text = "暂无 APN 配置",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
            return@ManagementCard
        }

        apnInfo.profileList.forEachIndexed { index, profile ->
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
            ApnProfileRow(
                profile = profile,
                enabled = enabled,
                onEdit = { onEdit(profile) },
                onSetDefault = { onSetDefault(profile.profileName) },
                onDelete = { onDelete(profile.profileName) }
            )
            if (index == apnInfo.profileList.lastIndex) {
                Spacer(modifier = Modifier.height(2.dp))
            }
        }
    }
}

@Composable
private fun ApnProfileRow(
    profile: ApnProfile,
    enabled: Boolean,
    onEdit: () -> Unit,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.displayName(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (profile.isEnabled()) FontWeight.SemiBold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = listOf(
                        profile.apn.ifBlank { "--" },
                        apnIpTypeLabel(profile.ipType),
                        authTypeLabel(profile.authType())
                    ).joinToString(" / "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                if (profile.isEnabled()) {
                    Text(
                        text = "当前",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (profile.isDefault()) {
                    Text(
                        text = "默认",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onEdit, enabled = enabled) {
                Text("编辑")
            }
            TextButton(
                onClick = onSetDefault,
                enabled = enabled && !profile.isEnabled()
            ) {
                Text("设为当前")
            }
            TextButton(
                onClick = onDelete,
                enabled = enabled && !profile.isEnabled() && !profile.isDefault()
            ) {
                Text("删除")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApnEditorSheet(
    profile: ApnProfile?,
    existingProfiles: List<ApnProfile>,
    enabled: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String) -> Unit
) {
    var profileName by remember(profile?.profileName) { mutableStateOf(profile?.profileName.orEmpty()) }
    var apnValue by remember(profile?.apn) { mutableStateOf(profile?.apn.orEmpty()) }
    var ipType by remember(profile?.ipType) { mutableStateOf(profile?.ipType?.ifBlank { "1" } ?: "1") }
    var authType by remember(profile?.authType()) { mutableStateOf(profile?.authType()?.ifBlank { "NONE" } ?: "NONE") }
    var username by remember(profile?.username()) { mutableStateOf(profile?.username().orEmpty()) }
    var password by remember(profile?.password()) { mutableStateOf(profile?.password().orEmpty()) }
    val validationError = apnProfileInputError(
        profileName = profileName,
        apn = apnValue,
        authType = authType,
        username = username,
        originalName = profile?.profileName,
        existingProfiles = existingProfiles
    )

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 640.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (profile == null) "新增 APN" else "编辑 APN",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
                value = profileName,
                onValueChange = { profileName = it.trim() },
                label = { Text("配置名称") },
                enabled = enabled && profile == null,
                isError = validationError != null && profileName.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = apnValue,
                onValueChange = { apnValue = it.trim() },
                label = { Text("APN") },
                enabled = enabled,
                isError = validationError != null && apnValue.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OptionSelector(
                label = "IP 类型",
                value = ipType,
                options = apnIpTypeOptions(ipType),
                enabled = enabled,
                onSelected = { ipType = it }
            )
            OptionSelector(
                label = "认证方式",
                value = authType,
                options = authTypeOptions(authType),
                enabled = enabled,
                onSelected = { authType = it }
            )
            AnimatedVisibility(authType != "NONE") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it.trim() },
                        label = { Text("用户名") },
                        enabled = enabled,
                        isError = validationError != null && username.isBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("密码") },
                        enabled = enabled,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
            if (validationError != null && (profileName.isNotBlank() || apnValue.isNotBlank())) {
                Text(
                    text = validationError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("取消")
                }
                Button(
                    onClick = {
                        onSave(
                            profileName.trim(),
                            apnValue.trim(),
                            ipType,
                            authType,
                            if (authType == "NONE") "" else username.trim(),
                            if (authType == "NONE") "" else password
                        )
                    },
                    enabled = enabled && validationError == null,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("保存")
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OptionSelector(
    label: String,
    value: String,
    options: List<SelectOption>,
    enabled: Boolean,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currentLabel = options.firstOrNull { it.value == value }?.label ?: value.ifBlank { "--" }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it }
    ) {
        OutlinedTextField(
            value = currentLabel,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            singleLine = true
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSelected(option.value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DhcpCard(
    dhcp: DhcpInfo,
    enabled: Boolean,
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
    val dns1Error = dnsInputError(dns1, required = true)
    val dns2Error = dnsInputError(dns2, required = false)

    ManagementCard(title = "局域网与 DHCP") {
        InfoRow("状态", if (dhcp.status == "1") "启用" else "禁用")
        InfoRow("IP 地址", dhcp.ip.ifBlank { "--" })
        InfoRow("子网掩码", dhcp.mask.ifBlank { "--" })
        InfoRow("地址池", "${dhcp.start.ifBlank { "--" }} - ${dhcp.end.ifBlank { "--" }}")
        InfoRow("租约时间", "${(dhcp.leaseTime.toLongOrNull() ?: 0) / 3600} 小时")
        InfoRow("DHCPv6", if (dhcp.dhcpv6Server == "1") "启用" else "禁用")

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
                    onValueChange = { dns1 = it.trim() },
                    label = { Text("DNS 1") },
                    isError = dns1Error != null && dns1.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = dns2,
                    onValueChange = { dns2 = it.trim() },
                    label = { Text("DNS 2") },
                    isError = dns2Error != null && dns2.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                listOfNotNull(
                    if (dns1.isNotBlank()) dns1Error else null,
                    if (dns2.isNotBlank()) dns2Error else null
                ).firstOrNull()?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onSaveDns(false, "", "") },
                        modifier = Modifier.weight(1f),
                        enabled = enabled
                    ) {
                        Text("恢复默认")
                    }
                    Button(
                        onClick = { onSaveDns(true, dns1, dns2) },
                        modifier = Modifier.weight(1f),
                        enabled = enabled && dns1Error == null && dns2Error == null
                    ) {
                        Text("保存")
                    }
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
                            TextButton(
                                onClick = { onDeleteStaticIp(entry.mac) },
                                enabled = enabled
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "删除")
                            }
                        }
                        HorizontalDivider()
                    }
                }
                OutlinedButton(
                    onClick = onAddStaticIp,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled
                ) {
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
    enabled: Boolean,
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
                enabled = enabled && !isSyncing
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
                        enabled = enabled && !isSyncing
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
            enabled = enabled && !isSyncing
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text("  手动添加")
        }
    }
}

@Composable
private fun ActionsCard(
    enabled: Boolean,
    onChangePassword: () -> Unit,
    onRestart: () -> Unit,
    onLogout: () -> Unit
) {
    ManagementCard(title = "系统操作") {
        OutlinedButton(
            onClick = onChangePassword,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        ) {
            Text("修改管理密码")
        }

        Button(
            onClick = onRestart,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        ) {
            Icon(Icons.Default.RestartAlt, contentDescription = null)
            Text("  重启设备")
        }

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Text("  退出登录")
        }
    }
}

@Composable
private fun DangerZoneCard(
    enabled: Boolean,
    onRestoreFactory: () -> Unit
) {
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
                enabled = enabled,
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

private fun apnIpTypeOptions(currentValue: String): List<SelectOption> {
    val options = listOf(
        SelectOption("1", "IPv4"),
        SelectOption("2", "IPv6"),
        SelectOption("3", "IPv4/IPv6")
    )
    return if (currentValue.isNotBlank() && options.none { it.value == currentValue }) {
        options + SelectOption(currentValue, apnIpTypeLabel(currentValue))
    } else {
        options
    }
}

private fun authTypeOptions(currentValue: String): List<SelectOption> {
    val options = listOf(
        SelectOption("NONE", "无"),
        SelectOption("PAP", "PAP"),
        SelectOption("CHAP", "CHAP")
    )
    return if (currentValue.isNotBlank() && options.none { it.value == currentValue }) {
        options + SelectOption(currentValue, currentValue)
    } else {
        options
    }
}

private fun apnIpTypeLabel(value: String): String = when (value) {
    "0", "3" -> "IPv4/IPv6"
    "1" -> "IPv4"
    "2" -> "IPv6"
    else -> "模式 $value"
}

private fun authTypeLabel(value: String): String = when (value.uppercase()) {
    "NONE", "" -> "无认证"
    "PAP" -> "PAP"
    "CHAP" -> "CHAP"
    else -> value
}

private fun currentSimLabel(simInfo: SimInfo): String {
    if (simInfo.switchMode == "0") return "智能选网"
    val current = simInfo.simList.firstOrNull { isCurrentSim(simInfo, it) }
    return current?.simName?.ifBlank { "SIM ${current.simId}" } ?: "--"
}

private fun adminPasswordError(newPassword: String, confirmPassword: String): String? = when {
    newPassword.isBlank() -> "请输入新密码"
    newPassword.length !in 4..32 -> "密码长度需为 4-32 位"
    newPassword.any { it.isWhitespace() } -> "密码不能包含空格"
    confirmPassword.isBlank() -> "请再次输入密码"
    newPassword != confirmPassword -> "两次密码不一致"
    else -> null
}

private fun apnProfileInputError(
    profileName: String,
    apn: String,
    authType: String,
    username: String,
    originalName: String?,
    existingProfiles: List<ApnProfile>
): String? {
    val trimmedName = profileName.trim()
    val trimmedApn = apn.trim()
    val duplicateName = existingProfiles.any {
        it.profileName.equals(trimmedName, ignoreCase = true) &&
            !it.profileName.equals(originalName.orEmpty(), ignoreCase = true)
    }
    return when {
        trimmedName.isBlank() -> "请输入配置名称"
        trimmedName.length > 32 -> "配置名称不能超过 32 个字符"
        !APN_PROFILE_NAME_REGEX.matches(trimmedName) -> "配置名称只能包含字母、数字、点、横线和下划线"
        duplicateName -> "配置名称已存在"
        trimmedApn.isBlank() -> "请输入 APN"
        trimmedApn.length > 64 -> "APN 不能超过 64 个字符"
        !APN_VALUE_REGEX.matches(trimmedApn) -> "APN 包含不支持的字符"
        authType != "NONE" && username.isBlank() -> "请输入认证用户名"
        else -> null
    }
}

private fun dnsInputError(value: String, required: Boolean): String? = when {
    value.isBlank() && required -> "请输入 DNS 地址"
    value.isBlank() -> null
    !isValidIpv4Address(value) -> "请输入有效 IPv4 地址"
    else -> null
}

private fun staticIpMacError(value: String, dhcp: DhcpInfo): String? {
    val normalized = normalizeMacAddress(value)
    return when {
        value.isBlank() -> "请输入 MAC 地址"
        !isValidMacAddress(value) -> "请输入有效 MAC 地址"
        dhcp.fixedIpList.any { normalizeMacAddress(it.mac) == normalized } -> "该 MAC 已存在绑定"
        else -> null
    }
}

private fun staticIpAddressError(value: String, dhcp: DhcpInfo): String? {
    val trimmed = value.trim()
    return when {
        trimmed.isBlank() -> "请输入 IP 地址"
        !isValidIpv4Address(trimmed) -> "请输入有效 IPv4 地址"
        !isUsableHostAddress(trimmed) -> "IP 末段需在 1-254 之间"
        trimmed == dhcp.ip -> "不能与路由器地址相同"
        dhcp.fixedIpList.any { it.ip == trimmed } -> "该 IP 已存在绑定"
        !isSameSubnet(trimmed, dhcp.ip, dhcp.mask) -> "IP 需与路由器处于同一网段"
        else -> null
    }
}

private fun macBlacklistInputError(value: String, macFilters: WlanMacFiltersInfo): String? {
    val normalized = normalizeMacAddress(value)
    return when {
        value.isBlank() -> "请输入 MAC 地址"
        !isValidMacAddress(value) -> "请输入有效 MAC 地址"
        normalized == macFilters.normalizedCurrentDeviceMac() -> "不能添加当前设备"
        macFilters.blacklistEntries().any { normalizeMacAddress(it.mac) == normalized } -> "该 MAC 已在黑名单中"
        else -> null
    }
}

private fun defaultStaticIp(gateway: String): String {
    val parts = gateway.split('.')
    return if (parts.size == 4) parts.take(3).joinToString(".") + "." else ""
}

private fun isSameSubnet(ip: String, gateway: String, mask: String): Boolean {
    if (!isValidIpv4Address(gateway)) return true
    if (mask.ifBlank { "255.255.255.0" } != "255.255.255.0") return true
    return ip.split('.').take(3) == gateway.split('.').take(3)
}

private fun isUsableHostAddress(value: String): Boolean =
    value.split('.').lastOrNull()?.toIntOrNull() in 1..254

private fun isValidIpv4Address(value: String): Boolean {
    val parts = value.trim().split('.')
    return parts.size == 4 && parts.all { part ->
        part.isNotEmpty() && part.length <= 3 && part.all { it.isDigit() } && part.toInt() in 0..255
    }
}

private fun normalizeMacInput(value: String): String {
    val raw = value.filter { it.isHexChar() }.take(12).uppercase()
    return raw.chunked(2).joinToString(":")
}

private fun normalizeMacAddress(value: String): String {
    val raw = value.filter { it.isHexChar() }.take(12).uppercase()
    return if (raw.length == 12) raw.chunked(2).joinToString(":") else value.trim().replace('-', ':').uppercase()
}

private fun Char.isHexChar(): Boolean =
    this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'

private val APN_PROFILE_NAME_REGEX = Regex("^[A-Za-z0-9_.-]+$")
private val APN_VALUE_REGEX = Regex("^[A-Za-z0-9*#._-]+$")
private val MAC_ADDRESS_REGEX = Regex("^([0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}$")

private fun isValidMacAddress(value: String): Boolean =
    MAC_ADDRESS_REGEX.matches(normalizeMacAddress(value))

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
