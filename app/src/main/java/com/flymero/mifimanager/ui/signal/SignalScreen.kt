package com.flymero.mifimanager.ui.signal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.flymero.mifimanager.ui.components.GaugeChart
import com.flymero.mifimanager.ui.components.InfoRow
import com.flymero.mifimanager.ui.theme.SignalBad
import com.flymero.mifimanager.ui.theme.SignalExcellent
import com.flymero.mifimanager.ui.theme.SignalFair
import com.flymero.mifimanager.ui.theme.SignalGood
import com.flymero.mifimanager.ui.theme.SignalPoor

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

    val lte = state.engineeringInfo.lte
    val status = state.statusInfo

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
            fontWeight = FontWeight.Bold
        )

        // Signal gauges
        if (lte != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "信号质量",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val rsrp = lte.rsrp.toFloatOrNull() ?: 0f
                        val rsrpColor = when {
                            rsrp >= 80 -> SignalBad
                            rsrp >= 60 -> SignalPoor
                            rsrp >= 40 -> SignalFair
                            rsrp >= 20 -> SignalGood
                            else -> SignalExcellent
                        }
                        GaugeChart(
                            value = rsrp,
                            maxValue = 140f,
                            label = "RSRP",
                            unit = "",
                            size = 90.dp,
                            color = rsrpColor
                        )

                        val sinr = lte.sinr.toFloatOrNull() ?: 0f
                        val sinrColor = when {
                            sinr >= 20 -> SignalExcellent
                            sinr >= 13 -> SignalGood
                            sinr >= 0 -> SignalFair
                            else -> SignalBad
                        }
                        GaugeChart(
                            value = sinr,
                            maxValue = 30f,
                            label = "SINR",
                            unit = "",
                            size = 90.dp,
                            color = sinrColor
                        )

                        val rsrq = lte.rsrq.toFloatOrNull() ?: 0f
                        GaugeChart(
                            value = rsrq,
                            maxValue = 40f,
                            label = "RSRQ",
                            unit = "",
                            size = 90.dp,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            // Cell info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "基站信息",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    InfoRow("MCC", lte.mcc)
                    InfoRow("MNC", lte.mnc)
                    InfoRow("TAC", lte.tac)
                    InfoRow("PCI", lte.phyCellId)
                    InfoRow("eNB ID", lte.enbId)
                    InfoRow("Cell ID", lte.cellId)
                    InfoRow("ECGI", lte.ecgi)
                }
            }

            // Band info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "频段信息",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    InfoRow("频段", "Band ${lte.band}")
                    InfoRow("带宽", lte.bandwidthMhz())
                    InfoRow("下行 EARFCN", lte.dlEuArfcn)
                    InfoRow("上行 EARFCN", lte.ulEuArfcn)
                    InfoRow("CQI", lte.cqi)
                    InfoRow("RSSI", lte.rssi)
                    InfoRow("发射功率", "${lte.txPower} dBm")
                    InfoRow("下行吞吐", "${lte.dlThroughPut} Mbps")
                    InfoRow("上行吞吐", "${lte.ulThroughPut} Mbps")
                }
            }
        }
    }
}
