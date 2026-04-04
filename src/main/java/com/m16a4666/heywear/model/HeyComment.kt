package com.m16a4666.heywear.model

//黑盒评论结构
data class HeyComment(
    val id: String,
    val userId: String,
    val userName: String,
    val avatar: String,
    val content: String,
    val time: String,
    val location: String,
    val likeCount: Int,
    val isOwner: Boolean,
    val childNum: Int = 0,
    val children: List<HeyComment> = emptyList(),
    val imgUrl: String? = null
)