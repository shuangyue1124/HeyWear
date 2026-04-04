package com.m16a4666.heywear.model

data class HeyPost(
    val linkId: String,      // 帖子ID
    val title: String,       // 标题
    val author: String,      // 作者
    val avatar: String?,     // 头像
    val description: String, // 正文
    val images: List<String>,// 图片列表
    val isArticle: Boolean   // 是否为长文章
)