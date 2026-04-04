package com.m16a4666.heywear.utils

import android.content.Context

object SettingsUtil {
    private const val PREF_NAME = "heywear_settings"
    private const val KEY_NO_IMAGE = "no_image_mode"
    private const val KEY_SHOW_TIME = "show_time_mode"
    private const val KEY_DISABLE_SWIPE = "disable_swipe_back" //禁用滑动返回

    // 无图模式
    fun setNoImageMode(context: Context, isEnabled: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_NO_IMAGE, isEnabled).apply()
    }

    fun isNoImageMode(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_NO_IMAGE, false)
    }

    // 时间显示
    fun setShowTime(context: Context, isEnabled: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SHOW_TIME, isEnabled).apply()
    }

    fun isShowTime(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SHOW_TIME, false)
    }

    //禁用滑动返回设置
    fun setDisableSwipe(context: Context, isEnabled: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DISABLE_SWIPE, isEnabled).apply()
    }

    fun isSwipeDisabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        //默认不禁用
        return prefs.getBoolean(KEY_DISABLE_SWIPE, false)
    }
}