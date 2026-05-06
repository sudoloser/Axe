/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * RpcImage.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.axe.data.rpc

import android.content.Context
import android.graphics.Bitmap
import com.my.axe.domain.repository.AxeRepository
import com.my.axe.preference.Prefs
import com.my.axe.data.utils.getAppInfo
import com.my.axe.data.utils.toBitmap
import com.my.axe.data.utils.toFile
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

sealed class RpcImage {
    abstract suspend fun resolveImage(repository: AxeRepository): String?

    class DiscordImage(val image: String) : RpcImage() {
        override suspend fun resolveImage(repository: AxeRepository): String {
            return "mp:${image}"
        }
    }

    class ExternalImage(val image: String) : RpcImage() {
        override suspend fun resolveImage(repository: AxeRepository): String? {
            return repository.getImage(image)
        }
    }

    class ApplicationIcon(val packageName: String, private val context: Context) : RpcImage() {
        val data = Prefs[Prefs.SAVED_IMAGES, "{}"]
        private val savedImages: HashMap<String, String> = Json.decodeFromString(data)

        override suspend fun resolveImage(repository: AxeRepository): String? {
            return if (savedImages.containsKey(packageName))
                savedImages[packageName]
            else
                retrieveImageFromApi(packageName, context, repository)
        }

        private suspend fun retrieveImageFromApi(
            packageName: String,
            context: Context,
            repository: AxeRepository,
        ): String? {
            val applicationInfo = context.getAppInfo(packageName)
            val bitmap = applicationInfo.toBitmap(context)
            val response = repository.uploadImage(bitmap.toFile(context, "image"))
            response?.let {
                savedImages[packageName] = it
                Prefs[Prefs.SAVED_IMAGES] = Json.encodeToString(savedImages)
            }
            return response
        }
    }

    class BitmapImage(
        private val context: Context,
        val bitmap: Bitmap?,
        private val packageName: String,
        val title: String,
    ) : RpcImage() {
        override suspend fun resolveImage(repository: AxeRepository): String? {
            val data = Prefs[Prefs.SAVED_ARTWORK, "{}"]
            val schema = "${this.packageName}:${this.title}"
            val savedImages = Json.decodeFromString<HashMap<String, String>>(data)
            return if (savedImages.containsKey(schema))
                savedImages[schema]
            else {
                val result = repository.uploadImage(bitmap.toFile(this.context, "art"))
                result?.let {
                    savedImages[schema] = it
                    Prefs[Prefs.SAVED_ARTWORK] = Json.encodeToString(savedImages)
                }
                result
            }
        }
    }
}
