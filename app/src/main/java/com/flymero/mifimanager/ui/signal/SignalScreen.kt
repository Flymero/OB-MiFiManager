package com.flymero.mifimanager.ui.signal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flymero.mifimanager.data.model.LteInfo
import com.flymero.mifimanager.ui.components.CardTitle
import com.flymero.mifimanager.ui.components.KeyValueRow
import com.flymero.mifimanager.ui.components.SectionCard
import com.flymero.mifimanager.ui.components.SectionDivider
import com.flymero.mifimanager.ui.components.StatusChip
import com.flymero.mifimanager.ui.theme.SignalExcellent
import com.flymero.mifimanager.ui.theme.SuccessContainer
import com.flymero.mifimanager.ui.theme.Warning
import com.flymero.mifimanager.ui.theme.WarningContainer

@OptIn(ExperimentalLayoutApi::class)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
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
            StatusChip(
                text = qualityText,
                color = if (score >= 75) SignalExcellent else Warning,
                containerColor = if (score >= 75) SuccessContainer else WarningContainer
            )
            Text(
                text = "综合评分 ${score}/100",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            LinearProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SignalMetricChip("RSRP", "${lte.rsrp} dBm")
                SignalMetricChip("SINR", "${lte.sinr} dB")
                SignalMetricChip("RSRQ", "${lte.rsrq} dB")
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
            KeyValueRow("CQI", lte.cqi)
            SectionDivider()
            KeyValueRow("RSSI", "${lte.rssi} dBm")
            SectionDivider()
            KeyValueRow("发射功率", "${lte.txPower} dBm")
        }
    }
}

@Composable
private fun SignalMetricChip(label: String, value: String) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
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
