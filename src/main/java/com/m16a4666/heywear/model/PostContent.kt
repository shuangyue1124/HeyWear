package com.m16a4666.heywear.model

// 定义文章内容的
sealed class ContentNode {
    // 文字块 (Html格式)
    data class TextNode(val html: String) : ContentNode()
    // 图片块
    data class ImageNode(val url: String) : ContentNode()
    // 头部信息
    data class HeaderNode(
        val title: String,
        val author: String,
        val avatar: String?,
        val time: String
    ) : ContentNode()
}