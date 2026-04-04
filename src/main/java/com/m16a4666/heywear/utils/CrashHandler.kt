package com.m16a4666.heywear.utils

import android.content.Context
import java.io.PrintWriter
import java.io.StringWriter

object CrashHandler : Thread.UncaughtExceptionHandler {

    private var context: Context? = null
    private var defaultHandler: Thread.UncaughtExceptionHandler? = null

    fun init(ctx: Context) {
        context = ctx.applicationContext
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        // 获取堆栈信息
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        e.printStackTrace(pw)
        val stackTrace = sw.toString()

        // 写入文件日志
        try {
            FileLogger.write(context, "CRASH_FATAL", stackTrace)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        // 3. 保存到SharedPreferences
        try {
            val prefs = context?.getSharedPreferences("crash_log", Context.MODE_PRIVATE)
            prefs?.edit()?.putString("last_crash", stackTrace)?.apply()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }


        defaultHandler?.uncaughtException(t, e)
    }

    // 读取并清除日志
    fun getAndClearCrashLog(context: Context): String? {
        val prefs = context.getSharedPreferences("crash_log", Context.MODE_PRIVATE)
        val log = prefs.getString("last_crash", null)
        if (log != null) {
        }
        return log
    }

    // 或者手动清除
    fun clearLog(context: Context) {
        val prefs = context.getSharedPreferences("crash_log", Context.MODE_PRIVATE)
        prefs.edit().remove("last_crash").apply()
    }
}