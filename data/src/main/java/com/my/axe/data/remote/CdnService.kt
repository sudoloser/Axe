package com.my.axe.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import okhttp3.Protocol
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CdnService @Inject constructor() {
    private val baseUrl = "https://cdn.qzz.io"
    private val client = HttpClient(OkHttp) {
        engine {
            config {
                connectTimeout(30, TimeUnit.SECONDS)
                readTimeout(30, TimeUnit.SECONDS)
                writeTimeout(30, TimeUnit.SECONDS)
                protocols(listOf(Protocol.HTTP_1_1))
            }
        }
    }

    suspend fun uploadPublic(filename: String, data: ByteArray): Result<String> = runCatching {
        client.post {
            url("$baseUrl/api/v1/upload/public")
            headers {
                append("X-Filename", filename)
                append(HttpHeaders.ContentType, "application/octet-stream")
                append(HttpHeaders.UserAgent, "Axe/1.2.2")
            }
            setBody(data)
        }.bodyAsText()
    }
}
