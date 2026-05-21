package com.flymero.mifimanager.data.api

import com.flymero.mifimanager.data.model.*
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface MiFiApi {

    // GET endpoints
    @GET("xml_action.cgi?method=get&module=duster")
    suspend fun getHomepageInfo(@Query("file") file: String = "json_homepage_info"): HomepageInfo

    @GET("xml_action.cgi?method=get&module=duster")
    suspend fun getStatusInfo(@Query("file") file: String = "json_status_info"): StatusInfo

    @GET("xml_action.cgi?method=get&module=duster")
    suspend fun getEngineeringInfo(@Query("file") file: String = "json_engineering_info"): EngineeringInfo

    @GET("xml_action.cgi?method=get&module=duster")
    suspend fun getWlanInfo(@Query("file") file: String = "json_wlan_info"): WlanInfo

    @GET("xml_action.cgi?method=get&module=duster")
    suspend fun getWlanSecurityInfo(@Query("file") file: String = "json_wlan_security_info"): WlanSecurityInfo

    @GET("xml_action.cgi?method=get&module=duster")
    suspend fun getWanInfo(@Query("file") file: String = "json_wan_info"): WanInfo

    @GET("xml_action.cgi?method=get&module=duster")
    suspend fun getWanPdpContextInfo(@Query("file") file: String = "json_wan_pdp_context_info"): WanPdpContextInfo

    @GET("xml_action.cgi?method=get&module=duster")
    suspend fun getApnProfileInfo(@Query("file") file: String = "json_wan_apn_profile_info"): ApnProfileInfo

    @GET("xml_action.cgi?method=get&module=duster")
    suspend fun getDeviceManagementInfo(@Query("file") file: String = "json_device_management_info"): DeviceManagementInfo

    @GET("xml_action.cgi?method=get&module=duster")
    suspend fun getDhcpInfo(@Query("file") file: String = "json_dhcp_info"): DhcpInfo

    @GET("xml_action.cgi?method=get&module=duster")
    suspend fun getLoginInfo(@Query("file") file: String = "json_login_info"): LoginInfo

    @GET("xml_action.cgi?method=get&module=duster")
    suspend fun getFirmwareInfo(@Query("file") file: String = "json_firmware_info"): FirmwareInfo

    @GET("xml_action.cgi?method=get&module=duster")
    suspend fun getCustomFwInfo(@Query("file") file: String = "json_custom_fw_info"): CustomFwInfo

    @GET("xml_action.cgi?method=get&module=duster")
    suspend fun getWlanMacFiltersInfo(@Query("file") file: String = "json_wlan_mac_filters_info"): WlanMacFiltersInfo

    @GET("xml_action.cgi?method=get&module=duster")
    suspend fun getWlanWpsInfo(@Query("file") file: String = "json_wlan_wps_info"): WpsInfo

    @GET("xml_action.cgi?method=get&module=duster")
    suspend fun getAccountManagementInfo(@Query("file") file: String = "json_account_management_info"): AccountManagementInfo

    @GET("xml_action.cgi?method=get&module=duster")
    suspend fun getStatisticsInfo(@Query("file") file: String = "json_statistics_info"): StatisticsInfo

    @GET("xml_action.cgi?method=get&module=duster")
    suspend fun getSimInfo(@Query("file") file: String = "json_mss_support_info"): SimInfo

    @GET("xml_action.cgi?method=get&module=duster")
    suspend fun getSmsAuthTerminalList(@Query("file") file: String = "json_sms_auth_get_terminal_list"): SmsAuthTerminalList

    @GET("xml_action.cgi?Action=logout")
    suspend fun logout()

    // POST endpoints
    @POST("xml_action.cgi?method=post&module=duster&file=json_wlan_set")
    suspend fun setWlan(@Body body: RequestBody): ApiResult

    @POST("xml_action.cgi?method=post&module=duster&file=json_wlan_security_set")
    suspend fun setWlanSecurity(@Body body: RequestBody): ApiResult

    @POST("xml_action.cgi?method=post&module=duster&file=json_wan_set")
    suspend fun setWan(@Body body: RequestBody): ApiResult

    @POST("xml_action.cgi?method=post&module=duster&file=json_device_management_set")
    suspend fun setDeviceManagement(@Body body: RequestBody): ApiResult

    @POST("xml_action.cgi?method=post&module=duster&file=json_account_management_set")
    suspend fun setAccountManagement(@Body body: RequestBody): ApiResult

    @POST("xml_action.cgi?method=post&module=duster&file=json_lan_set")
    suspend fun setLan(@Body body: RequestBody): ApiResult

    @POST("xml_action.cgi?method=post&module=duster&file=json_wlan_mac_filters_set")
    suspend fun setWlanMacFilters(@Body body: RequestBody): ApiResult

    @POST("xml_action.cgi?method=post&module=duster&file=json_statistics_set")
    suspend fun setStatistics(@Body body: RequestBody): ApiResult

    @POST("xml_action.cgi?method=post&module=duster&file=json_custom_fw_set")
    suspend fun setCustomFw(@Body body: RequestBody): ApiResult

    @GET("xml_action.cgi?method=get&module=duster&file=json_device_restart")
    suspend fun restartDevice()

    @GET("xml_action.cgi?Action=restore_factory")
    suspend fun restoreFactory()

    @POST("xml_action.cgi?method=post&module=duster&file=json_sms_auth_sendSms")
    suspend fun smsAuthSendSms(@Body body: RequestBody): SmsAuthResult

    @POST("xml_action.cgi?method=post&module=duster&file=json_sms_auth_terminalAuth")
    suspend fun smsAuthVerify(@Body body: RequestBody): SmsAuthResult

    @POST("xml_action.cgi?method=post&module=duster&file=json_mss_support_set")
    suspend fun setSimConfig(@Body body: RequestBody): ApiResult
}
