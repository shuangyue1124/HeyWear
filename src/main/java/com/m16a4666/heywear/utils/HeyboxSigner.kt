package com.m16a4666.heywear.utils

import java.security.MessageDigest
import kotlin.random.Random

object HeyboxSigner {
    private const val DICT = "AB45STUVWZEFGJ6CH01D237IXYPQRKLMN89"

    fun getTime(): String = (System.currentTimeMillis() / 1000).toString()

    fun getNonce(time: String): String {
        val randomSeed = Random.nextDouble().toString()
        return getMD5(time + randomSeed).uppercase()
    }

    fun getHkey(path: String, time: String, nonce: String): String {
        return generateHkey(path, time.toLong() + 1, nonce)
    }

    private fun generateHkey(url: String, timestamp: Long, nonce: String): String {
        val pathParts = url
            .substringBefore('?')
            .split('/')
            .filter(String::isNotEmpty)
        val normalizedPath = "/${pathParts.joinToString("/")}/"
        val parts = listOf(
            mapCharacters(timestamp.toString(), DICT.dropLast(2)),
            mapCharacters(normalizedPath, DICT),
            mapCharacters(nonce, DICT)
        )

        val interleaved = buildString {
            for (index in 0 until parts.maxOf(String::length)) {
                for (part in parts) {
                    if (index < part.length) append(part[index])
                }
            }
        }.take(20)

        val digest = getMD5(interleaved)
        val checksumValues = digest.takeLast(6).map { it.code }.toMutableList()
        val original = checksumValues.take(4)
        checksumValues[0] = mul14(original[0]) xor mul12(original[1]) xor
            mul6(original[2]) xor mul3(original[3])
        checksumValues[1] = mul3(original[0]) xor mul14(original[1]) xor
            mul12(original[2]) xor mul6(original[3])
        checksumValues[2] = mul6(original[0]) xor mul3(original[1]) xor
            mul14(original[2]) xor mul12(original[3])
        checksumValues[3] = mul12(original[0]) xor mul6(original[1]) xor
            mul3(original[2]) xor mul14(original[3])

        val prefix = mapCharacters(digest.take(5), DICT.dropLast(4))
        val checksum = Math.floorMod(checksumValues.sum(), 100)
        return prefix + checksum.toString().padStart(2, '0')
    }

    private fun mapCharacters(value: String, dictionary: String): String = buildString {
        for (character in value) {
            append(dictionary[character.code % dictionary.length])
        }
    }

    private fun xtime(value: Int): Int {
        return if (value and 0x80 != 0) {
            ((value shl 1) xor 0x1b) and 0xff
        } else {
            value shl 1
        }
    }

    private fun mul3(value: Int): Int = xtime(value) xor value

    private fun mul6(value: Int): Int = mul3(xtime(value))

    private fun mul12(value: Int): Int = mul6(mul3(xtime(value)))

    private fun mul14(value: Int): Int = mul12(value) xor mul6(value) xor mul3(value)

    fun getMD5(input: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
