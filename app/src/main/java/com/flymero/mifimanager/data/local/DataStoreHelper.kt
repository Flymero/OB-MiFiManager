package com.flymero.mifimanager.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreHelper @Inject constructor(
    @ApplicationContext val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("mifi_prefs", Context.MODE_PRIVATE)

    fun saveCredentials(username: String, password: String, rechargeNo: String) {
        prefs.edit()
            .putString("username", username)
            .putString("password", password)
            .putString("recharge_no", rechargeNo.trim())
            .putBoolean("remember", true)
            .apply()
    }

    fun saveRechargeNo(rechargeNo: String) {
        prefs.edit()
            .putString("recharge_no", rechargeNo.trim())
            .apply()
    }

    fun clearLoginCredentials() {
        prefs.edit()
            .remove("username")
            .remove("password")
            .putBoolean("remember", false)
            .apply()
    }

    fun clearCredentials() {
        prefs.edit()
            .remove("username")
            .remove("password")
            .remove("recharge_no")
            .putBoolean("remember", false)
            .apply()
    }

    fun getSavedUsername(): String = prefs.getString("username", "") ?: ""
    fun getSavedPassword(): String = prefs.getString("password", "") ?: ""
    fun getSavedRechargeNo(): String = (prefs.getString("recharge_no", "") ?: "").trim()
    fun shouldRemember(): Boolean = prefs.getBoolean("remember", false)

    fun hasSavedCredentials(): Boolean =
        shouldRemember() && getSavedUsername().isNotEmpty() && getSavedPassword().isNotEmpty()

    fun getRechargeNo(): String = getSavedRechargeNo()

    fun isDarkMode(): Boolean = prefs.getBoolean("dark_mode", false)

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("dark_mode", enabled).apply()
    }

    fun getDashboardCardIds(): List<String> =
        (prefs.getString("dashboard_card_order", "") ?: "")
            .split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    fun setDashboardCardIds(cardIds: List<String>) {
        prefs.edit()
            .putString("dashboard_card_order", cardIds.joinToString(","))
            .apply()
    }

    fun hasSeenPlanUsageHint(): Boolean =
        prefs.getBoolean("plan_usage_hint_seen", false)

    fun setPlanUsageHintSeen() {
        prefs.edit()
            .putBoolean("plan_usage_hint_seen", true)
            .apply()
    }
}
