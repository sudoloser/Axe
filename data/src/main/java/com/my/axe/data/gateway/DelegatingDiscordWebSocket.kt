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
    private val logger: Logger
) : DiscordWebSocket {

    private var currentImplementation: DiscordWebSocket? = null
    private var useRemote = Prefs[Prefs.USE_REMOTE_GATEWAY, false]
    private var lastPresence: Presence? = null

    // Track the preferences used to create the current implementation
    private var activeToken = ""
    private var activeUserId = ""
    private var activeServerUrl = ""
    private var activeSignature = ""

    private val _sessionActive = MutableStateFlow(false)
    override val sessionActive: StateFlow<Boolean> = _sessionActive.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    override val coroutineContext: CoroutineContext = scope.coroutineContext

    init {
        logger.i("DelegatingGateway", "Initializing with remote: $useRemote")
        updateImplementation(recreateIfNull = true)
        scope.launch {
            Prefs.preferenceChanges.collect { key ->
                when (key) {
                    Prefs.USE_REMOTE_GATEWAY,
                    Prefs.TOKEN,
                    Prefs.USER_ID,
                    Prefs.REMOTE_GATEWAY_URL,
                    Prefs.REMOTE_GATEWAY_SIGNATURE -> {
                        logger.i("DelegatingGateway", "Preference changed: $key. Forcing update.")
                        updateImplementation(forceUpdate = true)
                    }
                }
            }
        }
    }

    private var sessionActiveSyncJob: Job? = null

    private fun updateImplementation(
        forceUpdate: Boolean = false,
        recreateIfNull: Boolean = false
    ) {
        val newUseRemote = Prefs[Prefs.USE_REMOTE_GATEWAY, false]
        val token = Prefs[Prefs.TOKEN, ""]
        val userId = Prefs[Prefs.USER_ID, ""]
        val serverBaseUrl = Prefs[Prefs.REMOTE_GATEWAY_URL, "https://axe-server.onrender.com/"]
        val appSignature = Prefs[Prefs.REMOTE_GATEWAY_SIGNATURE, ""].ifEmpty { BuildConfig.AXE_APP_SIGNATURE }

        val prefsChanged = token != activeToken ||
                userId != activeUserId ||
                serverBaseUrl != activeServerUrl ||
                appSignature != activeSignature

        val shouldUpdate = (currentImplementation == null && recreateIfNull) ||
                (currentImplementation != null && (newUseRemote != useRemote || prefsChanged || forceUpdate || !currentImplementation!!.isActive))

        if (shouldUpdate) {
            logger.i("DelegatingGateway", "Updating implementation. Remote: $newUseRemote, Forced: $forceUpdate, RecreateIfNull: $recreateIfNull, PrefsChanged: $prefsChanged")
            
            // Check if it was connected OR active in any way
            val wasConnected = currentImplementation?.isWebSocketConnected() ?: false
            val wasActive = _sessionActive.value
            val wasNull = currentImplementation == null
            
            if (currentImplementation != null) {
                logger.i("DelegatingGateway", "Closing old implementation (wasConnected: $wasConnected, wasActive: $wasActive)")
                currentImplementation?.close()
            }
            
            sessionActiveSyncJob?.cancel()
            
            useRemote = newUseRemote
            activeToken = token
            activeUserId = userId
            activeServerUrl = serverBaseUrl
            activeSignature = appSignature

            currentImplementation = if (useRemote) {
                logger.i("DelegatingGateway", "Creating RemoteGatewayManager at $serverBaseUrl")
                RemoteGatewayManager(
                    token = token,
                    userId = userId,
                    appSignature = appSignature,
                    serverBaseUrl = serverBaseUrl,
                    logger = logger
                )
            } else {
                logger.i("DelegatingGateway", "Creating DiscordWebSocketImpl")
                DiscordWebSocketImpl(token, logger)
            }
            
            // Sync sessionActive from the current implementation
            sessionActiveSyncJob = scope.launch {
                currentImplementation?.sessionActive?.collect {
                    _sessionActive.value = it
                }
            }

            // If switching TO remote or forced update, try to refresh immediately
            // But skip if it was previously null to avoid faking sessionActive on pollers
            if (useRemote && !wasNull) {
                currentImplementation?.refreshSession()
            }
            
            // Re-connect if it was previously connected or active
            if (wasConnected || wasActive) {
                logger.i("DelegatingGateway", "Re-connecting new implementation (triggered by previous state)")
                scope.launch {
                    currentImplementation?.connect()
                    lastPresence?.let {
                        logger.i("DelegatingGateway", "Resending last presence to new implementation")
                        currentImplementation?.sendActivity(it)
                    }
                }
            }
        }
    }

    override suspend fun connect() {
        logger.i("DelegatingGateway", "connect() called")
        if (isWebSocketConnected()) {
            logger.i("DelegatingGateway", "Already connected. Skipping connection attempt.")
            return
        }
        updateImplementation(recreateIfNull = true)
        currentImplementation?.connect()
    }

    override suspend fun sendActivity(presence: Presence) {
        logger.i("DelegatingGateway", "sendActivity() called")
        lastPresence = presence
        updateImplementation(recreateIfNull = true)
        try {
            currentImplementation?.sendActivity(presence)
        } catch (e: Exception) {
            logger.e("DelegatingGateway", "Failed to send activity: ${e.message}")
        }
    }

    override fun isWebSocketConnected(): Boolean {
        updateImplementation(recreateIfNull = false)
        val connected = currentImplementation?.isWebSocketConnected() ?: false
        logger.d("DelegatingGateway", "isWebSocketConnected(): $connected")
        return connected
    }

    override fun refreshSession() {
        logger.i("DelegatingGateway", "refreshSession() called")
        updateImplementation(recreateIfNull = false)
        currentImplementation?.refreshSession()
    }

    override fun close() {
        logger.i("DelegatingGateway", "close() called. Nulling implementation.")
        sessionActiveSyncJob?.cancel()
        sessionActiveSyncJob = null
        if (currentImplementation != null) {
            logger.i("DelegatingGateway", "Closing current implementation...")
            currentImplementation?.close()
        } else {
            logger.w("DelegatingGateway", "close() called but currentImplementation is already null")
        }
        currentImplementation = null
        _sessionActive.value = false
        lastPresence = null
    }
}