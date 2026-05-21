package com.flymero.mifimanager.ui.signal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flymero.mifimanager.data.model.LteInfo
import com.flymero.mifimanager.ui.components.CardTitle
import com.flymero.mifimanager.ui.components.KeyValueRow
import com.flymero.mifimanager.ui.components.SectionCard
import com.flymero.mifimanager.ui.components.SectionDivider
import com.flymero.mifimanager.ui.theme.SignalExcellent
import com.flymero.mifimanager.ui.theme.AppColors
import com.flymero.mifimanager.ui.theme.Warning

@Composable
fun SignalScreen(viewModel: SignalViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    if (state.isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) { CircularProgressIndicator() }
        return
    }

    val lte = state.engineeringInfo.lte ?: return
    val score = lte.signalScore()
    val qualityText = lte.qualityText(score)
    val qualityColor = if (score >= 75) SignalExcellent else Warning
    val qualityContainer = if (score >= 75) AppColors.successContainer() else AppColors.warningContainer()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "信号详情",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold
        )

        SectionCard {
            CardTitle("信号质量")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = qualityText,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = qualityColor
                )
                Text(
                    text = "综合评分：${score}/100",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LinearProgressIndicator(
                    progress = { score / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = qualityColor,
                    trackColor = MaterialTheme.colorScheme.outlineVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SignalMetricCard(
                    label = "RSRP",
                    value = "${lte.rsrp} dBm",
                    valueColor = SignalExcellent,
                    modifier = Modifier.weight(1f)
                )
                SignalMetricCard(
                    label = "SINR",
                    value = "${lte.sinr} dB",
                    valueColor = SignalExcellent,
                    containerColor = qualityContainer,
                    borderColor = qualityContainer,
                    modifier = Modifier.weight(1f)
                )
                SignalMetricCard(
                    label = "RSRQ",
                    value = "${lte.rsrq} dB",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        SectionCard {
            CardTitle("基站信息")
            KeyValueRow("MCC", lte.mcc)
            SectionDivider()
            KeyValueRow("MNC", lte.mnc)
            SectionDivider()
            KeyValueRow("TAC", lte.tac)
            SectionDivider()
            KeyValueRow("PCI", lte.phyCellId)
            SectionDivider()
            KeyValueRow("eNB ID", lte.enbId)
            SectionDivider()
            KeyValueRow("Cell ID", lte.cellId)
            SectionDivider()
            KeyValueRow("ECGI", lte.ecgi)
        }

        SectionCard {
            CardTitle("频段信息")
            KeyValueRow("频段", "Band ${lte.band}")
            SectionDivider()
            KeyValueRow("带宽", lte.bandwidthMhz())
            SectionDivider()
            KeyValueRow("下行 EARFCN", lte.dlEuArfcn)
            SectionDivider()
            KeyValueRow("上行 EARFCN", lte.ulEuArfcn)
            SectionDivider()
            KeyValueRow("CQI", "${lte.cqi} (0-15)")
            SectionDivider()
            KeyValueRow("RSSI", "${lte.rssi} dBm")
            SectionDivider()
            KeyValueRow("发射功率", "${lte.txPower} dBm")
            SectionDivider()
            KeyValueRow("下行吞吐", "${lte.dlThroughPut} Mbps")
            SectionDivider()
            KeyValueRow("上行吞吐", "${lte.ulThroughPut} Mbps")
        }

        if (lte.mainRsrp.isNotBlank() || lte.diversityRsrp.isNotBlank()) {
            SectionCard {
                CardTitle("双天线 (MIMO)")
                KeyValueRow("主天线 RSRP", if (lte.mainRsrp.isNotBlank()) "-${lte.mainRsrp} dBm" else "--")
                SectionDivider()
                KeyValueRow("分集 RSRP", if (lte.diversityRsrp.isNotBlank()) "-${lte.diversityRsrp} dBm" else "--")
                SectionDivider()
                KeyValueRow("主天线 RSRQ", if (lte.mainRsrq.isNotBlank()) "-${lte.mainRsrq} dB" else "--")
                SectionDivider()
                KeyValueRow("分集 RSRQ", if (lte.diversityRsrq.isNotBlank()) "-${lte.diversityRsrq} dB" else "--")
            }
        }

        state.pdpContext?.let { pdp ->
            SectionCard {
                CardTitle("网络详情")
                if (state.networkName.isNotBlank()) {
                    KeyValueRow("运营商", state.networkName)
                    SectionDivider()
                }
                KeyValueRow("WAN IP", pdp.ipv4.ifBlank { "--" })
                SectionDivider()
                KeyValueRow("网关", pdp.gateway.ifBlank { "--" })
                SectionDivider()
                KeyValueRow("DNS 1", pdp.dns1.ifBlank { "--" })
                SectionDivider()
                KeyValueRow("DNS 2", pdp.dns2.ifBlank { "--" })
                SectionDivider()
                KeyValueRow("本次连接", pdp.formattedCurrentConn())
                SectionDivider()
                KeyValueRow("累计在线", pdp.formattedTotalConn())
            }
        }
    }
}

@Composable
private fun SignalMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun LteInfo.signalScore(): Int {
    val rsrpScore = when (rsrp.toIntOrNull()?.let { -it } ?: 140) {
        in 0..80 -> 40
        in 81..90 -> 34
        in 91..100 -> 28
        in 101..110 -> 20
        in 111..120 -> 12
        else -> 6
    }
    val sinrScore = when (sinr.toIntOrNull() ?: -10) {
        in 20..100 -> 35
        in 13..19 -> 28
        in 0..12 -> 18
        else -> 8
    }
    val rsrqScore = when (rsrq.toIntOrNull()?.let { -it } ?: 20) {
        in 0..8 -> 25
        in 9..11 -> 20
        in 12..15 -> 12
        else -> 6
    }
    return (rsrpScore + sinrScore + rsrqScore).coerceIn(0, 100)
}

private fun LteInfo.qualityText(score: Int): String = when {
    score >= 80 -> "优秀"
    score >= 65 -> "良好"
    score >= 45 -> "一般"
    else -> "较弱"
}
