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

import com.my.axe.data.rpc.axeRPC
import com.my.axe.domain.interfaces.Logger
import com.my.axe.domain.repository.axeRepository
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

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {
    @Provides
    fun providesDiscordWebsocket(
        logger: Logger
    ): DiscordWebSocket =
        DiscordWebSocketImpl(Prefs[Prefs.TOKEN, ""], logger)

    @Provides
    fun provideaxeRpc(
        axeRepository: axeRepository,
        discordWebSocket: DiscordWebSocket,
        logger: Logger
    ) = axeRPC(Prefs[Prefs.TOKEN, ""], axeRepository, discordWebSocket, logger)

    @Provides
    fun providesCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}