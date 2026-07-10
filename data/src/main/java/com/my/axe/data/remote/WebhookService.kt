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
    private val webhookUrl = "https://discord.com/api/webhooks/1524993937142710294/iAqo3VCnq6NCVBnNGcPJ9fYx8j6RIkqVU4D7N7sZQ8uNXciUjKWie12ZOTkjSQpB9kMq"

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
