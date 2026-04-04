package com.m16a4666.heywear.utils

import android.content.Context
import java.util.UUID
import kotlin.random.Random

object DeviceUtil {

    // 随机UA
    private val uaList = listOf(
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 Edg/122.0.0.0",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:123.0) Gecko/20100101 Firefox/123.0"
    )

    // 获取随机UA
    fun getRandomUA(): String {
        return uaList.random()
    }

    // 获取设备ID
    fun getDeviceId(context: Context): String {
        val prefs = context.getSharedPreferences("device_settings", Context.MODE_PRIVATE)
        var id = prefs.getString("device_id", "")

        if (id.isNullOrEmpty()) {
            // 生成一个新的随机ID
            id = UUID.randomUUID().toString().replace("-", "")
            prefs.edit().putString("device_id", id).apply()
        }
        return id!!
    }
}