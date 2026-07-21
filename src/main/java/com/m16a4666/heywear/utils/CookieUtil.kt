package com.m16a4666.heywear.utils

import android.content.Context

object CookieUtil {
    private const val PREF_NAME = "heywear_user"
    private const val KEY_COOKIE = "user_cookie"

    fun parseAndClean(setCookies: List<String>): String {
        val sb = StringBuilder()
        for (value in setCookies) {
            val cleanCookie = value.substringBefore(';')
            if (cleanCookie.isNotBlank()) sb.append(cleanCookie).append("; ")
        }
        return sb.toString().trim()
    }

    fun saveCookie(context: Context, cookie: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_COOKIE, cookie).apply()
        FileLogger.write(context, "Cookie", "Saved")
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
