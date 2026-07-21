package com.m16a4666.heywear.utils

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

internal data class HeyboxHttpResponse(
    val code: Int,
    val body: String,
    val setCookies: List<String>
)

internal class HeyboxHttpException(
    val statusCode: Int
) : IOException("网络请求失败（HTTP $statusCode）")

internal object HeyboxHttpClient {
    internal const val CONNECT_TIMEOUT_MS = 10_000
    internal const val READ_TIMEOUT_MS = 15_000
    internal const val MAX_RESPONSE_BYTES = 2 * 1024 * 1024
    private const val REFERER = "https://www.xiaoheihe.cn/"

    fun get(
        url: String,
        userAgent: String,
        cookie: String = "",
        connectionFactory: (URL) -> HttpURLConnection = {
            it.openConnection() as HttpURLConnection
        }
    ): HeyboxHttpResponse {
        val connection = connectionFactory(URL(url))
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = CONNECT_TIMEOUT_MS
            connection.readTimeout = READ_TIMEOUT_MS
            connection.instanceFollowRedirects = false
            connection.useCaches = false
            connection.setRequestProperty("User-Agent", userAgent)
            connection.setRequestProperty("Referer", REFERER)
            if (cookie.isNotEmpty()) {
                connection.setRequestProperty("Cookie", cookie)
            }

            val statusCode = connection.responseCode
            if (statusCode !in 200..299) {
                connection.errorStream?.close()
                throw HeyboxHttpException(statusCode)
            }

            val body = readBody(connection)
            val setCookies = connection.headerFields.entries
                .firstOrNull { it.key?.equals("Set-Cookie", ignoreCase = true) == true }
                ?.value
                ?.toList()
                .orEmpty()
            HeyboxHttpResponse(statusCode, body, setCookies)
        } finally {
            connection.disconnect()
        }
    }

    private fun readBody(connection: HttpURLConnection): String {
        val declaredLength = connection.contentLengthLong
        if (declaredLength > MAX_RESPONSE_BYTES) {
            throw IOException("接口响应过大")
        }

        connection.inputStream.use { input ->
            val output = ByteArrayOutputStream(
                declaredLength.takeIf { it in 1..MAX_RESPONSE_BYTES }
                    ?.toInt()
                    ?: DEFAULT_BUFFER_SIZE
            )
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var totalBytes = 0
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                totalBytes += count
                if (totalBytes > MAX_RESPONSE_BYTES) {
                    throw IOException("接口响应过大")
                }
                output.write(buffer, 0, count)
            }
            return output.toString(Charsets.UTF_8.name())
        }
    }
}
