package com.flymero.mifimanager.data.model

import com.google.gson.annotations.SerializedName

data class SimInfo(
    @SerializedName("switch_mode") val switchMode: String = "1",
    @SerializedName("current_sim_id") val currentSimId: String = "0",
    @SerializedName("current_sim_banned") val currentSimBanned: String = "0",
    @SerializedName("master_sim_id") val masterSimId: String = "0",
    @SerializedName("sole_sim_id") val soleSimId: String = "0",
    @SerializedName("virtual_iccid") val virtualIccid: String = "",
    @SerializedName("sim_list") val simList: List<SimCard> = emptyList()
)

data class SimCard(
    @SerializedName("sim_id") val simId: String = "",
    @SerializedName("sim_name") val simName: String = "",
    @SerializedName("sim_type") val simType: String = "",
    @SerializedName("sim_present") val simPresent: String = "0",
    @SerializedName("sim_banned") val simBanned: String = "0",
    @SerializedName("sim_iccid") val simIccid: String = "",
    @SerializedName("sim_imsi") val simImsi: String = ""
) {
    fun isPresent(): Boolean = simPresent == "1"
    fun isBanned(): Boolean = simBanned == "1"
}
