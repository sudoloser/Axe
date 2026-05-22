/*
 *
 *  ******************************************************************
 *  *  * Copyright (C) 2022
 *  *  * axeRPC.kt is part of Kizzy
 *  *  *  and can not be copied and/or distributed without the express
 *  *  * permission of yzziK(Vaibhav)
 *  *  *****************************************************************
 *
 *
 */

package com.my.axe.data.rpc

import com.my.axe.domain.interfaces.Logger
import com.my.axe.domain.repository.AxeRepository
import com.my.axe.preference.Prefs
import com.my.axe.preference.Prefs.CUSTOM_ACTIVITY_TYPE
import axe.gateway.DiscordWebSocket
import axe.gateway.entities.presence.Activity
import axe.gateway.entities.presence.Assets
import axe.gateway.entities.presence.Metadata
import axe.gateway.entities.presence.Party
import axe.gateway.entities.presence.Presence
import axe.gateway.entities.presence.Timestamps
import kotlinx.coroutines.isActive

class AxeRPC(
    private val token: String,
    private val axeRepository: AxeRepository,
    private val discordWebSocket: DiscordWebSocket,
    private val logger: Logger
) {
    private lateinit var presence: Presence
    private var activityName: String? = null
    private fun getApplicationId() = Prefs[Prefs.CUSTOM_ACTIVITY_APPLICATION_ID, Constants.APPLICATION_ID]
    private var details: String? = null
    private var state: String? = null
    private var party: Party? = null
    private var largeImage: RpcImage? = null
    private var smallImage: RpcImage? = null
    private var largeText: String? = null
    private var smallText: String? = null
    private var status: String? = null
    private var startTimestamps: Long? = null
    private var stopTimestamps: Long? = null
    private var type: Int = 0
    private var platform: String? = null
    private var buttons = ArrayList<String>()
    private var buttonUrl = ArrayList<String>()
    private var button1: String? = null
    private var button2: String? = null
    private var button1Url: String? = null
    private var button2Url: String? = null
    private var url: String? = null

    fun closeRPC() {
        discordWebSocket.close()
        clearState()
    }

    fun clearState() {
        activityName = null
        details = null
        state = null
        party = null
        largeImage = null
        smallImage = null
        largeText = null
        smallText = null
        status = null
        startTimestamps = null
        stopTimestamps = null
        type = 0
        platform = null
        button1 = null
        button2 = null
        button1Url = null
        button2Url = null
        url = null
        buttons.clear()
        buttonUrl.clear()
    }

    fun isRpcRunning(): Boolean {
        return discordWebSocket.isWebSocketConnected()
    }

    /**
     * Use Regex to check if token are valid
     * @return true if token is valid else false
     *
     * source: [#token-structure](https://gist.github.com/aydynx/5d29e903417354fd25641b98efc9d437#token-structure)
     */
    private fun isUserTokenValid(): Boolean {
        /*val regex = Regex(
            "[a-z\\d]{24}\\.[a-z\\d]{6}\\.([\\w-]{107}|[\\w-]{38}|[\\w-]{27})|mfa\\.[\\w-]{84}",
            RegexOption.IGNORE_CASE
        )
        return regex.matches(token)*/
        return token.isNotBlank()
    }

    /**
     * Activity Name of Rpc
     *
     * @param activity_name
     * @return
     */
    fun setName(activity_name: String?): AxeRPC {
        this.activityName = activity_name
        return this
    }

    /**
     * Details of Rpc
     *
     * @param details
     * @return
     */
    fun setDetails(details: String?): AxeRPC {
        this.details = details
        return this
    }

    /**
     * Rpc State
     *
     * @param state
     * @return
     */
    fun setState(state: String?): AxeRPC {
        this.state = state
        return this
    }

    /**
     * Party of Rpc
     *
     * @param current Current party size
     * @param max Max party size
     * @return
     */
    fun setPartySize(current: Int?, max: Int?): AxeRPC {
        if (current != null && max != null) {
            this.party = Party(
                id = "axe",
                size = arrayOf(current, max)
            )
        }
        return this
    }

    /**
     * Large image on rpc
     * How to get Image ?
     * Upload image to any discord chat and copy its media link it should look like "https://media.discordapp.net/attachments/90202992002/xyz.png" now just use the image link from attachments part
     * so it would look like: .setLargeImage("attachments/90202992002/xyz.png")
     * @param large_image
     * @return
     */
    fun setLargeImage(large_image: RpcImage?, large_text: String? = null): AxeRPC {
        this.largeText = large_text.takeIf { !it.isNullOrEmpty() }
        this.largeImage = large_image
        return this
    }

    /**
     * Small image on Rpc
     *
     * @param small_image
     * @return
     */
    fun setSmallImage(small_image: RpcImage?, small_text: String? = null): AxeRPC {
        this.smallText = small_text.takeIf { !it.isNullOrEmpty() }
        this.smallImage = small_image
        return this
    }

    /**
     * start timestamps
     *
     * @param start_timestamps
     * @return
     */
    fun setStartTimestamps(start_timestamps: Long?): AxeRPC {
        this.startTimestamps = start_timestamps
        return this
    }

    /**
     * stop timestamps
     *
     * @param stop_timestamps
     * @return
     */
    fun setStopTimestamps(stop_timestamps: Long?): AxeRPC {
        this.stopTimestamps = stop_timestamps
        return this
    }

    /**
     * Activity Types
     * 0: Playing
     * 1: Streaming
     * 2: Listening
     * 3: Watching
     * 5: Competing
     *
     * @param type
     * @return
     */

    fun setType(type: Int): AxeRPC {
        if (type in 0..5)
            this.type = type
        else this.type = 0
        return this
    }

    /** Status type for profile online,idle,dnd
     *
     * @param status
     * @return
     */
    fun setStatus(status: String?): AxeRPC {
        this.status = status
        return this
    }

    /** Platform type for profile xbox,playstation,pc,ios,android etc.
     *
     * @param platform
     * @return
     */
    fun setPlatform(platform: String?): AxeRPC {
        this.platform = platform
        return this
    }

    /**
     * Button1 text
     * @param button1_Text
     * @return
     */
    fun setButton1(button1_Text: String?): AxeRPC {
        this.button1 = button1_Text
        return this
    }

    /**
     * Button2 text
     * @param button2_text
     * @return
     */
    fun setButton2(button2_text: String?): AxeRPC {
        this.button2 = button2_text
        return this
    }

    /**
     * Button1 url
     * @param url
     * @return
     */
    fun setButton1URL(url: String?): AxeRPC {
        this.button1Url = url
        return this
    }

    /**
     * Button2 url
     * @param url
     * @return
     */
    fun setButton2URL(url: String?): AxeRPC {
        this.button2Url = url
        return this
    }
    /**
     * Streaming Url
     * @param url The streaming type currently only supports Twitch and YouTube.
     * Only https://twitch.tv/ and https://youtube.com/ urls will work
     */
    fun setStreamUrl(url: String?): AxeRPC {
        this.url = url
        return this
    }

    private fun String.sanitize(): String {
        return if (this.length > 128) {
            this.substring(0, 128)
        } else {
            this
        }
    }

    suspend fun build() {
        val buttonsList = mutableListOf<String>()
        val buttonUrlsList = mutableListOf<String>()

        button1?.let { label ->
            button1Url?.let { url ->
                buttonsList.add(label)
                buttonUrlsList.add(url)
            }
        }
        button2?.let { label ->
            button2Url?.let { url ->
                buttonsList.add(label)
                buttonUrlsList.add(url)
            }
        }

        presence = Presence(
            activities = listOf(
                Activity(
                    name = activityName,
                    state = state?.sanitize(),
                    details = details?.sanitize(),
                    party = party.takeIf { party != null },
                    type = type,
                    platform = platform?.sanitize(),
                    timestamps = Timestamps(
                        start = startTimestamps,
                        end = stopTimestamps
                    ).takeIf { startTimestamps != null || stopTimestamps != null },
                    assets = Assets(
                        largeImage = largeImage?.resolveImage(axeRepository),
                        smallImage = smallImage?.resolveImage(axeRepository),
                        largeText = largeText?.sanitize(),
                        smallText = smallText?.sanitize()
                    ).takeIf { largeImage != null || smallImage != null },
                    buttons = buttonsList.takeIf { buttonsList.isNotEmpty() },
                    metadata = Metadata(buttonUrls = buttonUrlsList).takeIf { buttonUrlsList.isNotEmpty() },
                    applicationId = getApplicationId().takeIf { it.isNotEmpty() } ?: Constants.APPLICATION_ID,
                    url = url
                )
            ),
            afk = true,
            since = startTimestamps.takeIf { startTimestamps != null }?: System.currentTimeMillis(),
            status = status
        )
        connectToWebSocket()
    }
    private suspend fun connectToWebSocket() {
        if (!isUserTokenValid())
            logger.e(
                tag = "axeRPC",
                event = "Token Seems to be invalid, Please Login if you haven't"
            )
        
        if (!::presence.isInitialized) {
            logger.e("axeRPC", "Cannot connect: Presence is not initialized. Call build() first.")
            return
        }

        discordWebSocket.connect()
        discordWebSocket.sendActivity(presence)
    }

    suspend fun updateRPC(commonRpc: CommonRpc, enableTimestamps: Boolean? = true) {
        if (!discordWebSocket.isActive) return
        var time = Timestamps(start = startTimestamps)
        if (commonRpc.time != null)
            Timestamps(end = commonRpc.time.end, start = commonRpc.time.start).also { time = it }
        if (commonRpc.partyCurrentSize != null && commonRpc.partyMaxSize != null)
            Party(id = "axe", size = arrayOf(commonRpc.partyCurrentSize, commonRpc.partyMaxSize)).also { party = it }

        val buttonsList = mutableListOf<String>()
        val buttonUrlsList = mutableListOf<String>()

        button1?.let { label ->
            button1Url?.let { url ->
                buttonsList.add(label)
                buttonUrlsList.add(url)
            }
        }
        button2?.let { label ->
            button2Url?.let { url ->
                buttonsList.add(label)
                buttonUrlsList.add(url)
            }
        }

        discordWebSocket.sendActivity(
            Presence(
                activities = listOf(
                    Activity(
                        name = commonRpc.name,
                        details = commonRpc.details?.takeIf { it.isNotEmpty() }?.sanitize(),
                        state = commonRpc.state?.takeIf { it.isNotEmpty() }?.sanitize(),
                        type = commonRpc.type ?: Prefs[CUSTOM_ACTIVITY_TYPE, 0],
                        platform = commonRpc.platform?.sanitize(),
                        timestamps = time.takeIf { enableTimestamps == true },
                        assets = Assets(
                                largeImage = commonRpc.largeImage?.resolveImage(axeRepository),
                                smallImage = commonRpc.smallImage?.resolveImage(axeRepository),
                                largeText = commonRpc.largeText?.sanitize(),
                                smallText = commonRpc.smallText?.sanitize()
                            ).takeIf { commonRpc.largeImage != null || commonRpc.smallImage != null },
                        party = party.takeIf { party != null },
                        buttons = buttonsList.takeIf { buttonsList.isNotEmpty() },
                        metadata = Metadata(buttonUrls = buttonUrlsList).takeIf { buttonUrlsList.isNotEmpty() },
                        applicationId = getApplicationId().takeIf { it.isNotEmpty() } ?: Constants.APPLICATION_ID

                    )
                ),
                afk = true,
                since = startTimestamps,
                status = this.status
            )
        )
    }
}
