package com.flymero.mifimanager.data.model

import com.google.gson.annotations.SerializedName

data class OrderListResponse(
    @SerializedName("code") val code: Int = -1,
    @SerializedName("msg") val msg: String = "",
    @SerializedName("data") val data: List<OrderItem>? = null
) {
    val isSuccess: Boolean get() = code == 1
}

data class OrderItem(
    @SerializedName("order_no") val orderNo: String = "",
    @SerializedName("package_name") val packageName: String = "",
    @SerializedName("show_name") val showName: String = "",
    @SerializedName("amount") val amount: String = "0",
    @SerializedName("state") val state: String = "",
    @SerializedName("pay_type") val payType: String = "",
    @SerializedName("type_name") val typeName: String = "",
    @SerializedName("is_next") val isNext: String = "",
    @SerializedName("effectivetime") val effectiveTime: String = "",
    @SerializedName("expiretime") val expireTime: String = "",
    @SerializedName("paytime_text") val payTime: String = "",
    @SerializedName("createtime_text") val createTime: String = ""
) {
    fun displayTime(): String = payTime.ifBlank { createTime }

    fun statusText(): String = when (state) {
        "0" -> "待生效"
        "1" -> "生效中"
        "2" -> "已过期"
        else -> state
    }

    fun displayName(): String = showName.ifBlank { packageName }
}
