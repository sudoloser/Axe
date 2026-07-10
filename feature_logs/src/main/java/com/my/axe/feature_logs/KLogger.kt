/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * KLogger.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.axe.feature_logs

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.my.axe.domain.interfaces.Logger
import com.my.axe.domain.model.logs.LogEvent
import com.my.axe.domain.model.logs.LogLevel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KLogger: Logger {
    private val logs = mutableStateListOf<LogEvent>()
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)

    fun getLogs(): SnapshotStateList<LogEvent> {
        return logs
    }

    override fun clear() {
        synchronized(logs) { logs.clear() }
    }

    override fun i(tag: String, event: String) {
        addToLog(LogLevel.INFO, tag, event)
    }

    override fun e(tag: String, event: String) {
        addToLog(LogLevel.ERROR, tag, event)
    }

    override fun d(tag: String, event: String) {
        addToLog(LogLevel.DEBUG, tag, event)
    }

    override fun w(tag: String, event: String) {
        addToLog(LogLevel.WARN, tag, event)
    }

    override fun getFormattedLogs(maxLines: Int): String {
        synchronized(logs) {
            val recent = logs.takeLast(maxLines)
            return recent.joinToString("\n") { log ->
                val time = timeFormat.format(Date(log.createdAt))
                "[$time][${log.level.name}] ${log.tag}: ${log.text}"
            }
        }
    }

    private fun addToLog(level: LogLevel, tag: String, event: String) {
        synchronized(logs) {
            if (logs.size > 300) {
                logs.removeAt(0)
            }
            val log = LogEvent(level, tag, event, System.currentTimeMillis())
            logs.add(log)
        }
    }

    companion object {
        private var instance: KLogger? = null
        @JvmStatic
        fun getInstance(): KLogger? {
            var localInstance = instance
            if (localInstance == null) {
                synchronized(KLogger::class.java) {
                    localInstance = instance
                    if (localInstance == null) {
                        localInstance = KLogger()
                        instance = localInstance
                    }
                }
            }
            return localInstance
        }

        @JvmStatic
        fun init() {
            if (instance == null) {
                instance = KLogger()
            }
        }
    }
}