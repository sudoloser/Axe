/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * AppDetectionService.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

@file:Suppress("DEPRECATION")

package com.my.axe.feature_rpc_base.services

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.os.Build
import android.os.IBinder
import com.blankj.utilcode.util.AppUtils
import com.my.axe.data.rpc.AxeRPC
import com.my.axe.data.rpc.CommonRpc
import com.my.axe.data.rpc.RpcImage
import com.my.axe.data.rpc.TemplateProcessor
import com.my.axe.data.utils.ConfigUtils
import com.my.axe.data.utils.toRpcImage
import com.my.axe.domain.model.rpc.RpcButtons
import com.my.axe.domain.model.rpc.RpcConfig
import com.my.axe.feature_rpc_base.Constants
import com.my.axe.feature_rpc_base.detection.ShizukuDetectionStrategy
import com.my.axe.feature_rpc_base.detection.UsageStatsDetectionStrategy
import com.my.axe.feature_rpc_base.setLargeIcon
import com.my.axe.preference.Prefs
import com.my.axe.resources.R
import com.blankj.utilcode.util.FileIOUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.SortedMap
import java.util.TreeMap
import javax.inject.Inject

@AndroidEntryPoint
class AppDetectionService : Service() {

    @Inject
    lateinit var AxeRPC: AxeRPC

    @Inject
    lateinit var scope: CoroutineScope

    @Inject
    lateinit var notificationBuilder: Notification.Builder

    @Inject
    lateinit var notificationManager: NotificationManager

    private lateinit var pendingIntent: PendingIntent

    private lateinit var restartPendingIntent: PendingIntent

    private var runningPackage = ""
    private var currentConfigName: String? = null

