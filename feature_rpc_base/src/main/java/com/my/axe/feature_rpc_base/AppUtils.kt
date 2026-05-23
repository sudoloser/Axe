/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * AppUtils.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

@file:Suppress("DEPRECATION")

package com.my.axe.feature_rpc_base

import android.app.ActivityManager
import android.content.Context
import com.my.axe.feature_rpc_base.services.AppDetectionService
import com.my.axe.feature_rpc_base.services.CustomRpcService
import com.my.axe.feature_rpc_base.services.ExperimentalRpc
import com.my.axe.feature_rpc_base.services.MediaRpcService
import com.my.axe.preference.Prefs
import axe.gateway.DiscordWebSocket
import javax.inject.Singleton

@Singleton
object AppUtils {
    private lateinit var activityManager: ActivityManager
    private var discordWebSocket: DiscordWebSocket? = null

    fun init(context: Context, discordWebSocket: DiscordWebSocket? = null) {
        activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        this.discordWebSocket = discordWebSocket
    }

    private fun isRemoteActive(type: String): Boolean {
        val useRemote = Prefs[Prefs.USE_REMOTE_GATEWAY, false]
        val sessionActive = discordWebSocket?.sessionActive?.value ?: false
        val lastType = Prefs[Prefs.LAST_RPC_TYPE, ""]
        return useRemote && sessionActive && lastType == type
    }

    fun appDetectionRunning(): Boolean {
        return checkForRunningService<AppDetectionService>() || isRemoteActive("APPS")
    }

    fun mediaRpcRunning(): Boolean {
        return checkForRunningService<MediaRpcService>() || isRemoteActive("MEDIA")
    }

    fun customRpcRunning(type: String? = null): Boolean {
        val localRunning = if (type == null) {
            checkForRunningService<CustomRpcService>()
        } else {
            checkForRunningService<CustomRpcService>() && CustomRpcService.runningType == type
        }

        return localRunning || (type != null && isRemoteActive(type))
    }

    fun experimentalRpcRunning(): Boolean {
        return checkForRunningService<ExperimentalRpc>() || isRemoteActive("EXPERIMENTAL")
    }
...
    fun overlayRunning(): Boolean {
        return isServiceRunning("com.my.axe.feature_overlay.OverlayService")
    }

    private fun isServiceRunning(className: String): Boolean {
        for (runningServiceInfo in activityManager.getRunningServices(
            Int.MAX_VALUE
        )) {
            if (className == runningServiceInfo.service.className)
                return true
        }
        return false
    }

    private inline fun <reified T : Any> checkForRunningService(): Boolean {
        return isServiceRunning(T::class.java.name)
    }
}