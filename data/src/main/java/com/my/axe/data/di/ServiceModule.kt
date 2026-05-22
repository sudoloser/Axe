/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * ServiceModule.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.axe.data.di

import com.my.axe.data.BuildConfig
import axe.gateway.RemoteGatewayManager
import com.my.axe.data.rpc.AxeRPC
import com.my.axe.domain.interfaces.Logger
import com.my.axe.domain.repository.AxeRepository
import com.my.axe.preference.Prefs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import axe.gateway.DiscordWebSocket
import axe.gateway.DiscordWebSocketImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.UUID

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {
    @Provides
    fun providesDiscordWebsocket(
        logger: Logger
    ): DiscordWebSocket {
        val useRemoteGateway = Prefs[Prefs.USE_REMOTE_GATEWAY, false]
        return if (useRemoteGateway) {
            val customUrl = Prefs[Prefs.REMOTE_GATEWAY_URL, "https://axe-server.onrender.com/"]
            val customSignature = Prefs[Prefs.REMOTE_GATEWAY_SIGNATURE, ""].ifEmpty { BuildConfig.AXE_APP_SIGNATURE }
            
            var savedId = Prefs[Prefs.REMOTE_GATEWAY_SESSION_ID, ""]
            if (savedId.isEmpty()) {
                savedId = UUID.randomUUID().toString()
                Prefs[Prefs.REMOTE_GATEWAY_SESSION_ID] = savedId
            }

            RemoteGatewayManager(
                token = Prefs[Prefs.TOKEN, ""],
                userId = Prefs[Prefs.USER_ID, ""],
                appSignature = customSignature,
                serverBaseUrl = customUrl,
                sessionId = savedId,
                logger = logger
            )
        } else {
            DiscordWebSocketImpl(Prefs[Prefs.TOKEN, ""], logger)
        }
    }

    @Provides
    fun provideAxeRpc(
        axeRepository: AxeRepository,
        discordWebSocket: DiscordWebSocket,
        logger: Logger
    ) = AxeRPC(Prefs[Prefs.TOKEN, ""], axeRepository, discordWebSocket, logger)

    @Provides
    fun providesCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}