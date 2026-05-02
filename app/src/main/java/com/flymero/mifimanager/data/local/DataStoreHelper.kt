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
            .putString("recharge_no", rechargeNo)
            .putBoolean("remember", true)
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
    fun getSavedRechargeNo(): String = prefs.getString("recharge_no", "") ?: ""
    fun shouldRemember(): Boolean = prefs.getBoolean("remember", false)

    fun hasSavedCredentials(): Boolean =
        shouldRemember() && getSavedUsername().isNotEmpty() && getSavedPassword().isNotEmpty()

    fun getRechargeNo(): String = prefs.getString("recharge_no", "") ?: ""
}
