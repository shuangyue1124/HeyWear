package com.m16a4666.heywear.utils

import android.content.Context

object CookieUtil {
    private const val PREF_NAME = "heywear_user"
    private const val KEY_COOKIE = "user_cookie"

    fun parseAndClean(headers: Map<String, List<String>>): String {
        val sb = StringBuilder()
        for ((key, values) in headers) {
            if (key != null && key.equals("Set-Cookie", ignoreCase = true)) {
                for (value in values) {
                    val cleanCookie = value.split(";")[0]
                    sb.append(cleanCookie).append("; ")
                }
            }
        }
        return sb.toString().trim()
    }

    fun saveCookie(context: Context, cookie: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_COOKIE, cookie).apply()
        // 记Cookie的
        FileLogger.write(context, "Cookie", "Saved: ${cookie.take(10)}******")
    }

    fun getCookie(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_COOKIE, "") ?: ""
    }

    fun isLoggedIn(context: Context): Boolean {
        return getCookie(context).isNotEmpty()
    }

    fun getUserId(context: Context): String {
        val cookie = getCookie(context)
        if (cookie.isEmpty()) return ""
        val regex = "user_heybox_id=(\\d+)".toRegex()
        val match = regex.find(cookie)
        return match?.groupValues?.get(1) ?: ""
    }

    fun clear(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_COOKIE).apply()
        FileLogger.write(context, "Cookie", "Cleared")
    }
}