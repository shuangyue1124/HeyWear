package com.m16a4666.heywear.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class HeyboxSignerTest {
    @Test
    fun capturedLinkTreeSignatureRemainsStable() {
        val actual = HeyboxSigner.getHkey(
            path = "/bbs/app/link/tree",
            time = "1777445804",
            nonce = "AE5EDADC4E88EF44979008EACFB73FA0"
        )

        assertEquals("IVVU711", actual)
    }
}
