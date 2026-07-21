package com.m16a4666.heywear.utils

import androidx.compose.runtime.mutableStateListOf
import com.m16a4666.heywear.BuildConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DebugLogger {
    val IS_DEBUG: Boolean = BuildConfig.DEBUG

    val logs by lazy(LazyThreadSafetyMode.NONE) { mutableStateListOf<String>() }

    fun log(tag: String, msg: String) {
        if (!IS_DEBUG) return
        val time = SimpleDateFormat("mm:ss", Locale.getDefault()).format(Date())
        logs.add(0, "[$time] $tag: $msg")
        // 保留50条
        if (logs.size > 50) logs.removeAt(logs.lastIndex)
    }

    fun clear() {
        logs.clear()
    }
}
