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
import com.my.axe.data.gateway.DelegatingDiscordWebSocket
import axe.gateway.DiscordWebSocket
import com.my.axe.data.rpc.AxeRPC
import com.my.axe.domain.interfaces.Logger
import com.my.axe.domain.repository.AxeRepository
import com.my.axe.preference.Prefs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    @Provides
    @Singleton
    fun providesDiscordWebsocket(
        logger: Logger
    ): DiscordWebSocket {
        return DelegatingDiscordWebSocket(
            logger = logger
        )
    }

    @Provides
    @Singleton
    fun provideAxeRpc(
        axeRepository: AxeRepository,
        discordWebSocket: DiscordWebSocket,
        logger: Logger
    ) = AxeRPC(axeRepository, discordWebSocket, logger)

    @Provides
    @Singleton
    fun providesCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}
