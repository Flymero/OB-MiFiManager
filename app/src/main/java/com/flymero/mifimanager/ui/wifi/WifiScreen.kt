package com.flymero.mifimanager.ui.wifi

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flymero.mifimanager.data.model.WpsInfo
import com.flymero.mifimanager.ui.components.CardTitle
import com.flymero.mifimanager.ui.components.KeyValueRow
import com.flymero.mifimanager.ui.components.SectionCard
import com.flymero.mifimanager.ui.components.SectionDivider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiScreen(viewModel: WifiViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var ssid by remember(state.securityInfo) { mutableStateOf(state.securityInfo.decodedSsid()) }
    var password by remember(state.securityInfo) { mutableStateOf(state.securityInfo.currentKey()) }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedMode by remember(state.securityInfo) { mutableStateOf(state.securityInfo.mode) }
    var wifiEnabled by remember(state.wlanInfo) { mutableStateOf(state.wlanInfo.wlanEnable == "1") }
    var apIsolate by remember(state.wlanInfo) { mutableStateOf(state.wlanInfo.apIsolate == "1") }
    var ssidBroadcast by remember(state.securityInfo) { mutableStateOf(state.securityInfo.ssidBcast == "1") }
    var autoOffEnabled by remember(state.wlanInfo) { mutableStateOf(state.wlanInfo.wifiSleepTime != "0") }
    var autoOffMinutes by remember(state.wlanInfo) { mutableStateOf(state.wlanInfo.wifiSleepTime.takeUnless { it == "0" }.orEmpty()) }
    var autoChannel by remember(state.wlanInfo) { mutableStateOf(state.wlanInfo.bandwidthAcsOrDefault() == "1") }
    var selectedChannel by remember(state.wlanInfo) { mutableStateOf(state.wlanInfo.channel) }
    var selectedBandwidth by remember(state.wlanInfo) { mutableStateOf(state.wlanInfo.bandwidth) }
    var maxClients by remember(state.wlanInfo) { mutableStateOf(state.wlanInfo.maxClients) }
    var wpsPin by remember { mutableStateOf("") }
    var showSecurityModeSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.saveResult) {
        state.saveResult?.let {
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

    val securityModes = securityModeOptions()
    val autoOffError = autoOffMinutesError(autoOffEnabled, autoOffMinutes)

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "WiFi 管理",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )

            SectionCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "WiFi 开关",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "控制无线网络广播",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = wifiEnabled, onCheckedChange = { wifiEnabled = it })
                }
            }

            SectionCard {
                CardTitle("网络设置")
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = ssid,
                        onValueChange = { ssid = it },
                        label = { Text("SSID（网络名称）") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.large
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("密码") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.large,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                    OutlinedButton(
                        onClick = { showSecurityModeSheet = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text("加密方式：${securityModeLabel(selectedMode)}")
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "隐藏 SSID",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Switch(checked = !ssidBroadcast, onCheckedChange = { ssidBroadcast = !it })
                    }

                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "将按管理页真实参数格式保存 WiFi 设置",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.saveSecurity(ssid, password, selectedMode, ssidBroadcast) },
                        enabled = !state.isSaving,
                        modifier = Modifier.align(Alignment.End),
                        shape = CircleShape
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                        Text("保存设置")
                    }
                }
            }

            SectionCard {
                CardTitle("高级设置")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "自动信道",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (autoChannel) "路由器自动选择最佳信道" else "手动指定信道和带宽",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = autoChannel, onCheckedChange = { autoChannel = it })
                }
                if (!autoChannel) {
                    SectionDivider()
                    ChannelSelector(
                        selectedChannel = selectedChannel,
                        firstChannel = state.wlanInfo.firstChannel.toIntOrNull() ?: 1,
                        lastChannel = state.wlanInfo.lastChannel.toIntOrNull() ?: 13,
                        onChannelSelected = { selectedChannel = it }
                    )
                    SectionDivider()
                    BandwidthSelector(
                        selectedBandwidth = selectedBandwidth,
                        onBandwidthSelected = { selectedBandwidth = it }
                    )
                }
                SectionDivider()
                OutlinedTextField(
                    value = maxClients,
                    onValueChange = { maxClients = it.filter(Char::isDigit).take(2) },
                    label = { Text("最大连接数") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = { Text("建议 1-32") },
                    shape = MaterialTheme.shapes.large
                )
                SectionDivider()
                KeyValueRow("Beacon 周期", "${state.wlanInfo.beaconPeriod} ms")
                SectionDivider()
                KeyValueRow("DTIM 间隔", state.wlanInfo.dtimInterval)
                SectionDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AP 隔离",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Switch(checked = apIsolate, onCheckedChange = { apIsolate = it })
                }
                Button(
                    onClick = {
                        viewModel.saveWlanSettings(
                            wlanEnable = wifiEnabled,
                            apIsolate = apIsolate,
                            bandwidthAcs = autoChannel,
                            channel = selectedChannel,
                            bandwidth = selectedBandwidth,
                            maxClients = maxClients
                        )
                    },
                    enabled = !state.isSaving,
                    modifier = Modifier.align(Alignment.End),
                    shape = CircleShape
                ) {
                    Text("保存高级设置")
                }
            }

            SectionCard {
                CardTitle("WiFi 自动关闭")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "启用定时关闭",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (autoOffEnabled) "到时后自动关闭 WiFi" else "当前不启用定时关闭",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoOffEnabled,
                        onCheckedChange = {
                            autoOffEnabled = it
                            if (it && autoOffMinutes.isBlank()) autoOffMinutes = "10"
                        }
                    )
                }
                if (autoOffEnabled) {
                    SectionDivider()
                    OutlinedTextField(
                        value = autoOffMinutes,
                        onValueChange = { autoOffMinutes = it.filter(Char::isDigit).take(2) },
                        label = { Text("关闭前等待时间（分钟）") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        supportingText = { Text(autoOffError ?: "支持 10-60 分钟") },
                        isError = autoOffError != null,
                        shape = MaterialTheme.shapes.large
                    )
                }
                Button(
                    onClick = { viewModel.saveWifiAutoOff(autoOffEnabled, autoOffMinutes) },
                    enabled = !state.isSaving && autoOffError == null,
                    modifier = Modifier.align(Alignment.End),
                    shape = CircleShape
                ) {
                    Text("保存定时设置")
                }
            }

            WpsCard(
                wpsInfo = state.wpsInfo,
                wpsPin = wpsPin,
                isSaving = state.isSaving,
                onPinChange = { wpsPin = it },
                onStartPushButton = viewModel::startWpsPushButton,
                onStartPin = { viewModel.startWpsPin(wpsPin) },
                onCancel = viewModel::cancelWps
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }

    if (showSecurityModeSheet) {
        ModalBottomSheet(onDismissRequest = { showSecurityModeSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "选择加密方式",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "当前：${securityModeLabel(selectedMode)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                securityModes.forEachIndexed { index, option ->
                    TextButton(
                        onClick = {
                            showSecurityModeSheet = false
                            selectedMode = option.value
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(option.label)
                                if (option.recommended) {
                                    Text(
                                        text = "推荐",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                if (option.insecure) {
                                    Text(
                                        text = "不安全",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            if (option.value == selectedMode) {
                                Text(
                                    text = "当前",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    if (index != securityModes.lastIndex) HorizontalDivider()
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

private data class SecurityModeOption(
    val value: String,
    val label: String,
    val recommended: Boolean = false,
    val insecure: Boolean = false
)

private fun securityModeOptions(): List<SecurityModeOption> = listOf(
    SecurityModeOption("Mixed", "WPA/WPA2 混合", insecure = true),
    SecurityModeOption("WPA-PSK", "WPA-PSK", insecure = true),
    SecurityModeOption("WPA2-PSK", "WPA2-PSK", recommended = true),
    SecurityModeOption("WPA2-WPA3", "WPA2/WPA3 过渡", recommended = true),
    SecurityModeOption("WPA3-SAE", "WPA3-SAE", recommended = true)
)

private fun securityModeLabel(value: String): String =
    securityModeOptions().firstOrNull { it.value == value }?.label ?: value

@Composable
private fun WpsCard(
    wpsInfo: WpsInfo,
    wpsPin: String,
    isSaving: Boolean,
    onPinChange: (String) -> Unit,
    onStartPushButton: () -> Unit,
    onStartPin: () -> Unit,
    onCancel: () -> Unit
) {
    SectionCard {
        CardTitle("WPS")
        KeyValueRow("当前状态", wpsInfo.statusText())
        SectionDivider()
        Button(
            onClick = onStartPushButton,
            enabled = !isSaving && !wpsInfo.isMatching(),
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape
        ) {
            Text("开始一键配对")
        }
        OutlinedTextField(
            value = wpsPin,
            onValueChange = { value ->
                onPinChange(value.filter { it.isDigit() || it == '-' }.take(9))
            },
            label = { Text("WPS PIN") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = { Text("支持 4 位、8 位，或 1234-5678") }
        )
        Button(
            onClick = onStartPin,
            enabled = !isSaving && isValidWpsPin(wpsPin) && !wpsInfo.isMatching(),
            modifier = Modifier.fillMaxWidth(),
            shape = CircleShape
        ) {
            Text("使用 PIN 配对")
        }
        if (wpsInfo.isMatching()) {
            Button(
                onClick = onCancel,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape
            ) {
                Text("取消配对")
            }
        }
    }
}

private fun isValidWpsPin(pin: String): Boolean {
    val normalized = pin.replace("-", "")
    return normalized.length in listOf(4, 8) && normalized.all { it.isDigit() }
}

private fun autoOffMinutesError(enabled: Boolean, minutes: String): String? {
    if (!enabled) return null
    val value = minutes.toIntOrNull() ?: return "请输入 10-60 分钟"
    return if (value in 10..60) null else "请输入 10-60 分钟"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChannelSelector(
    selectedChannel: String,
    firstChannel: Int,
    lastChannel: Int,
    onChannelSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val channels = (firstChannel..lastChannel).map { it.toString() }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = "信道 $selectedChannel",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = MaterialTheme.shapes.large
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            channels.forEach { ch ->
                DropdownMenuItem(
                    text = { Text("信道 $ch") },
                    onClick = {
                        onChannelSelected(ch)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BandwidthSelector(
    selectedBandwidth: String,
    onBandwidthSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("1" to "20 MHz", "2" to "20/40 MHz")
    val currentLabel = options.firstOrNull { it.first == selectedBandwidth }?.second ?: "20 MHz"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = currentLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("带宽") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = MaterialTheme.shapes.large
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onBandwidthSelected(value)
                        expanded = false
                    }
                )
            }
        }
    }
}
