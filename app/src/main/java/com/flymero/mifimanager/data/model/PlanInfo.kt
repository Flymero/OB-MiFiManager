package com.flymero.mifimanager.data.model

import com.google.gson.annotations.SerializedName

data class PlanLoginResponse(
    @SerializedName("code") val code: Int = -1,
    @SerializedName("msg") val msg: String = "",
    @SerializedName("data") val data: PlanInfo? = null
) {
    val isSuccess: Boolean get() = code == 1
}

data class PlanInfo(
    @SerializedName("balance") val balance: String = "0",
    @SerializedName("totalAmount") val totalAmount: Double = 0.0,
    @SerializedName("remainAmount") val remainAmount: Double = 0.0,
    @SerializedName("operator") val operator: String = "",
    @SerializedName("packageName") val packageName: String = "",
    @SerializedName("expiretime") val expiretime: String = "",
    @SerializedName("status") val status: String = "",
    @SerializedName("realname_status") val realnameStatus: String = "",
    @SerializedName("payment_type_text") val paymentTypeText: String = "",
    @SerializedName("equipment") val equipment: PlanEquipment? = null,
    @SerializedName("coupon") val coupon: Int = 0
) {
    fun usedAmount(): Double = totalAmount - remainAmount

    fun usedFormatted(): String = formatMB(usedAmount())
    fun remainFormatted(): String = formatMB(remainAmount)
    fun totalFormatted(): String = formatMB(totalAmount)

    fun usagePercent(): Float {
        if (totalAmount <= 0) return 0f
        return (usedAmount() / totalAmount * 100).toFloat()
    }

    private fun formatMB(mb: Double): String = when {
        mb >= 1048576.0 -> "%.2f TB".format(mb / 1048576.0)
        mb >= 1024.0 -> "%.2f GB".format(mb / 1024.0)
        else -> "%.0f MB".format(mb)
    }
}

data class PlanEquipment(
    @SerializedName("dev_no") val devNo: String = "",
    @SerializedName("reportTime") val reportTime: String = "",
    @SerializedName("deviceStatus") val deviceStatus: Int = 0,
    @SerializedName("runningTime") val runningTime: String = "",
    @SerializedName("hotspotName") val hotspotName: String = "",
    @SerializedName("hotspotPassword") val hotspotPassword: String = "",
    @SerializedName("card_list") val cardList: List<PlanSimCard> = emptyList()
)

data class PlanSimCard(
    @SerializedName("iccid") val iccid: String = "",
    @SerializedName("currentUsage") val currentUsage: Int = 0,
    @SerializedName("operator_text") val operatorText: String = "",
    @SerializedName("realname_status_text") val realnameStatusText: String = ""
) {
    fun isInUse(): Boolean = currentUsage == 1
}
