package com.my.axe.feature_home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.my.axe.data.remote.Author
import com.my.axe.data.remote.CdnService
import com.my.axe.data.remote.CdnUploadResponse
import com.my.axe.data.remote.Embed
import com.my.axe.data.remote.Field
import com.my.axe.data.remote.Footer
import com.my.axe.data.remote.Image
import com.my.axe.data.remote.WebhookPayload
import com.my.axe.data.remote.WebhookService
import com.my.axe.domain.interfaces.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import javax.inject.Inject

data class BugReportState(
    val isSubmitting: Boolean = false,
    val uploadProgress: Float = 0f,
    val uploadStatus: String = "",
    val resultMessage: String? = null,
    val isSuccess: Boolean = false,
)

@HiltViewModel
class BugReportViewModel @Inject constructor(
    private val cdnService: CdnService,
    private val webhookService: WebhookService,
    private val logger: Logger,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(BugReportState())
    val state: StateFlow<BugReportState> = _state.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true }

    fun submitBugReport(
        title: String,
        description: String,
        imageUris: List<Uri>,
        deviceModel: String,
        androidVersion: String,
        appVersion: String,
    ) {
        viewModelScope.launch {
            _state.value = BugReportState(
                isSubmitting = true,
                uploadStatus = "Uploading images...",
            )

            val imageUrls = mutableListOf<String>()

            for ((index, uri) in imageUris.withIndex()) {
                _state.value = _state.value.copy(
                    uploadStatus = "Uploading image ${index + 1}/${imageUris.size}...",
                )

                val inputStream = withContext(Dispatchers.IO) {
                    try { context.contentResolver.openInputStream(uri) } catch (e: Exception) { null }
                }

                if (inputStream == null) {
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        resultMessage = "Failed to read image $index",
                    )
                    return@launch
                }

                val bytes = withContext(Dispatchers.IO) {
                    inputStream.use { it.readBytes() }
                }

                val fileName = "bug_${System.currentTimeMillis()}_$index.jpg"
                val response = withContext(Dispatchers.IO) {
                    cdnService.uploadPublic(fileName, bytes)
                }

                response.fold(
                    onSuccess = { body ->
                        val parsed = json.decodeFromString<CdnUploadResponse>(body)
                        val link = parsed.data?.link
                        if (parsed.success && link != null) {
                            imageUrls.add(link)
                        } else {
                            _state.value = _state.value.copy(
                                isSubmitting = false,
                                resultMessage = "Upload failed for image $index",
                            )
                            return@launch
                        }
                    },
                    onFailure = { e ->
                        _state.value = _state.value.copy(
                            isSubmitting = false,
                            resultMessage = "Upload error: ${e.message}",
                        )
                        return@launch
                    },
                )

                _state.value = _state.value.copy(
                    uploadProgress = (index + 1).toFloat() / imageUris.size.coerceAtLeast(1),
                )
            }

            _state.value = _state.value.copy(uploadStatus = "Submitting report...")

            // A stable URL shared across all embeds so Discord groups them
            // into an inline image gallery on the same message card.
            val galleryAnchor = imageUrls.firstOrNull() ?: "https://github.com/sudoloser/axe"

            val rawLogs = logger.getFormattedLogs(maxLines = 30)
            val logFieldValue = if (rawLogs.isNotBlank()) {
                "```\n$rawLogs\n```".take(1024)
            } else {
                "No logs available"
            }

            val fields = buildList {
                add(Field("📱 Device", deviceModel, inline = true))
                add(Field("🤖 Android", androidVersion, inline = true))
                if (appVersion.isNotBlank()) add(Field("📦 App Version", appVersion, inline = true))
                if (imageUrls.isEmpty()) add(Field("🖼 Screenshots", "None attached", inline = false))
                add(Field("📋 Latest Logs", logFieldValue, inline = false))
            }

            val timestamp = Instant.now().toString()

            // Primary embed — contains all the report details + first image
            val primaryEmbed = Embed(
                author = Author(
                    name = "🐛  Bug Report",
                    iconUrl = "https://cdn.discordapp.com/emojis/1135593021519458336.webp",
                ),
                title = title.ifBlank { "Untitled Report" },
                description = buildString {
                    appendLine(description.ifBlank { "*No description provided.*" })
                    if (imageUrls.size > 1) {
                        appendLine()
                        appendLine("*${imageUrls.size} screenshots attached ↓*")
                    }
                },
                color = 0xEB459E, // Discord pink — stands out in the feed
                url = galleryAnchor,
                fields = fields,
                image = imageUrls.firstOrNull()?.let { Image(it) },
                footer = Footer(
                    text = "Axe Bug Report  •  Submitted via in-app reporter",
                    iconUrl = "https://raw.githubusercontent.com/sudoloser/axe/main/axe.png",
                ),
                timestamp = timestamp,
            )

            // Extra image embeds — share `url = galleryAnchor` so Discord
            // clusters them visually with the primary embed as a gallery.
            val extraImageEmbeds = imageUrls.drop(1).map { imgUrl ->
                Embed(
                    url = galleryAnchor,
                    color = 0xEB459E,
                    image = Image(imgUrl),
                )
            }

            val payload = WebhookPayload(embeds = listOf(primaryEmbed) + extraImageEmbeds)
            val payloadJson = json.encodeToString(payload)

            val webhookResult = withContext(Dispatchers.IO) {
                webhookService.postRaw(payloadJson)
            }

            webhookResult.fold(
                onSuccess = {
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        uploadProgress = 1f,
                        uploadStatus = "",
                        resultMessage = "Bug report submitted successfully! Thank you.",
                        isSuccess = true,
                    )
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isSubmitting = false,
                        uploadProgress = 1f,
                        uploadStatus = "",
                        resultMessage = "Failed to submit: ${e.message}",
                        isSuccess = false,
                    )
                },
            )
        }
    }

    fun resetState() {
        _state.value = BugReportState()
    }
}
