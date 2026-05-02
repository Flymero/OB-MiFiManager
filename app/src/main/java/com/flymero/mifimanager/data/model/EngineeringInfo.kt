package com.flymero.mifimanager.data.model

import com.google.gson.annotations.SerializedName

data class EngineeringInfo(
    @SerializedName("Engineering_mode") val engineeringMode: String = "",
    @SerializedName("sys_mode") val sysMode: String = "",
    @SerializedName("lte") val lte: LteInfo? = null
)

data class LteInfo(
    @SerializedName("mcc") val mcc: String = "",
    @SerializedName("lenOfMnc") val lenOfMnc: String = "",
    @SerializedName("mnc") val mnc: String = "",
    @SerializedName("tac") val tac: String = "",
    @SerializedName("phyCellId") val phyCellId: String = "",
    @SerializedName("dlEuArfcn") val dlEuArfcn: String = "",
    @SerializedName("ulEuArfcn") val ulEuArfcn: String = "",
    @SerializedName("band") val band: String = "",
    @SerializedName("dlBandwidth") val dlBandwidth: String = "",
    @SerializedName("ECGI") val ecgi: String = "",
    @SerializedName("eNB_ID") val enbId: String = "",
    @SerializedName("Cell_ID") val cellId: String = "",
    @SerializedName("rsrp") val rsrp: String = "",
    @SerializedName("rsrq") val rsrq: String = "",
    @SerializedName("sinr") val sinr: String = "",
    @SerializedName("mainRsrp") val mainRsrp: String = "",
    @SerializedName("diversityRsrp") val diversityRsrp: String = "",
    @SerializedName("mainRsrq") val mainRsrq: String = "",
    @SerializedName("diversityRsrq") val diversityRsrq: String = "",
    @SerializedName("rssi") val rssi: String = "",
    @SerializedName("cqi") val cqi: String = "",
    @SerializedName("dlThroughPut") val dlThroughPut: String = "",
    @SerializedName("dlPeakThroughPut") val dlPeakThroughPut: String = "",
    @SerializedName("ulThroughPut") val ulThroughPut: String = "",
    @SerializedName("ulPeakThroughPut") val ulPeakThroughPut: String = "",
    @SerializedName("txPower") val txPower: String = ""
) {
    fun bandwidthMhz(): String = when (dlBandwidth) {
        "0" -> "1.4 MHz"
        "1" -> "3 MHz"
        "2" -> "5 MHz"
        "3" -> "10 MHz"
        "4" -> "15 MHz"
        "5" -> "20 MHz"
        else -> "$dlBandwidth MHz"
    }
}
