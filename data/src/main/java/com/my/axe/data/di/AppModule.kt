/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * AppModule.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.axe.data.di

import com.my.axe.data.BuildConfig
import com.my.axe.data.remote.ApiService
import com.my.axe.data.remote.Base
import com.my.axe.data.remote.CdnService
import com.my.axe.data.remote.Discord
import com.my.axe.data.remote.Github
import com.my.axe.data.remote.Imgur
import com.my.axe.data.remote.ImgurApiService
import com.my.axe.data.remote.WebhookService
import com.my.axe.data.repository.AxeRepositoryImpl
import com.my.axe.domain.repository.AxeRepository
import com.my.axe.preference.Prefs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    @Base
    fun provideBaseUrl() = Prefs[Prefs.CUSTOM_API_BASE_URL, ""].ifEmpty { BuildConfig.BASE_URL }

    @Provides
    @Singleton
    @Discord
    fun provideDiscordBaseUrl() = BuildConfig.DISCORD_API_BASE_URL

    @Provides
    @Singleton
    @Github
    fun provideGithubBaseUrl() = BuildConfig.GITHUB_API_BASE_URL

    @Provides
    @Singleton
    @Imgur
    fun provideImgurBaseUrl() = BuildConfig.IMGUR_API_BASE_URL

    @Provides
    fun provideJson() = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Provides
    fun provideHttpClient(
        json: Json,
        kLogger: com.my.axe.domain.interfaces.Logger
    ): HttpClient {
        return HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(json)
            }
            install(HttpTimeout) {
                connectTimeoutMillis = 30_000
                requestTimeoutMillis = 30_000
                socketTimeoutMillis = 30_000
            }
            install(Logging) {
                level = LogLevel.HEADERS
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
                logger = object : Logger {
                    override fun log(message: String) {
                        kLogger.d("Ktor", message)
                    }
                }
            }
        }
    }

    @Provides
    @Singleton
    fun provideCdnService(): CdnService {
        return CdnService()
    }

    @Provides
    @Singleton
    fun provideWebhookService(
        client: HttpClient
    ): WebhookService {
        return WebhookService(client)
    }

    @Provides
    fun provideAxeRepository(
        apiService: ApiService,
        imgurApiService: ImgurApiService
    ): AxeRepository {
        return AxeRepositoryImpl(apiService, imgurApiService)
    }
}