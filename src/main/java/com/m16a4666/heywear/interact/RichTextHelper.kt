package com.m16a4666.heywear.interact

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.m16a4666.heywear.utils.EmojiUtil
import java.util.regex.Pattern

object RichTextHelper {

    // 正则匹配：匹配中括号内包含 中文、英文、数字、下划线、感叹号 的内容
    // 例如: [cube_滑稽], [heygirl_rua!], [2024]
    private val EMOJI_REGEX = Pattern.compile("\\[([a-zA-Z0-9\\u4e00-\\u9fa5_!]+)\\]")

    // 把普通文本转换为带表情占位符的富文本
    fun buildSpannedString(text: String): AnnotatedString {
        return buildAnnotatedString {
            val matcher = EMOJI_REGEX.matcher(text)
            var lastIndex = 0

            while (matcher.find()) {
                // 添加表情前的普通文字
                append(text.substring(lastIndex, matcher.start()))

                // 获取括号里的Key(cube_滑稽一类)
                val emojiKey = matcher.group(1) ?: ""

                // 检查EmojiUtil里是否有这个表情的图片链接
                // 注意下EmojiUtil.map的Key必须和这里提取的一致
                if (EmojiUtil.map.containsKey(emojiKey)) {
                    // 插入图片占位符 (id 就是 map 的 key)
                    appendInlineContent(id = emojiKey, alternateText = "[$emojiKey]")
                } else {
                    // 如果本地没收录这个表情，就显示原文本
                    append(matcher.group())
                }
                lastIndex = matcher.end()
            }
            //添加剩下的文字
            if (lastIndex < text.length) {
                append(text.substring(lastIndex))
            }
        }
    }

    // 生 Compose需要的图片内容映射
    fun getInlineContentMap(): Map<String, InlineTextContent> {
        val inlineMap = mutableMapOf<String, InlineTextContent>()

        EmojiUtil.map.forEach { (key, url) ->
            inlineMap[key] = InlineTextContent(
                Placeholder(
                    width = 20.sp,
                    height = 20.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                )
            ) {
                // Coil加载图片
                AsyncImage(
                    model = url,
                    contentDescription = key,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        return inlineMap
    }
}