package com.my.axe.feature_rpc_base.detection

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.SortedMap
import java.util.TreeMap

class UsageStatsDetectionStrategy(private val context: Context) : DetectionStrategy {
    override fun getForegroundApp(): String? {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val currentTimeMillis = System.currentTimeMillis()
        val queryUsageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            currentTimeMillis - 10000,
            currentTimeMillis
        )

        if (queryUsageStats != null && queryUsageStats.size > 1) {
            val treeMap: SortedMap<Long, UsageStats> = TreeMap()
            for (usageStatsItem in queryUsageStats) {
                treeMap[usageStatsItem.lastTimeUsed] = usageStatsItem
            }
            return treeMap.lastKey()?.let { treeMap[it]?.packageName }
        }
        return null
    }
}
