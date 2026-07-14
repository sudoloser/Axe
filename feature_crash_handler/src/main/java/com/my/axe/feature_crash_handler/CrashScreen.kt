package com.my.axe.feature_crash_handler

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.my.axe.data.utils.shareAsFile
import com.my.axe.resources.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.exitProcess
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.Instant

data class CrashReportData(
    val stackTrace: String,
    val manufacturer: String,
    val device: String,
    val androidVersion: String,
    val appVersionName: String,
    val appVersionCode: Long,
) {
    val appVersion: String get() = "$appVersionName ($appVersionCode)"
    val deviceInfo: String get() = "$manufacturer $device"

    val trace: String get() = buildString {
        appendLine("axe crash report")
        appendLine("Manufacturer: $manufacturer")
        appendLine("Device: $device")
        appendLine("Android version: $androidVersion")
        appendLine("App version: $appVersion")
        appendLine("Stacktrace: ")
        append(stackTrace)
    }
}

private const val PASTEBIN_API_KEY = "sFw8YmFtGJ8R1j0tAo4-Ci7n9UuzSVWQ"
private const val DISCORD_WEBHOOK_URL = "https://discord.com/api/webhooks/1526517239011082240/1E_IRWy3IA9jKn6Hpt3gg4ePyVNxEulF9MIF47j9sVtgtw2F6cGgpSfpiIJM-k9co1zZ"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrashScreen(report: CrashReportData) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isSending by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.app_crashed),
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold)
                    )
                },
                actions = {
                    IconButton(onClick = {
                        exitProcess(0)
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = Icons.Default.Close.name
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExtendedFloatingActionButton(
                    onClick = {
                        scope.launch {
                            isSending = true
                            uploadCrashLogs(report)
                                .onSuccess { pastebinUrl ->
                                    snackbarHostState.showSnackbar("Logs sent! $pastebinUrl")
                                }
                                .onFailure { e ->
                                    snackbarHostState.showSnackbar("Failed: ${e.message}")
                                }
                            isSending = false
                        }
                    },
                    enabled = !isSending
                ) {
                    Icon(
                        imageVector = if (isSending) Icons.Filled.Close else Icons.Default.Send,
                        contentDescription = "Send",
                        modifier = Modifier.padding(end = 5.dp)
                    )
                    Text(text = "Send logs")
                }
                ExtendedFloatingActionButton(onClick = {
                    ctx.shareAsFile(report.trace, "axe_Log.txt")
                }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Logs",
                        modifier = Modifier.padding(end = 5.dp)
                    )
                    Text(text = stringResource(id = R.string.share_crash_logs))
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.share_crash_logs_desc),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 5.dp)
                )
                ElevatedCard(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    )
                ) {
                    LazyColumn {
                        item {
                            Text(
                                modifier = Modifier.padding(10.dp),
                                text = report.trace,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }
    }
}

private suspend fun uploadCrashLogs(report: CrashReportData): Result<String> = withContext(Dispatchers.IO) {
    runCatching {
        val pastebinUrl = uploadToPastebin(report.trace)
        val embed = buildCrashEmbed(pastebinUrl, report)
        sendDiscordWebhook(embed)
        pastebinUrl
    }
}

private fun uploadToPastebin(content: String): String {
    val params = buildString {
        append("api_dev_key="); append(URLEncoder.encode(PASTEBIN_API_KEY, "UTF-8"))
        append("&api_option="); append(URLEncoder.encode("paste", "UTF-8"))
        append("&api_paste_code="); append(URLEncoder.encode(content, "UTF-8"))
        append("&api_paste_private="); append(URLEncoder.encode("1", "UTF-8"))
        append("&api_paste_expire_date="); append(URLEncoder.encode("1D", "UTF-8"))
        append("&api_paste_format="); append(URLEncoder.encode("text", "UTF-8"))
    }
    val conn = URL("https://pastebin.com/api/api_post.php").openConnection() as HttpURLConnection
    conn.requestMethod = "POST"
    conn.doOutput = true
    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
    OutputStreamWriter(conn.outputStream).use { it.write(params) }
    val response = BufferedReader(InputStreamReader(conn.inputStream)).readText()
    if (!response.startsWith("https://pastebin.com/")) throw Exception(response)
    return response
}

private fun sendDiscordWebhook(payload: String) {
    val conn = URL(DISCORD_WEBHOOK_URL).openConnection() as HttpURLConnection
    conn.requestMethod = "POST"
    conn.doOutput = true
    conn.setRequestProperty("Content-Type", "application/json")
    OutputStreamWriter(conn.outputStream).use { it.write(payload) }
    val code = conn.responseCode
    if (code !in 200..299) {
        val error = conn.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
        throw Exception("Discord webhook returned $code: $error")
    }
}

private fun buildCrashEmbed(pastebinUrl: String, report: CrashReportData): String {
    return buildString {
        appendLine("{")
        appendLine("  \"embeds\": [")
        appendLine("    {")
        appendLine("      \"title\": \"💥 Crash Report\",")
        appendLine("      \"color\": 15158332,")
        appendLine("      \"fields\": [")
        appendLine("        {\"name\": \"📱 Device\", \"value\": \"${jsonEscape(report.deviceInfo)}\", \"inline\": true},")
        appendLine("        {\"name\": \"🤖 Android\", \"value\": \"${jsonEscape(report.androidVersion)}\", \"inline\": true},")
        appendLine("        {\"name\": \"📦 App Version\", \"value\": \"${jsonEscape(report.appVersion)}\", \"inline\": true},")
        appendLine("        {\"name\": \"📋 Crash Logs\", \"value\": \"[View on Pastebin]($pastebinUrl)\", \"inline\": false}")
        appendLine("      ],")
        appendLine("      \"timestamp\": \"${Instant.now()}\"")
        appendLine("    }")
        appendLine("  ]")
        appendLine("}")
    }
}

private fun jsonEscape(value: String): String = buildString {
    for (c in value) {
        when (c) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            '\b' -> append("\\b")
            '\u000C' -> append("\\f")
            else -> append(c)
        }
    }
}

