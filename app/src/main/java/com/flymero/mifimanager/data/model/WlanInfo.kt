package com.flymero.mifimanager.data.model

import com.google.gson.annotations.SerializedName

data class WlanInfo(
    @SerializedName("dual_band_support") val dualBandSupport: String = "",
    @SerializedName("rf_band") val rfBand: String = "",
    @SerializedName("net_mode") val netMode: String = "",
    @SerializedName("channel") val channel: String = "",
    @SerializedName("first_channel") val firstChannel: String = "",
    @SerializedName("last_channel") val lastChannel: String = "",
    @SerializedName("bandwidth") val bandwidth: String = "",
    @SerializedName("wlan_enable") val wlanEnable: String = "",
    @SerializedName("max_clients") val maxClients: String = "",
    @SerializedName("wifi_sleep_time") val wifiSleepTime: String = "",
    @SerializedName("beacon_period") val beaconPeriod: String = "",
    @SerializedName("dtim_interval") val dtimInterval: String = "",
    @SerializedName("ap_isolate") val apIsolate: String = "",
    @SerializedName("only_20m") val only20m: String = ""
)
