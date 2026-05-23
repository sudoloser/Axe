package com.my.axe

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.my.axe.feature_crash_handler.CrashHandlerConfig
import com.my.axe.feature_logs.LoggerProvider
import com.my.axe.preference.PreferenceConfig
import com.my.axe.feature_rpc_base.AppUtils
import dagger.hilt.android.HiltAndroidApp
import axe.gateway.DiscordWebSocket
import javax.inject.Inject

@HiltAndroidApp
class App: Application() {

    @Inject
    lateinit var discordWebSocket: DiscordWebSocket

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        CrashHandlerConfig.apply()
        PreferenceConfig.apply(this)
        LoggerProvider.init()
        AppUtils.init(this, discordWebSocket)
    }
}