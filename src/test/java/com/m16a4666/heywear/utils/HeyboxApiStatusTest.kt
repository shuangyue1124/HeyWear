package com.m16a4666.heywear.utils

import org.junit.Assert.assertTrue
import org.junit.Assert.assertSame
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class HeyboxApiStatusTest {
    @Test
    fun captchaResponseIsRejectedBeforeReadingResultLink() {
        val actual = evaluateHeyboxApiStatus(
            status = "show_captcha",
            message = ""
        )

        assertTrue(actual is HeyboxApiStatus.Rejected)
    }

    @Test
    fun okResponseRemainsAccepted() {
        val actual = evaluateHeyboxApiStatus(
            status = "ok",
            message = ""
        )

        assertSame(HeyboxApiStatus.Ok, actual)
    }

    @Test
    fun captchaResponseThrowsLocalizedApiException() {
        val error = assertThrows(HeyboxApiException::class.java) {
            requireHeyboxApiOk(status = "show_captcha", message = "")
        }

        assertEquals("show_captcha", error.status)
        assertEquals("接口触发安全验证", error.message)
    }
}
