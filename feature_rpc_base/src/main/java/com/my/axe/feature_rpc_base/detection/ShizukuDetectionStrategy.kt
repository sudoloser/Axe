package com.my.axe.feature_rpc_base.detection

import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import rikka.shizuku.Shizuku

class ShizukuDetectionStrategy : DetectionStrategy {
    override fun getForegroundApp(): String? {
        if (!Shizuku.pingBinder()) return null
        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) return null

        return try {
            val isAtLeastQ = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
            val serviceName = if (isAtLeastQ) "activity_task" else "activity"
            
            val serviceBinder = getServiceBinder(serviceName) ?: return null
            val wrappedBinder = Shizuku.newBinderWrapper(serviceBinder)
            
            val stubClass = if (isAtLeastQ) "android.app.IActivityTaskManager\$Stub" else "android.app.IActivityManager\$Stub"
            val iInterface = Class.forName(stubClass)
                .getMethod("asInterface", IBinder::class.java)
                .invoke(null, wrappedBinder)

            val tasks = if (isAtLeastQ) {
                iInterface.javaClass.getMethod("getTasks", Int::class.java, Boolean::class.java, Boolean::class.java)
                    .invoke(iInterface, 1, false, false) as List<*>
            } else {
                iInterface.javaClass.getMethod("getTasks", Int::class.java, Int::class.java)
                    .invoke(iInterface, 1, 0) as List<*>
            }

            val taskInfo = tasks.firstOrNull() ?: return null
            val topActivity = taskInfo.javaClass.getField("topActivity").get(taskInfo)
            topActivity?.javaClass?.getMethod("getPackageName")?.invoke(topActivity) as? String
            
        } catch (e: Exception) {
            null
        }
    }

    private fun getServiceBinder(name: String): IBinder? {
        return try {
            Class.forName("android.os.ServiceManager")
                .getMethod("getService", String::class.java)
                .invoke(null, name) as? IBinder
        } catch (e: Exception) {
            null
        }
    }
}
