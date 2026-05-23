package com.my.axe.data.gateway

import android.content.SharedPreferences
import axe.gateway.DiscordWebSocket
import axe.gateway.DiscordWebSocketImpl
import axe.gateway.RemoteGatewayManager
import axe.gateway.entities.presence.Presence
import com.my.axe.domain.interfaces.Logger
import com.my.axe.preference.Prefs
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.CoroutineContext

class DelegatingDiscordWebSocket(
    private val token: String,
    private val userId: String,
    private val appSignature: String,
    private val serverBaseUrl: String,
    private val logger: Logger
) : DiscordWebSocket {

    private var currentImplementation: DiscordWebSocket? = null
    private var useRemote = Prefs[Prefs.USE_REMOTE_GATEWAY, false]

    private val _sessionActive = MutableStateFlow(false)
    override val sessionActive: StateFlow<Boolean> = _sessionActive.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    override val coroutineContext: CoroutineContext = scope.coroutineContext

    private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == Prefs.USE_REMOTE_GATEWAY) {
            logger.i("DelegatingGateway", "Preference changed: $key. Updating implementation.")
            updateImplementation()
        }
    }

    init {
        updateImplementation()
        Prefs.getPrefs().registerOnSharedPreferenceChangeListener(prefListener)
    }

    private var sessionActiveSyncJob: Job? = null

    private fun updateImplementation() {
        val newUseRemote = Prefs[Prefs.USE_REMOTE_GATEWAY, false]
        if (currentImplementation == null || newUseRemote != useRemote) {
            logger.i("DelegatingGateway", "Switching implementation. Remote: $newUseRemote")
            
            // If we are currently connected, we should close the current implementation
            // and potentially reconnect with the new one if the RPC was active.
            val wasConnected = currentImplementation?.isWebSocketConnected() ?: false
            
            currentImplementation?.close()
            sessionActiveSyncJob?.cancel()
            
            useRemote = newUseRemote
            currentImplementation = if (useRemote) {
                RemoteGatewayManager(
                    token = token,
                    userId = userId,
                    appSignature = appSignature,
                    serverBaseUrl = serverBaseUrl,
                    sessionId = "",
                    logger = logger
                )
            } else {
                DiscordWebSocketImpl(token, logger)
            }
            
            // Sync sessionActive from the current implementation
            sessionActiveSyncJob = scope.launch {
                currentImplementation?.sessionActive?.collect {
                    _sessionActive.value = it
                }
            }

            // If switching TO remote, try to refresh immediately to see if a session exists
            if (useRemote) {
                currentImplementation?.refreshSession()
            }
            
            // If it was connected, attempt to reconnect the new implementation
            if (wasConnected) {
                scope.launch {
                    currentImplementation?.connect()
                }
            }
        }
    }

    override suspend fun connect() {
        updateImplementation()
        currentImplementation?.connect()
    }

    override suspend fun sendActivity(presence: Presence) {
        updateImplementation()
        currentImplementation?.sendActivity(presence)
    }

    override fun isWebSocketConnected(): Boolean {
        updateImplementation()
        return currentImplementation?.isWebSocketConnected() ?: false
    }

    override fun refreshSession() {
        updateImplementation()
        currentImplementation?.refreshSession()
    }

    override fun close() {
        Prefs.getPrefs().unregisterOnSharedPreferenceChangeListener(prefListener)
        sessionActiveSyncJob?.cancel()
        currentImplementation?.close()
    }
}