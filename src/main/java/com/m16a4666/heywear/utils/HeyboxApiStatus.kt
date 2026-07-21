package com.m16a4666.heywear.utils

internal sealed interface HeyboxApiStatus {
    data object Ok : HeyboxApiStatus

    data class Rejected(
        val status: String,
        val message: String
    ) : HeyboxApiStatus
}

internal fun evaluateHeyboxApiStatus(status: String, message: String): HeyboxApiStatus {
    if (status == "ok") {
        return HeyboxApiStatus.Ok
    }

    val normalizedStatus = status.ifBlank { "unknown" }
    val userMessage = when (normalizedStatus) {
        "show_captcha" -> "详情接口触发安全验证"
        else -> message.ifBlank { "接口返回异常状态：$normalizedStatus" }
    }
    return HeyboxApiStatus.Rejected(normalizedStatus, userMessage)
}
