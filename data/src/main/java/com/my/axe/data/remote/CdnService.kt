package com.my.axe.data.remote

import com.my.axe.data.rpc.Constants
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import okhttp3.Protocol
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CdnService @Inject constructor() {
    private val baseUrl = "https://api.imgur.com/3"
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
            url("$baseUrl/image")
            headers {
                append(HttpHeaders.Authorization, "Client-ID ${Constants.IMGUR_CLIENT_ID}")
                append(HttpHeaders.UserAgent, "Axe/1.2.2")
            }
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("image", data, Headers.build {
                            append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                        })
                        append("type", "file")
                    }
                )
            )
        }.bodyAsText()
    }
}
