/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * ConfigUtils.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.axe.data.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import com.blankj.utilcode.util.FileIOUtils
import com.my.axe.data.rpc.Constants
import com.my.axe.domain.model.rpc.RpcConfig
import com.my.axe.preference.Prefs
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FilenameFilter

object ConfigUtils {
    val json = Json {
        ignoreUnknownKeys = true
    }

    val FILE_FILTER = FilenameFilter { _: File?, f: String ->
        f.endsWith(".json")
    }

    fun getConfigDir(context: Context): File {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            File(context.filesDir, "Configs")
        else {
            val selected = Prefs[Prefs.CONFIGS_DIRECTORY, Constants.DOWNLOADS_DIRECTORY]
            if (selected == Constants.DOWNLOADS_DIRECTORY)
                File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "axe"
                )
            else
                File(context.filesDir, "Configs")
        }
    }

    fun getAllConfigs(context: Context): List<String> {
        val dir = getConfigDir(context)
        if (!dir.exists()) return emptyList()
        return dir.list(FILE_FILTER)?.map { it.removeSuffix(".json") }?.sorted() ?: emptyList()
    }

    fun loadConfig(context: Context, configName: String): RpcConfig? {
        val file = File(getConfigDir(context), "$configName.json")
        if (!file.exists()) return null
        return try {
            val jsonString = FileIOUtils.readFile2String(file)
            json.decodeFromString<RpcConfig>(jsonString)
        } catch (e: Exception) {
            null
        }
    }

    fun RpcConfig.dataToString(): String {
        return json.encodeToString(this)
    }
}
