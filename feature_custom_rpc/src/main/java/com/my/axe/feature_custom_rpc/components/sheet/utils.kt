/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * utils.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.axe.feature_custom_rpc.components.sheet

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import com.blankj.utilcode.util.FileIOUtils
import com.my.axe.data.utils.ConfigUtils
import com.my.axe.data.utils.getFileName
import com.my.axe.domain.model.rpc.RpcConfig
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

internal val FILE_FILTER = ConfigUtils.FILE_FILTER

internal fun Context.dir() = ConfigUtils.getConfigDir(this)

internal fun Context.handleUriResult(uri: Uri?, onSuccess: (json: String) -> Unit) {
    if (uri == null)
        return
    val fileName = this.getFileName(uri)
    if (!fileName.endsWith(".json"))
        return

    val file = File(this.cacheDir, "tmp.json")
    val inputStream = this.contentResolver.openInputStream(uri)
    inputStream?.use { input ->
        file.outputStream().use { out ->
            input.copyTo(out)
        }
    }
    FileIOUtils.readFile2String(file).also { json ->
        onSuccess(json)
    }
}
internal val json = Json {
    ignoreUnknownKeys = true
}

internal fun RpcConfig.dataToString(): String {
    return json.encodeToString(this)
}


internal fun String.stringToData(): RpcConfig {
    return try {
        return json.decodeFromString(this)
    } catch (ex: Exception) {
        RpcConfig()
    }
}