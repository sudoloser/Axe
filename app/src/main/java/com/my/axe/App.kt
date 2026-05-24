package com.my.axe

import android.app.Application
import android.os.Build.VERSION.SDK_INT
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import com.google.android.material.color.DynamicColors
import com.my.axe.feature_crash_handler.CrashHandlerConfig
import com.my.axe.feature_logs.LoggerProvider
import com.my.axe.preference.PreferenceConfig
import com.my.axe.feature_rpc_base.AppUtils
import dagger.hilt.android.HiltAndroidApp
import axe.gateway.DiscordWebSocket
import javax.inject.Inject

@HiltAndroidApp
class App: Application(), ImageLoaderFactory {

    @Inject
    lateinit var discordWebSocket: DiscordWebSocket

    override fun onCreate() {
        PreferenceConfig.apply(this)
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        CrashHandlerConfig.apply()
        LoggerProvider.init()
        AppUtils.init(this, discordWebSocket)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(SvgDecoder.Factory())
                if (SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }
}