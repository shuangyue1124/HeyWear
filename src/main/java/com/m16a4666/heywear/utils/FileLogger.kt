package com.m16a4666.heywear.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun networkEndpointForLog(url: String): String = url.substringBefore('?')
internal const val MAX_LOG_ENTRY_CHARS = 4_000
internal const val MAX_LOG_FILE_BYTES = 128 * 1024L

internal fun boundedLogContent(content: String): String = content.take(MAX_LOG_ENTRY_CHARS)

internal fun shouldRotateLogFile(currentBytes: Long, newEntryBytes: Int): Boolean {
    return newEntryBytes > MAX_LOG_FILE_BYTES ||
        currentBytes > MAX_LOG_FILE_BYTES - newEntryBytes
}

object FileLogger {
    private const val FILE_NAME = "heywear_debug.log"
    private const val TAG = "FileLogger"

    //写日志
    fun write(context: Context?, tag: String, content: String) {
        //DebugLogger开了才写
        if (!DebugLogger.IS_DEBUG) return

        if (context == null) return

        try {
            //路径/sdcard/Android/data/com.m16a4666.heywear/files/heywear_debug.log
            val logDir = context.getExternalFilesDir(null) ?: return
            if (!logDir.exists() && !logDir.mkdirs()) {
                return
            }

            val logFile = File(logDir, FILE_NAME)
            val time = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val safeContent = boundedLogContent(content)

            //格式化日志条目
            val logEntry = "\n[$time] [$tag]\n$safeContent\n--------------------------------\n"
            val logBytes = logEntry.toByteArray(Charsets.UTF_8)
            Log.d(tag, safeContent)

            //写入文件部分
            if (shouldRotateLogFile(logFile.length(), logBytes.size)) {
                logFile.writeBytes(logBytes)
            } else {
                logFile.appendBytes(logBytes)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Failed to write log: ${e.message}")
        }
    }

    //网络请求
    fun logNetwork(context: Context?, url: String, code: Int) {
        if (!DebugLogger.IS_DEBUG) return
        val endpoint = networkEndpointForLog(url)
        write(context, "Network", "URL: $endpoint\nCode: $code")
    }

    //获取日志文件的绝对路径
    fun getLogPath(context: Context): String {
        return context.getExternalFilesDir(null)?.absolutePath + "/" + FILE_NAME
    }

    //清空日志文件
    fun clear(context: Context) {
        try {
            val logDir = context.getExternalFilesDir(null) ?: return
            val file = File(logDir, FILE_NAME)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
