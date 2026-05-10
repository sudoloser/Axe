package com.my.axe.feature_rpc_base.di

import com.my.axe.feature_rpc_base.RpcRateLimiter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RpcPublicApiModule {

    @Provides
    @Singleton
    fun provideRpcRateLimiter(): RpcRateLimiter {
        return RpcRateLimiter()
    }
}