    override fun onBind(intent: Intent): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == Constants.ACTION_STOP_SERVICE) {
            stopSelf()
        } else if (intent?.action == Constants.ACTION_RESTART_SERVICE) {
            stopSelf()
            startService(Intent(this, AppDetectionService::class.java))
        } else {
            handleAppDetection()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Prefs[Prefs.LAST_RPC_TYPE] = ""
        scope.cancel()
        AxeRPC.closeRPC()
        super.onDestroy()
    }

    private fun handleAppDetection() {
        val enabledPackages = getEnabledPackages()

        val stopIntent = createStopIntent()
        pendingIntent = createPendingIntent(stopIntent)

        val restartIntent = createRestartIntent()
        restartPendingIntent = PendingIntent.getService(
            this,
            0, restartIntent, PendingIntent.FLAG_IMMUTABLE
        )
        // Adding action to notification builder here to avoid having multiple Exit buttons
        // https://github.com/dead8309/axe/issues/197
        notificationBuilder
            .setSmallIcon(R.drawable.ic_apps)
            .addAction(R.drawable.ic_apps, getString(R.string.restart), restartPendingIntent)
            .addAction(R.drawable.ic_apps, getString(R.string.exit), pendingIntent)


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            startForeground(Constants.NOTIFICATION_ID, createDefaultNotification())
        } else {
            startForeground(Constants.NOTIFICATION_ID, createDefaultNotification(), FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        }

        val rpcButtons = getRpcButtons()

        scope.launch {
            while (isActive) {
                val packageName = getForegroundPackage()

                if (packageName != null && packageName !in EXCLUDED_APPS) {
                    handleValidPackage(packageName, enabledPackages, rpcButtons)
                }
                delay(5000)
            }
        }
    }

    private fun getForegroundPackage(): String? {
        val useShizuku = Prefs[Prefs.USE_SHIZUKU, false]
        var packageName: String? = null

        if (useShizuku) {
            packageName = ShizukuDetectionStrategy().getForegroundApp()
        }

        if (packageName == null) {
            packageName = UsageStatsDetectionStrategy(this).getForegroundApp()
        }

        return packageName
    }

    private fun getEnabledPackages(): List<String> {
        val apps = Prefs[Prefs.ENABLED_APPS, "[]"]
        return Json.decodeFromString(apps)
    }

    private fun getRpcButtons(): RpcButtons {
        val rpcButtonsString = Prefs[Prefs.RPC_BUTTONS_DATA, "{}"]
        return Json.decodeFromString(rpcButtonsString)
    }

    private suspend fun handleValidPackage(
        packageName: String,
        enabledPackages: List<String>,
        rpcButtons: RpcButtons,
    ) {
        val customConfigName = Prefs.getAppCustomConfig(packageName)
        if (packageName in enabledPackages && (packageName != runningPackage || customConfigName != currentConfigName)) {
            handleEnabledPackage(packageName, rpcButtons, customConfigName)
            runningPackage = packageName
            currentConfigName = customConfigName
        } else if (packageName != runningPackage) {
            handleDisabledPackage()
            runningPackage = ""
            currentConfigName = null
        }
    }

    private suspend fun handleEnabledPackage(
        packageName: String,
        rpcButtons: RpcButtons,
        customConfigName: String?
    ) {
        val customConfig = customConfigName?.let { loadConfig(it) }

        if (!AxeRPC.isRpcRunning() || packageName != runningPackage || customConfigName != currentConfigName) {
            val isRunning = AxeRPC.isRpcRunning()
            scope.launch {
                com.blankj.utilcode.util.LogUtils.i("AppDetectionService", "Package change detected: $packageName (old: $runningPackage). Config: $customConfigName (old: $currentConfigName). isRunning: $isRunning")
            }
            if (isRunning) {
                AxeRPC.closeRPC()
            }
            AxeRPC.apply {
                if (customConfig != null) {
                    applyCustomConfig(packageName, customConfig)
                } else {
                    applyDefaultConfig(packageName, rpcButtons)
                }
                build()
            }
        }
        notificationManager.notify(
            Constants.NOTIFICATION_ID, notificationBuilder
                .setContentText(packageName)
                .setLargeIcon(
                    rpcImage = if (customConfig != null) customConfig.largeImg.toRpcImage() else RpcImage.ApplicationIcon(packageName, this@AppDetectionService),
                    context = this@AppDetectionService
                )
                .build()
        )
    }

    private fun applyCustomConfig(packageName: String, config: RpcConfig) {
        val templateProcessor = TemplateProcessor(detectedAppInfo = CommonRpc(name = AppUtils.getAppName(packageName)))
        AxeRPC.apply {
            setName(templateProcessor.process(config.name) ?: AppUtils.getAppName(packageName))
            setDetails(templateProcessor.process(config.details)?.ifEmpty { null })
            setState(templateProcessor.process(config.state)?.ifEmpty { null })
            setPartySize(config.partyCurrentSize.toIntOrNull(), config.partyMaxSize.toIntOrNull())
            setStatus(config.status.ifEmpty { "online" })
            setType(config.type.toIntOrNull() ?: 0)
            setPlatform(config.platform.ifEmpty { null })
            setStartTimestamps(config.timestampsStart.toLongOrNull() ?: System.currentTimeMillis())
            setStopTimestamps(config.timestampsStop.toLongOrNull())
            setButton1(templateProcessor.process(config.button1)?.ifEmpty { null })
            setButton1URL(templateProcessor.process(config.button1link)?.ifEmpty { null })
            setButton2(templateProcessor.process(config.button2)?.ifEmpty { null })
            setButton2URL(templateProcessor.process(config.button2link)?.ifEmpty { null })
            setLargeImage(config.largeImg.toRpcImage() ?: RpcImage.ApplicationIcon(packageName, this@AppDetectionService), templateProcessor.process(config.largeText))
            setSmallImage(config.smallImg.toRpcImage(), templateProcessor.process(config.smallText))
            setStreamUrl(config.url.ifEmpty { null })
        }
    }

    private fun applyDefaultConfig(packageName: String, rpcButtons: RpcButtons) {
        AxeRPC.apply {
            setName(AppUtils.getAppName(packageName))
            setStartTimestamps(System.currentTimeMillis())
            setStatus(Prefs[Prefs.CUSTOM_ACTIVITY_STATUS, "dnd"])
            setLargeImage(RpcImage.ApplicationIcon(packageName, this@AppDetectionService))
            if (Prefs[Prefs.USE_RPC_BUTTONS, false]) {
                with(rpcButtons) {
                    setButton1(button1.takeIf { it.isNotEmpty() })
                    setButton1URL(button1Url.takeIf { it.isNotEmpty() })
                    setButton2(button2.takeIf { it.isNotEmpty() })
                    setButton2URL(button2Url.takeIf { it.isNotEmpty() })
                }
            }
        }
    }

    private fun loadConfig(configName: String): RpcConfig? {
        val file = File(ConfigUtils.getConfigDir(this), "$configName.json")
        if (!file.exists()) return null
        return try {
            val jsonString = FileIOUtils.readFile2String(file)
            Json.decodeFromString<RpcConfig>(jsonString)
        } catch (e: Exception) {
            null
        }
    }

    private fun handleDisabledPackage() {
        val isRunning = AxeRPC.isRpcRunning()
        com.blankj.utilcode.util.LogUtils.i("AppDetectionService", "handleDisabledPackage() called. isRunning: $isRunning")
        if (isRunning) {
            AxeRPC.closeRPC()
        }
        notificationManager.notify(Constants.NOTIFICATION_ID, createDefaultNotification())
    }

    private fun createDefaultNotification(): Notification {
        return Notification.Builder(this, Constants.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_apps)
            .setContentTitle(getString(R.string.service_enabled))
            .addAction(R.drawable.ic_apps, getString(R.string.exit), pendingIntent)
            .addAction(R.drawable.ic_apps, getString(R.string.restart), restartPendingIntent)
            .build()
    }

    private fun createStopIntent(): Intent {
        val stopIntent = Intent(this, AppDetectionService::class.java)
        stopIntent.action = Constants.ACTION_STOP_SERVICE
        return stopIntent
    }

    private fun createRestartIntent(): Intent {
        val restartIntent = Intent(this, AppDetectionService::class.java)
        restartIntent.action = Constants.ACTION_RESTART_SERVICE
        return restartIntent
    }

    private fun createPendingIntent(stopIntent: Intent): PendingIntent {
        return PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)
    }

    companion object {
        val EXCLUDED_APPS = listOf("com.my.axe", "com.discord")
    }
}
