package com.my.axe.feature_rpc_base.detection

import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

class ShizukuDetectionStrategy : DetectionStrategy {
    override fun getForegroundApp(): String? {
        if (!Shizuku.pingBinder()) return null
        if (Shizuku.checkSelfPermission() != android.content.pm.PackageManager.PERMISSION_GRANTED) return null

        return try {
            val process = Shizuku.newProcess(arrayOf("dumpsys", "window", "visible-apps"), null, null)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (line?.contains("resumed=true") == true) {
                    // Extract package name from line like:
                    //   Window #5 Window{... u0 com.android.settings/com.android.settings.Settings resumed=true}
                    val match = Regex("""\s([a-zA-Z0-9._]+)/""").find(line!!)
                    return match?.groupValues?.get(1)
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}
