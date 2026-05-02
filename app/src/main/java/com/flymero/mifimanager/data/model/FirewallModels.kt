package com.flymero.mifimanager.data.model

import com.google.gson.annotations.SerializedName

data class CustomFwInfo(
    @SerializedName("custom_rules_list") val customRulesList: List<FirewallRule> = emptyList()
)

data class FirewallRule(
    @SerializedName("index") val index: String = "",
    @SerializedName("rule_name") val ruleName: String = "",
    @SerializedName("enabled") val enabled: String = "",
    @SerializedName("src_ip") val srcIp: String = "",
    @SerializedName("src_port") val srcPort: String = "",
    @SerializedName("dst_ip") val dstIp: String = "",
    @SerializedName("dst_port") val dstPort: String = "",
    @SerializedName("proto") val proto: String = ""
)

data class WlanMacFiltersInfo(
    @SerializedName("mac_filter_enable") val macFilterEnable: String = "",
    @SerializedName("mac_filter_policy") val macFilterPolicy: String = "",
    @SerializedName("mac_filter_list") val macFilterList: List<MacFilter> = emptyList()
)

data class MacFilter(
    @SerializedName("mac") val mac: String = "",
    @SerializedName("name") val name: String = ""
)
