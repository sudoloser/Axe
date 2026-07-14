package com.my.axe.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PastebinService @Inject constructor(
    private val client: HttpClient,
) {
    private val apiKey = "sFw8YmFtGJ8R1j0tAo4-Ci7n9UuzSVWQ"

    suspend fun upload(content: String, title: String? = null): Result<String> = runCatching {
        val body = buildString {
            append("api_dev_key="); append(URLEncoder.encode(apiKey, "UTF-8"))
            append("&api_option="); append(URLEncoder.encode("paste", "UTF-8"))
            append("&api_paste_code="); append(URLEncoder.encode(content, "UTF-8"))
            append("&api_paste_private="); append(URLEncoder.encode("1", "UTF-8"))
            append("&api_paste_expire_date="); append(URLEncoder.encode("1D", "UTF-8"))
            append("&api_paste_format="); append(URLEncoder.encode("text", "UTF-8"))
            if (title != null) {
                append("&api_paste_name="); append(URLEncoder.encode(title, "UTF-8"))
            }
        }
        val response = client.post("https://pastebin.com/api/api_post.php") {
            setBody(body)
            contentType(ContentType.Application.FormUrlEncoded)
        }
        val responseBody = response.bodyAsText()
        if (responseBody.startsWith("https://pastebin.com/")) {
            responseBody
        } else {
            throw Exception("Pastebin upload failed: $responseBody")
        }
    }
}
