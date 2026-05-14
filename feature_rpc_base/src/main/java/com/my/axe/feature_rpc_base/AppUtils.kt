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
import javax.inject.Singleton

@Singleton
object AppUtils {
    private lateinit var activityManager: ActivityManager
    fun init(context: Context) {
        activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    }
    fun appDetectionRunning(): Boolean {
        return checkForRunningService<AppDetectionService>()
    }

    fun mediaRpcRunning(): Boolean {
        return checkForRunningService<MediaRpcService>()
    }

    fun customRpcRunning(): Boolean {
        return checkForRunningService<CustomRpcService>()
    }

    fun experimentalRpcRunning(): Boolean {
        return checkForRunningService<ExperimentalRpc>()
    }

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