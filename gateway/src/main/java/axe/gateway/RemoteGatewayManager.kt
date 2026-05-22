package axe.gateway

import axe.gateway.entities.presence.Presence
import com.my.axe.domain.interfaces.Logger
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class RemoteGatewayManager(
    private val token: String,
    private val userId: String,
    private val appSignature: String,
    private val serverBaseUrl: String,
    private val logger: Logger
) : DiscordWebSocket, WebSocketListener() {

    private val wsUrl: String
    private val statusUrl: String
    private val stopUrl: String
    
    init {
        val base = serverBaseUrl.trimEnd('/')
        // Convert https:// to wss:// for websocket
        wsUrl = base.replace("http://", "ws://").replace("https://", "wss://") + "/api/"
        // Status and Stop endpoints
        statusUrl = base.replace("ws://", "http://").replace("wss://", "https://") + "/api/session/"
        stopUrl = base.replace("ws://", "http://").replace("wss://", "https://") + "/api/stop"
    }
    
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        // More robust scrubbing for body/headers if they ever leak tokens
        val scrubbed = message.replace(Regex("\"token\":\"[^\"]+\""), "\"token\":\"***\"")
                              .replace(Regex("x-app-signature: [^\\s]+"), "x-app-signature: ***")
        logger.d("RemoteGatewayNet", scrubbed)
    }.apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .pingInterval(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private var webSocket: WebSocket? = null
    private var heartbeatJob: Job? = null
    private var retryDelay = 2000L
    private val maxRetryDelay = 60000L
    
    private var isAuthorized = false
    private var isDeliberatelyClosed = false
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO
    private val scope = CoroutineScope(coroutineContext)

    override suspend fun connect() {
        if (token.isBlank()) {
            val err = "Cannot connect to Remote Gateway: Discord Token is empty!"
            logger.e("RemoteGateway", err)
            throw IllegalStateException(err)
        }
        
        isDeliberatelyClosed = false

        if (webSocket != null) {
            logger.w("RemoteGateway", "Connect called but already connecting/connected")
            return
        }
        
        isAuthorized = false
        logger.i("RemoteGateway", "Initiating Connection...")
        logger.d("RemoteGateway", "WS URL: $wsUrl")
        
        val request = Request.Builder()
            .url(wsUrl)
            .build()
        
        webSocket = client.newWebSocket(request, this)
    }

    /**
     * Refresh existing session on server (pings server via HTTP)
     */
    override fun refreshSession() {
        scope.launch {
            try {
                logger.i("RemoteGateway", "Refreshing session for user $userId on server...")
                val request = Request.Builder()
                    .url(statusUrl + userId)
                    .addHeader("x-app-signature", appSignature)
                    .get()
                    .build()
                
                client.newCall(request).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (it.isSuccessful) {
                                logger.i("RemoteGateway", "Session refresh successful (HTTP ${it.code})")
                            } else {
                                logger.w("RemoteGateway", "Session refresh failed or not found (HTTP ${it.code})")
                            }
                        }
                    }
                    override fun onFailure(call: Call, e: java.io.IOException) {
                        logger.e("RemoteGateway", "Failed to reach server for refresh: ${e.message}")
                    }
                })
            } catch (e: Exception) {
                logger.e("RemoteGateway", "Error during session refresh: ${e.message}")
            }
        }
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        logger.i("RemoteGateway", "Network connection established (HTTP ${response.code}). Sending AUTH...")
        retryDelay = 2000L 
        
        val authMessage = AuthMessage(
            type = "AUTH",
            app_signature = appSignature,
            user_id = userId.ifEmpty { "000000000000000000" },
            token = token,
            session_id = userId,
            timestamp = System.currentTimeMillis()
        )
        
        try {
            val jsonAuth = json.encodeToString(authMessage)
            webSocket.send(jsonAuth)
            logger.i("RemoteGateway", "AUTH payload sent to server")
            isAuthorized = true 
            startHeartbeat()
        } catch (e: Exception) {
            logger.e("RemoteGateway", "Failed to send AUTH: ${e.message}")
        }
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        logger.d("RemoteGateway", "Incoming: $text")
        try {
            val msg = json.decodeFromString<ServerMessage>(text)
            if (msg.type == "ERROR") {
                logger.e("RemoteGateway", "SERVER ERROR: ${msg.message}")
                if (msg.message?.contains("signature", ignoreCase = true) == true) {
                    logger.e("RemoteGateway", "Invalid App Signature! Check settings.")
                    isAuthorized = false
                    retryDelay = maxRetryDelay 
                }
            }
        } catch (e: Exception) {
            // Ignore non-standard messages
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        logger.w("RemoteGateway", "Server closing connection: $code / $reason")
        isAuthorized = false
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        logger.e("RemoteGateway", "WebSocket Failure: ${t.message ?: "Unknown error"}")
        this.webSocket = null
        isAuthorized = false
        stopHeartbeat()
        
        // Reconnect with exponential backoff if not closed deliberately
        scope.launch {
            if (isDeliberatelyClosed) {
                logger.i("RemoteGateway", "Deliberately closed, skipping retry.")
                return@launch
            }
            logger.i("RemoteGateway", "Retrying connection in ${retryDelay/1000}s...")
            delay(retryDelay)
            retryDelay = (retryDelay * 2).coerceAtMost(maxRetryDelay)
            connect()
        }
    }

    private fun startHeartbeat() {
        stopHeartbeat()
        heartbeatJob = scope.launch {
            while (isActive) {
                delay(TimeUnit.HOURS.toMillis(6))
                if (isAuthorized && webSocket != null) {
                    val heartbeat = HeartbeatMessage(
                        type = "HEARTBEAT",
                        user_id = userId,
                        session_id = userId
                    )
                    try {
                        webSocket?.send(json.encodeToString(heartbeat))
                        logger.i("RemoteGateway", "Relay heartbeat sent")
                    } catch (e: Exception) {
                        logger.e("RemoteGateway", "Failed to send heartbeat: ${e.message}")
                    }
                }
            }
        }
    }

    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    override suspend fun sendActivity(presence: Presence) {
        logger.d("RemoteGateway", "Presence update requested. Checking authorization...")
        
        // Wait for authorization (max 10 seconds)
        var count = 0
        while (!isAuthorized && count < 100) {
            if (webSocket == null) {
                throw IllegalStateException("Remote Gateway disconnected during authorization wait")
            }
            delay(100)
            count++
        }

        if (!isAuthorized || webSocket == null) {
            logger.e("RemoteGateway", "Gave up waiting for auth (timeout 10s). Activity not sent.")
            throw IllegalStateException("Remote Gateway authorization timeout")
        }

        val message = PresenceUpdateMessage(
            type = "PRESENCE_UPDATE",
            user_id = userId,
            session_id = userId,
            presence = presence
        )
        
        try {
            val jsonMsg = json.encodeToString(message)
            webSocket?.send(jsonMsg)
            logger.i("RemoteGateway", "Presence update sent to relay: ${presence.activities?.firstOrNull()?.name}")
        } catch (e: Exception) {
            logger.e("RemoteGateway", "Failed to send presence over WebSocket: ${e.message}")
            throw e
        }
    }

    override fun isWebSocketConnected(): Boolean {
        return webSocket != null && isAuthorized
    }

    override fun close() {
        logger.i("RemoteGateway", "close() called from stack trace: ${Thread.currentThread().stackTrace.getOrNull(3)}")
        logger.i("RemoteGateway", "Close called. Cleaning up...")
        isDeliberatelyClosed = true
        isAuthorized = false
        stopHeartbeat()
        webSocket?.close(1000, "App closed")
        webSocket = null
        
        // Call stop endpoint via HTTP POST (async)
        scope.launch {
            try {
                val stopRequest = StopRequest(
                    user_id = userId,
                    session_id = userId,
                    app_signature = appSignature
                )
                val body = json.encodeToString(stopRequest).toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(stopUrl)
                    .post(body)
                    .build()
                
                client.newCall(request).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            logger.i("RemoteGateway", "Purge request response: ${it.code}")
                        }
                    }
                    override fun onFailure(call: Call, e: java.io.IOException) {
                        logger.e("RemoteGateway", "Purge request failed: ${e.message}")
                    }
                })
            } catch (e: Exception) {
                logger.e("RemoteGateway", "Failed to setup stop request: ${e.message}")
            }
        }
    }

    @Serializable
    private data class AuthMessage(
        val type: String,
        val app_signature: String,
        val user_id: String,
        val token: String,
        val session_id: String,
        val timestamp: Long
    )

    @Serializable
    private data class HeartbeatMessage(
        val type: String,
        val user_id: String,
        val session_id: String
    )

    @Serializable
    private data class PresenceUpdateMessage(
        val type: String,
        val user_id: String,
        val session_id: String,
        val presence: Presence
    )

    @Serializable
    private data class StopRequest(
        val user_id: String,
        val session_id: String,
        val app_signature: String
    )
    
    @Serializable
    private data class ServerMessage(
        val type: String? = null,
        val message: String? = null
    )
}
