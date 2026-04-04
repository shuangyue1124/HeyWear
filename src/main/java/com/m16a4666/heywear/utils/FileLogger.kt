package com.m16a4666.heywear.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
            val logDir = context.getExternalFilesDir(null)
            if (logDir != null && !logDir.exists()) {
                logDir.mkdirs()
            }

            val logFile = File(logDir, FILE_NAME)
            val time = SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            //格式化日志条目
            val logEntry = "\n[$time] [$tag]\n$content\n--------------------------------\n"
            Log.d(tag, content)

            //写入文件部分
            val writer = FileWriter(logFile, true)
            writer.append(logEntry)
            writer.flush()
            writer.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Failed to write log: ${e.message}")
        }
    }

    //网络请求
    fun logNetwork(context: Context?, url: String, code: Int, headers: Map<String, List<String>>, body: String) {
        if (!DebugLogger.IS_DEBUG) return

        val sb = StringBuilder()
        sb.append("URL: $url\n")
        sb.append("Code: $code\n")

        //查找Cookie头
        val cookies = headers["Set-Cookie"] ?: headers["set-cookie"]

        if (cookies != null) {
            sb.append("Set-Cookie Found: ${cookies.size} items\n")
            cookies.forEach { cookieString ->
                //只记录前15位不然号被盗就老实了
                val masked = if (cookieString.length > 15) {
                    cookieString.take(15) + "******"
                } else {
                    "******"
                }
                sb.append(" - $masked\n")
            }
        } else {
            sb.append("Set-Cookie: NULL\n")
        }

        //只记录前1000个字符
        val truncatedBody = if (body.length > 1000) body.take(1000) + "...(truncated)" else body
        sb.append("Body: $truncatedBody")

        write(context, "Network", sb.toString())
    }

    //获取日志文件的绝对路径
    fun getLogPath(context: Context): String {
        return context.getExternalFilesDir(null)?.absolutePath + "/" + FILE_NAME
    }

    //清空日志文件
    fun clear(context: Context) {
        try {
            val file = File(context.getExternalFilesDir(null), FILE_NAME)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}