@Preview
@Composable
fun PreviewCrashScreen() = CrashScreen(report = CrashReportData(
    stackTrace = """
    java.lang.SecurityException: Missing permission to control media.
    	at android.os.Parcel.createExceptionOrNull(Parcel.java:3011)
    	at android.os.Parcel.createException(Parcel.java:2995)
    	at android.os.Parcel.readException(Parcel.java:2978)
    	at android.os.Parcel.readException(Parcel.java:2920)
    	at android.media.session.ISessionManager$Stub$Proxy.getSessions(ISessionManager.java:672)
    	at android.media.session.MediaSessionManager.getActiveSessionsForUser(MediaSessionManager.java:272)
    	at android.media.session.MediaSessionManager.getActiveSessions(MediaSessionManager.java:194)
    	at com.my.axe.data.get_current_data.media.GetCurrentPlayingMedia.invoke(GetCurrentlyPlayingMedia.kt:38)
    	at com.my.axe.services.MediaRpcService$onCreate$1.invokeSuspend(MediaRpcService.kt:61)
    	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33)
    	at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:106)
    	at kotlinx.coroutines.internal.LimitedDispatcher.run(LimitedDispatcher.kt:42)
    	at kotlinx.coroutines.scheduling.TaskImpl.run(Tasks.kt:95)
    	at kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:570)
    	at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.executeTask(CoroutineScheduler.kt:750)
    	at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.runWorker(CoroutineScheduler.kt:677)
    	at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:664)
    	Suppressed: kotlinx.coroutines.DiagnosticCoroutineContextException: [StandaloneCoroutine{Cancelling}@2fd5814, Dispatchers.IO]
    Caused by: android.os.RemoteException: Remote stack trace:
    	at com.android.server.media.MediaSessionService.enforceMediaPermissions(MediaSessionService.java:616)
    	at com.android.server.media.MediaSessionService.-$$Nest$menforceMediaPermissions(Unknown Source:0)
    	at com.android.server.media.MediaSessionService$SessionManagerImpl.verifySessionsRequest(MediaSessionService.java:2163)
    	at com.android.server.media.MediaSessionService$SessionManagerImpl.getSessions(MediaSessionService.java:1269)
    	at android.media.session.ISessionManager$Stub.onTransact(ISessionManager.java:317)
    """.trimIndent(),
    manufacturer = "******",
    device = "*****",
    androidVersion = "**",
    appVersionName = "***",
    appVersionCode = 0,
))
