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
            return if (image.startsWith("mp:")) image else "mp:${image}"
        }
    }

    class ExternalImage(val image: String) : RpcImage() {
        override suspend fun resolveImage(repository: AxeRepository): String? {
            return if (resolvedCache.containsKey(image)) {
                resolvedCache[image]
            } else {
                val result = repository.getImage(image)
                result?.let { resolvedCache[image] = it }
                result
            }
        }
    }

    class ApplicationIcon(val packageName: String, private val context: Context) : RpcImage() {
        override suspend fun resolveImage(repository: AxeRepository): String? {
            val savedImages = getSavedImages()
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
                val savedImages = getSavedImages().toMutableMap()
                savedImages[packageName] = it
                saveImages(savedImages)
            }
            return response
        }

        private fun getSavedImages(): Map<String, String> {
            val data = Prefs[Prefs.SAVED_IMAGES, "{}"]
            return try { Json.decodeFromString(data) } catch (e: Exception) { emptyMap() }
        }

        private fun saveImages(images: Map<String, String>) {
            Prefs[Prefs.SAVED_IMAGES] = Json.encodeToString(images)
        }
    }

    class BitmapImage(
        private val context: Context,
        val bitmap: Bitmap?,
        private val packageName: String,
        val title: String,
    ) : RpcImage() {
        override suspend fun resolveImage(repository: AxeRepository): String? {
            val schema = "${this.packageName}:${this.title}"
            val savedArtwork = getSavedArtwork()
            return if (savedArtwork.containsKey(schema))
                savedArtwork[schema]
            else {
                val result = repository.uploadImage(bitmap.toFile(this.context, "art"))
                result?.let {
                    val updatedArtwork = getSavedArtwork().toMutableMap()
                    updatedArtwork[schema] = it
                    saveArtwork(updatedArtwork)
                }
                result
            }
        }

        private fun getSavedArtwork(): Map<String, String> {
            val data = Prefs[Prefs.SAVED_ARTWORK, "{}"]
            return try { Json.decodeFromString(data) } catch (e: Exception) { emptyMap() }
        }

        private fun saveArtwork(artwork: Map<String, String>) {
            Prefs[Prefs.SAVED_ARTWORK] = Json.encodeToString(artwork)
        }
    }

    companion object {
        private val resolvedCache = mutableMapOf<String, String>()
        
        fun clearCache() {
            resolvedCache.clear()
        }
    }
}
