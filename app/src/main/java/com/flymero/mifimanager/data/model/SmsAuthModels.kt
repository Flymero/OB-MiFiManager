package com.flymero.mifimanager.data.model

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    fun remainingTime(): String {
        if (expireTime.isBlank()) return ""
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val expire = sdf.parse(expireTime) ?: return expireTime
            val diff = expire.time - Date().time
            if (diff <= 0) return "已过期"
            val days = diff / 86400000
            val hours = (diff % 86400000) / 3600000
            when {
                days > 0 -> "剩余 ${days}天${hours}小时"
                hours > 0 -> "剩余 ${hours}小时"
                else -> "即将过期"
            }
        } catch (e: Exception) {
            expireTime
        }
    }
}

data class SmsAuthResult(
    @SerializedName("code") val code: Int = -1,
    @SerializedName("message") val message: String = ""
) {
    val isSuccess: Boolean get() = code == 0
}
