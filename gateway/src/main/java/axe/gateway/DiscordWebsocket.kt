package axe.gateway

import axe.gateway.entities.presence.Presence
import kotlinx.coroutines.CoroutineScope

interface DiscordWebSocket: CoroutineScope {
    suspend fun connect()
    suspend fun sendActivity(presence: Presence)
    fun isWebSocketConnected(): Boolean
    fun close()
}