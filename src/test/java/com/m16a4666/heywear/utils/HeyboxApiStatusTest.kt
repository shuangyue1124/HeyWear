package com.m16a4666.heywear.utils

import org.junit.Assert.assertTrue
import org.junit.Assert.assertSame
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
}
