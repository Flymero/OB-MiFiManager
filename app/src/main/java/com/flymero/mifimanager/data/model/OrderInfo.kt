package com.flymero.mifimanager.data.model

import com.google.gson.annotations.SerializedName

data class OrderListResponse(
    @SerializedName("code") val code: Int = -1,
    @SerializedName("msg") val msg: String = "",
    @SerializedName("data") val data: OrderListData? = null
) {
    val isSuccess: Boolean get() = code == 1
}

data class OrderListData(
    @SerializedName("list") val list: List<OrderItem> = emptyList()
)

data class OrderItem(
    @SerializedName("order_no") val orderNo: String = "",
    @SerializedName("package_name") val packageName: String = "",
    @SerializedName("amount") val amount: String = "0",
    @SerializedName("status_text") val statusText: String = "",
    @SerializedName("pay_time") val payTime: String = "",
    @SerializedName("create_time") val createTime: String = ""
) {
    fun displayTime(): String = payTime.ifBlank { createTime }
}
