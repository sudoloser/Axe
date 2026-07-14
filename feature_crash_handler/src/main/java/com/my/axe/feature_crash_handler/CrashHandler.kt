/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * CrashHandler.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.axe.feature_crash_handler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.DeviceUtils
import com.developer.crashx.CrashActivity
import com.my.axe.ui.theme.axeTheme
import com.my.axe.ui.theme.LocalDarkTheme
import com.my.axe.ui.theme.LocalDynamicColorSwitch

class CrashHandler : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rawTrace = CrashActivity.getStackTraceFromIntent(intent)
        val report = CrashReportData(
            stackTrace = rawTrace ?: "",
            manufacturer = DeviceUtils.getManufacturer(),
            device = DeviceUtils.getModel(),
            androidVersion = DeviceUtils.getSDKVersionName(),
            appVersionName = AppUtils.getAppVersionName(),
            appVersionCode = AppUtils.getAppVersionCode(),
        )
        setContent {
            axeTheme(
                darkTheme = LocalDarkTheme.current.isDarkTheme(),
                isHighContrastModeEnabled = LocalDarkTheme.current.isHighContrastModeEnabled,
                isDynamicColorEnabled = LocalDynamicColorSwitch.current,
            ){
                CrashScreen(report = report)
            }
        }
    }
}