package com.flymero.mifimanager.ui.util

import androidx.annotation.DrawableRes
import com.flymero.mifimanager.R

enum class CarrierType {
    ChinaMobile,
    ChinaUnicom,
    ChinaTelecom
}

fun carrierTypeOf(value: String): CarrierType? {
    val text = value.trim()
    return when {
        text.contains("移动", ignoreCase = true) ||
            text.contains("cmcc", ignoreCase = true) ||
            text.contains("chn mobile", ignoreCase = true) ||
            text.contains("china mobile", ignoreCase = true) -> CarrierType.ChinaMobile
        text.contains("联通", ignoreCase = true) ||
            text.contains("unicom", ignoreCase = true) ||
            text.contains("chn unicom", ignoreCase = true) ||
            text.contains("china unicom", ignoreCase = true) -> CarrierType.ChinaUnicom
        text.contains("电信", ignoreCase = true) ||
            text.contains("telecom", ignoreCase = true) ||
            text.contains("chn telecom", ignoreCase = true) ||
            text.contains("china telecom", ignoreCase = true) -> CarrierType.ChinaTelecom
        else -> null
    }
}

fun formatCarrierName(value: String): String {
    val text = value.trim()
    if (text.isEmpty()) return ""
    return when (carrierTypeOf(text)) {
        CarrierType.ChinaMobile -> if (text.contains("中国移动")) text else "中国移动"
        CarrierType.ChinaUnicom -> if (text.contains("中国联通")) text else "中国联通"
        CarrierType.ChinaTelecom -> if (text.contains("中国电信")) text else "中国电信"
        null -> text
    }
}

@DrawableRes
fun carrierLogoRes(value: String): Int? = when (carrierTypeOf(value)) {
    CarrierType.ChinaMobile -> R.drawable.china_mobile_square_
    CarrierType.ChinaUnicom -> R.drawable.china_unicom_square_
    CarrierType.ChinaTelecom -> R.drawable.china_telecom_1
    null -> null
}
