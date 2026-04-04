package com.m16a4666.heywear.model

data class UserProfile(
    val nickname: String = "加载中...",
    val avatar: String? = null,
    val level: Int = 0,
    val followCount: Int = 0, // 关注
    val fanCount: Int = 0,    // 粉丝
    val likeCount: Int = 0,   // 获赞
    val favCount: Int = 0,    // 收藏
    val historyCount: Int = 0 // 历史
)