package axe.gateway

import axe.gateway.entities.presence.Presence
import com.my.axe.domain.interfaces.Logger
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
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

    private val serverUrl = if (serverBaseUrl.endsWith("/")) "${serverBaseUrl}api/" else "$serverBaseUrl/api/"
    private val stopUrl = if (serverBaseUrl.endsWith("/")) "${serverBaseUrl}api/stop" else "$serverBaseUrl/api/stop"
    private val sessionId = UUID.randomUUID().toString()
    private val client = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)
        .build()
    
    private var webSocket: WebSocket? = null
    private var heartbeatJob: Job? = null
    private var retryDelay = 1000L
    private val maxRetryDelay = 64000L
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override val coroutineContext: CoroutineContext = SupervisorJob() + Dispatchers.IO
    private val scope = CoroutineScope(coroutineContext)

    override suspend fun connect() {
        if (webSocket != null) return
        
        val request = Request.Builder()
            .url(serverUrl)
            .build()
        
        webSocket = client.newWebSocket(request, this)
        logger.i("RemoteGateway", "Connecting to $serverUrl")
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        logger.i("RemoteGateway", "WebSocket Opened")
        retryDelay = 1000L // Reset retry delay
        
        val authMessage = AuthMessage(
            type = "AUTH",
            app_signature = appSignature,
            user_id = userId,
            token = token,
            session_id = sessionId,
            timestamp = System.currentTimeMillis()
        )
        
        webSocket.send(json.encodeToString(authMessage))
        logger.i("RemoteGateway", "Auth sent")
        
        startHeartbeat()
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        logger.d("RemoteGateway", "Message received: $text")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        logger.w("RemoteGateway", "Closing: $code / $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        logger.e("RemoteGateway", "Failure: ${t.message}")
        this.webSocket = null
        stopHeartbeat()
        
        // Reconnect with exponential backoff
        scope.launch {
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
                val heartbeat = HeartbeatMessage(
                    type = "HEARTBEAT",
                    session_id = sessionId
                )
                webSocket?.send(json.encodeToString(heartbeat))
                logger.i("RemoteGateway", "Heartbeat sent")
            }
        }
    }

    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    override suspend fun sendActivity(presence: Presence) {
        val message = PresenceUpdateMessage(
            type = "PRESENCE_UPDATE",
            session_id = sessionId,
            presence = presence
        )
        webSocket?.send(json.encodeToString(message))
        logger.i("RemoteGateway", "Activity update sent")
    }

    override fun isWebSocketConnected(): Boolean {
        return webSocket != null
    }

    override fun close() {
        logger.i("RemoteGateway", "Closing connection")
        stopHeartbeat()
        webSocket?.close(1000, "App closed")
        webSocket = null
        
        // Call stop endpoint
        scope.launch {
            try {
                val stopRequest = StopRequest(
                    session_id = sessionId,
                    app_signature = appSignature
                )
                val body = json.encodeToString(stopRequest).toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(stopUrl)
                    .post(body)
                    .build()
                
                client.newCall(request).execute().use { response ->
                    logger.i("RemoteGateway", "Stop request response: ${response.code}")
                }
            } catch (e: Exception) {
                logger.e("RemoteGateway", "Failed to send stop request: ${e.message}")
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
        val session_id: String
    )

    @Serializable
    private data class PresenceUpdateMessage(
        val type: String,
        val session_id: String,
        val presence: Presence
    )

    @Serializable
    private data class StopRequest(
        val session_id: String,
        val app_signature: String
    )
}
