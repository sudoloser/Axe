package axe.gateway

import axe.gateway.entities.presence.Presence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface DiscordWebSocket: CoroutineScope {
    val sessionActive: StateFlow<Boolean>
    suspend fun connect()
    suspend fun sendActivity(presence: Presence)
    fun isWebSocketConnected(): Boolean
    fun refreshSession() {}
    fun close()
}