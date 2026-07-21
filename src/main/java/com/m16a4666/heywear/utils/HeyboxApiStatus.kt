package com.m16a4666.heywear.utils

import java.io.IOException

internal sealed interface HeyboxApiStatus {
    data object Ok : HeyboxApiStatus

    data class Rejected(
        val status: String,
        val message: String
    ) : HeyboxApiStatus
}

internal class HeyboxApiException(
    val status: String,
    message: String
) : IOException(message)

internal fun evaluateHeyboxApiStatus(status: String, message: String): HeyboxApiStatus {
    if (status == "ok") {
        return HeyboxApiStatus.Ok
    }

    val normalizedStatus = status.ifBlank { "unknown" }
    val userMessage = when (normalizedStatus) {
        "show_captcha" -> "接口触发安全验证"
        else -> message.ifBlank { "接口返回异常状态：$normalizedStatus" }
    }
    return HeyboxApiStatus.Rejected(normalizedStatus, userMessage)
}

internal fun requireHeyboxApiOk(status: String, message: String) {
    when (val result = evaluateHeyboxApiStatus(status, message)) {
        HeyboxApiStatus.Ok -> Unit
        is HeyboxApiStatus.Rejected -> throw HeyboxApiException(
            status = result.status,
            message = result.message
        )
    }
}
