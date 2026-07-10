package com.my.axe.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import javax.inject.Inject

class CdnService @Inject constructor(
    private val client: HttpClient,
) {
    private val baseUrl = "https://cdn.qzz.io"

    suspend fun uploadPublic(filename: String, data: ByteArray): Result<HttpResponse> = runCatching {
        client.post {
            url("$baseUrl/api/v1/upload/public")
            headers {
                append(HttpHeaders.ContentType, ContentType.Application.OctetStream.toString())
                append("X-Filename", filename)
            }
            setBody(data)
        }
    }
}
