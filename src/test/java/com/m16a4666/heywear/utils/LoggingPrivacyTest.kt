package com.m16a4666.heywear.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoggingPrivacyTest {
    @Test
    fun networkLogDropsQueryCredentials() {
        val endpoint = networkEndpointForLog(
            "https://api.xiaoheihe.cn/bbs/app/feeds?hkey=secret&nonce=private"
        )

        assertEquals("https://api.xiaoheihe.cn/bbs/app/feeds", endpoint)
        assertFalse(endpoint.contains("secret"))
        assertFalse(endpoint.contains("private"))
    }

    @Test
    fun cookieParserKeepsOnlyCookiePairs() {
        val cookie = CookieUtil.parseAndClean(
            listOf(
                "user_pkey=value; Path=/; HttpOnly",
                "user_heybox_id=123; Path=/"
            )
        )

        assertEquals("user_pkey=value; user_heybox_id=123;", cookie)
    }

    @Test
    fun debugFilePolicyBoundsEntriesAndRotatesAtLimit() {
        val bounded = boundedLogContent("x".repeat(MAX_LOG_ENTRY_CHARS + 10))

        assertEquals(MAX_LOG_ENTRY_CHARS, bounded.length)
        assertFalse(shouldRotateLogFile(MAX_LOG_FILE_BYTES - 10, 10))
        assertTrue(shouldRotateLogFile(MAX_LOG_FILE_BYTES - 10, 11))
    }
}
