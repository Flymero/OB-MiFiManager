package com.flymero.mifimanager.data.model

import com.google.gson.annotations.SerializedName

data class SmsAuthTerminalList(
    @SerializedName("terminal_list") val terminalList: List<SmsAuthTerminal> = emptyList()
)

data class SmsAuthTerminal(
    @SerializedName("name") val name: String = "",
    @SerializedName("terminalMac") val terminalMac: String = "",
    @SerializedName("expireTime") val expireTime: String = "",
    @SerializedName("isAuth") val isAuth: String = "0"
) {
    fun isAuthenticated(): Boolean = isAuth == "1"
}

data class SmsAuthResult(
    @SerializedName("code") val code: Int = -1,
    @SerializedName("message") val message: String = ""
) {
    val isSuccess: Boolean get() = code == 0
}
