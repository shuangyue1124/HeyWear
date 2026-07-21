package com.m16a4666.heywear.utils

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class HeyboxHttpClientTest {
    @Test
    fun getAppliesResourceLimitsAndReturnsRequiredData() {
        val connection = FakeConnection(
            code = 200,
            body = "{\"status\":\"ok\"}",
            headers = mapOf("set-cookie" to listOf("user_pkey=value; Path=/"))
        )

        val response = HeyboxHttpClient.get(
            url = "https://api.xiaoheihe.cn/test?hkey=secret",
            userAgent = "test-agent",
            cookie = "user_cookie=value",
            connectionFactory = { connection }
        )

        assertEquals(200, response.code)
        assertEquals("{\"status\":\"ok\"}", response.body)
        assertEquals(listOf("user_pkey=value; Path=/"), response.setCookies)
        assertEquals(HeyboxHttpClient.CONNECT_TIMEOUT_MS, connection.connectTimeout)
        assertEquals(HeyboxHttpClient.READ_TIMEOUT_MS, connection.readTimeout)
        assertFalse(connection.useCaches)
        assertFalse(connection.instanceFollowRedirects)
        assertEquals("test-agent", connection.getRequestProperty("User-Agent"))
        assertEquals("https://www.xiaoheihe.cn/", connection.getRequestProperty("Referer"))
        assertEquals("user_cookie=value", connection.getRequestProperty("Cookie"))
        assertTrue(connection.disconnected)
    }

    @Test
    fun getRejectsHttpErrorsWithoutReturningTheirBody() {
        val connection = FakeConnection(code = 503, body = "private response")

        val error = assertThrows(HeyboxHttpException::class.java) {
            HeyboxHttpClient.get(
                url = "https://api.xiaoheihe.cn/test",
                userAgent = "test-agent",
                connectionFactory = { connection }
            )
        }

        assertEquals(503, error.statusCode)
        assertEquals("网络请求失败（HTTP 503）", error.message)
        assertTrue(connection.disconnected)
    }

    @Test
    fun getRejectsOversizedResponseBeforeReadingIt() {
        val connection = FakeConnection(
            code = 200,
            body = "not read",
            declaredLength = HeyboxHttpClient.MAX_RESPONSE_BYTES + 1L
        )

        val error = assertThrows(java.io.IOException::class.java) {
            HeyboxHttpClient.get(
                url = "https://api.xiaoheihe.cn/test",
                userAgent = "test-agent",
                connectionFactory = { connection }
            )
        }

        assertEquals("接口响应过大", error.message)
        assertFalse(connection.inputStreamOpened)
        assertTrue(connection.disconnected)
    }

    private class FakeConnection(
        private val code: Int,
        body: String,
        private val headers: Map<String, List<String>> = emptyMap(),
        private val declaredLength: Long? = null
    ) : HttpURLConnection(URL("https://api.xiaoheihe.cn/test")) {
        private val bodyBytes = body.toByteArray(Charsets.UTF_8)
        var disconnected = false
        var inputStreamOpened = false

        override fun connect() = Unit

        override fun disconnect() {
            disconnected = true
        }

        override fun usingProxy(): Boolean = false

        override fun getResponseCode(): Int = code

        override fun getContentLengthLong(): Long = declaredLength ?: bodyBytes.size.toLong()

        override fun getInputStream(): ByteArrayInputStream {
            inputStreamOpened = true
            return ByteArrayInputStream(bodyBytes)
        }

        override fun getErrorStream() = ByteArrayInputStream(bodyBytes)

        override fun getHeaderFields(): Map<String, List<String>> = headers

        override fun getOutputStream() = ByteArrayOutputStream()
    }
}
