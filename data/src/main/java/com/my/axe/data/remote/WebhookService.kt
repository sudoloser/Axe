package com.my.axe.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import javax.inject.Inject

class WebhookService @Inject constructor(
    private val client: HttpClient,
) {
    private val webhookUrl = "https://discord.com/api/webhooks/1525098118062801038/4nhxNjGMLy33nEzsPjOWXXZVMfA1muPMlCBWgmwj0w3w-VEjqGR7zvvEXlM8S771FYDu"

    suspend fun postRaw(jsonBody: String): Result<HttpResponse> = runCatching {
        client.post {
            url(webhookUrl)
            headers {
                append(HttpHeaders.ContentType, "application/json")
            }
            setBody(jsonBody)
        }
    }
}
