package com.m16a4666.heywear.utils

import java.security.MessageDigest
import kotlin.random.Random

/**
 * 核心签名算法
 * 为了避免被找上门这里把算法删了，自己去抓包吧，我只能给思路
 */
object HeyboxSigner {

    // 字典表
    private const val DICT = "114514"

    // 获取当前时间戳

    fun getTime(): String = (System.currentTimeMillis() / 1000).toString()

    // 获取随机Nonce
    fun getNonce(time: String): String {
        // 这里的逻辑是MD5(时间戳+随机盐/随机数)（我写的时候是这样）
        val randomSeed = Random.nextDouble().toString()
        return getMD5(time + randomSeed).uppercase()
    }

    /**
     * 获取核心签名Hkey
     * 1：path 请求路径，如 "/bbs/app/feeds"
     * 2：time 当前时间戳
     * 3：nonce 上一步生成的 nonce
     */
    fun getHkey(path: String, time: String, nonce: String): String {
        // 然后这个坑我讲讲，正经运算里你得把时间戳+1
        return generateHkey(path, time.toLong() + 1, nonce)
    }

    //混淆逻辑
    private fun generateHkey(url: String, timestamp: Long, nonce: String): String {
        // 1: 对URL路径进行标准化处理
        // 2: 使用字典表DICT对timestamp,url,nonce进行特定规则的字符映射替换
        // 3: 将替换后的三组字符串进行交叉合并
        // 4: 计算合并字符串前20位的MD5
        // 5: 取MD5的后6位，然后进行位运算 (这里自己抓包) 生成Checksum
        // 6: 取Checksum并与MD5前5位的映射结果进行最终拼接
        
        // 这里返回的是假签名
        // 请在此处实现你的真实签名逻辑
        return "FAKE_HKEY"
    }

    // 计算MD5 你用的上的
    fun getMD5(input: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}