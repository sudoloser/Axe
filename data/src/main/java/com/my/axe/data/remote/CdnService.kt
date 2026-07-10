package com.my.axe.data.remote

import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

class CdnService @Inject constructor() {
    private val baseUrl = "https://cdn.qzz.io"

    suspend fun uploadPublic(filename: String, data: ByteArray): Result<String> = runCatching {
        val url = URL("$baseUrl/api/v1/upload/public")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("X-Filename", filename)
        connection.setRequestProperty("Content-Type", "application/octet-stream")
        connection.doOutput = true
        connection.connectTimeout = 30_000
        connection.readTimeout = 30_000
        connection.setFixedLengthStreamingMode(data.size)
        try {
            val outputStream: OutputStream = connection.outputStream
            outputStream.write(data)
            outputStream.flush()
            outputStream.close()
            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().readText()
            } else {
                val error = connection.errorStream?.bufferedReader()?.readText() ?: "HTTP $responseCode"
                throw RuntimeException(error)
            }
        } finally {
            connection.disconnect()
        }
    }
}
