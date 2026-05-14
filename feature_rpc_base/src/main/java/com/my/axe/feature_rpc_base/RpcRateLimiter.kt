package com.my.axe.feature_rpc_base

import java.util.concurrent.ConcurrentHashMap

/**
 * Proper rate limiting (minimum 15 seconds per source app)
 */
class RpcRateLimiter(private val minIntervalMillis: Long = 15000L) {
    private val lastUpdateMap = ConcurrentHashMap<String, Long>()

    fun canUpdate(sourcePackage: String): Boolean {
        val currentTime = System.currentTimeMillis()
        val lastUpdate = lastUpdateMap[sourcePackage] ?: 0L
        
        return if (currentTime - lastUpdate >= minIntervalMillis) {
            lastUpdateMap[sourcePackage] = currentTime
            true
        } else {
            false
        }
    }

    fun clear(sourcePackage: String) {
        lastUpdateMap.remove(sourcePackage)
    }
}
