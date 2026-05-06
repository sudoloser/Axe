/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * axeRepositoryImpl.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.axe.data.repository

import com.my.axe.data.remote.ApiService
import com.my.axe.data.remote.GamesResponse
import com.my.axe.data.remote.ImgurApiService
import com.my.axe.data.remote.toGame
import com.my.axe.data.rpc.Constants
import com.my.axe.data.utils.toAttachmentAsset
import com.my.axe.data.utils.toExternalAsset
import com.my.axe.data.utils.toImageURL
import com.my.axe.domain.model.Contributor
import com.my.axe.domain.model.Game
import com.my.axe.domain.model.release.Release
import com.my.axe.domain.model.user.User
import com.my.axe.domain.repository.axeRepository
import com.my.axe.preference.Prefs
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import java.io.File
import javax.inject.Inject

class axeRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val imgurApi: ImgurApiService,
) : axeRepository {

    override suspend fun getImage(url: String): String? {
        return if (Prefs[Prefs.USE_IMGUR, false]) {
            imgurApi.getImage(url, Prefs[Prefs.TOKEN]).getOrNull()?.toExternalAsset()
        } else {
            api.getImage(url).getOrNull()?.toAttachmentAsset()
        }
    }

    override suspend fun uploadImage(file: File): String? {
        return if (Prefs[Prefs.USE_IMGUR, false]) {
            imgurApi.uploadImage(file, Prefs[Prefs.IMGUR_CLIENT_ID, Constants.IMGUR_CLIENT_ID])
                .getOrNull()?.toImageURL()?.let { this.getImage(it) }
        } else {
            api.uploadImage(file).getOrNull()?.toAttachmentAsset()
        }
    }

    override suspend fun getGames(): List<Game> {
        return api.getGames().getOrNull()?.body<List<GamesResponse>>()?.map { it.toGame() }
            ?: emptyList()
    }

    override suspend fun getUser(userid: String): User {
        return api.getUser(userid).getOrNull()?.body() ?: User()
    }

    override suspend fun getContributors(): List<Contributor> {
        return api.getContributors().getOrNull()?.body() ?: emptyList()
    }

    override suspend fun checkForUpdate(): Release {
        return api.checkForUpdate().getOrNull()?.releaseBody() ?: Release()
    }
}

suspend fun HttpResponse.releaseBody(): Release {
    return if (this.status.value == 200) {
        Prefs.saveLatestRelease(this.body())
        this.body()
    } else {
        Prefs.getSavedLatestRelease() ?: Release()
    }
}