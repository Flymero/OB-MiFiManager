package com.flymero.mifimanager.ui.dashboard

enum class DashboardCardType(val id: String, val title: String) {
    DeviceStatus("device_status", "设备状态"),
    PlanUsage("plan_usage", "套餐信息"),
    TrafficStats("traffic_stats", "用量统计"),
    NetworkConnection("network_connection", "网络连接");

    companion object {
        val defaultOrder = listOf(
            DeviceStatus,
            PlanUsage,
            TrafficStats,
            NetworkConnection
        )

        fun fromId(id: String): DashboardCardType? =
            entries.firstOrNull { it.id == id }

        fun normalize(ids: List<String>): List<DashboardCardType> {
            val savedCards = ids.mapNotNull(::fromId).distinct()
            val missingCards = defaultOrder.filterNot { it in savedCards }
            return (savedCards + missingCards).ifEmpty { defaultOrder }
        }
    }
}
