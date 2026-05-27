package com.my.axe.data.gateway

import com.my.axe.data.BuildConfig
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
    private var token: String,
    private var userId: String,
    private var appSignature: String,
    private var serverBaseUrl: String,
    private val logger: Logger
) : DiscordWebSocket {

    private var currentImplementation: DiscordWebSocket? = null
    private var useRemote = Prefs[Prefs.USE_REMOTE_GATEWAY, false]

    private val _sessionActive = MutableStateFlow(false)
    override val sessionActive: StateFlow<Boolean> = _sessionActive.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    override val coroutineContext: CoroutineContext = scope.coroutineContext

    init {
        updateImplementation()
        scope.launch {
            Prefs.preferenceChanges.collect { key ->
                when (key) {
                    Prefs.USE_REMOTE_GATEWAY,
                    Prefs.TOKEN,
                    Prefs.USER_ID,
                    Prefs.REMOTE_GATEWAY_URL,
                    Prefs.REMOTE_GATEWAY_SIGNATURE -> {
                        logger.i("DelegatingGateway", "Preference changed: $key. Updating state and implementation.")
                        // Update local state from Prefs
                        token = Prefs[Prefs.TOKEN, ""]
                        userId = Prefs[Prefs.USER_ID, ""]
                        serverBaseUrl = Prefs[Prefs.REMOTE_GATEWAY_URL, "https://axe-server.onrender.com/"]
                        appSignature = Prefs[Prefs.REMOTE_GATEWAY_SIGNATURE, ""].ifEmpty { BuildConfig.AXE_APP_SIGNATURE }
                        
                        updateImplementation(forceUpdate = true)
                    }
                }
            }
        }
    }

    private var sessionActiveSyncJob: Job? = null

    private fun updateImplementation(forceUpdate: Boolean = false) {
        val newUseRemote = Prefs[Prefs.USE_REMOTE_GATEWAY, false]
        if (currentImplementation == null || newUseRemote != useRemote || forceUpdate) {
            logger.i("DelegatingGateway", "Updating implementation. Remote: $newUseRemote, Forced: $forceUpdate")
            
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

            // If switching TO remote or forced update, try to refresh immediately
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
        logger.i("DelegatingGateway", "Close called. Nulling implementation.")
        sessionActiveSyncJob?.cancel()
        currentImplementation?.close()
        currentImplementation = null
        _sessionActive.value = false
    }
}