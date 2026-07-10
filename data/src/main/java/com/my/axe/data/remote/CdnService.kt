package com.my.axe.data.remote

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CdnService @Inject constructor() {
    private val baseUrl = "https://cdn.qzz.io"
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun uploadPublic(filename: String, data: ByteArray): Result<String> = runCatching {
        val body = data.toRequestBody("application/octet-stream".toMediaType())
        val request = Request.Builder()
            .url("$baseUrl/api/v1/upload/public")
            .header("X-Filename", filename)
            .post(body)
            .build()
        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""
        if (!response.isSuccessful) throw RuntimeException("HTTP ${response.code}: $responseBody")
        responseBody
    }
}
