package com.m16a4666.heywear.utils

import androidx.compose.runtime.mutableStateListOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DebugLogger {
    //true就是开日志了
    const val IS_DEBUG = true

    val logs = mutableStateListOf<String>()

    fun log(tag: String, msg: String) {
        if (!IS_DEBUG) return
        val time = SimpleDateFormat("mm:ss", Locale.getDefault()).format(Date())
        logs.add(0, "[$time] $tag: $msg")
        // 保留50条
        if (logs.size > 50) logs.removeLast()
    }

    fun clear() {
        logs.clear()
    }
}