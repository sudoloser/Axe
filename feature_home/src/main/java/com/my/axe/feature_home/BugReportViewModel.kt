package com.my.axe.feature_home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.my.axe.data.remote.CdnService
import com.my.axe.data.remote.CdnUploadResponse
import com.my.axe.data.remote.Embed
import com.my.axe.data.remote.Field
import com.my.axe.data.remote.Image
import com.my.axe.data.remote.WebhookPayload
import com.my.axe.data.remote.WebhookService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
                    onSuccess = { httpResponse ->
                        val body = withContext(Dispatchers.IO) { httpResponse.bodyAsText() }
                        val parsed = json.decodeFromString<CdnUploadResponse>(body)
                        if (parsed.success && parsed.url != null) {
                            imageUrls.add("https://cdn.qzz.io${parsed.url}")
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

            val fields = buildList {
                add(Field("Device", deviceModel, true))
                add(Field("Android", androidVersion, true))
                if (appVersion.isNotBlank()) add(Field("App Version", appVersion, true))
            }

            val embed = Embed(
                title = title,
                description = description.ifBlank { "No description provided" },
                color = 0x5865F2,
                fields = fields,
                image = imageUrls.firstOrNull()?.let { Image(it) },
            )

            val payload = WebhookPayload(embeds = listOf(embed))
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
