package com.m16a4666.heywear.interact

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import androidx.wear.compose.material.*
import coil.compose.AsyncImage
import com.m16a4666.heywear.model.HeyPost
import com.m16a4666.heywear.model.UserProfile
import com.m16a4666.heywear.utils.CookieUtil
import com.m16a4666.heywear.utils.FileLogger
import com.m16a4666.heywear.utils.HeyboxSigner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun PersonalCenterScreen(
    context: android.content.Context,
    onLogout: () -> Unit,
    onRefreshSettings: () -> Unit = {},
    onPostClick: (HeyPost) -> Unit
) {
    var userProfile by remember { mutableStateOf(UserProfile()) }
    var isLoading by remember { mutableStateOf(true) }

    // 覆盖层状态：null, "settings", "donate", "about", "my_content", "fav", "history"
    var overlayState by remember { mutableStateOf<String?>(null) }

    // 联网加载
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                if (!CookieUtil.isLoggedIn(context)) {
                    userProfile = UserProfile(nickname = "游客", avatar = null)
                    isLoading = false
                    return@withContext
                }

                val userId = try { CookieUtil.getUserId(context) } catch (e:Exception) { "" }
                if (userId.isEmpty()) throw Exception("No ID")

                val path = "/bbs/app/profile/user/profile"
                val cookie = CookieUtil.getCookie(context)
                val time = HeyboxSigner.getTime()
                val nonce = HeyboxSigner.getNonce(time)
                val hkey = HeyboxSigner.getHkey(path, time, nonce)
                val baseParams = "os_type=web&app=heybox&client_type=web&version=999.0.4&web_version=2.5&x_client_type=web&x_app=heybox_website&heybox_id=$userId&x_os_type=Windows&device_info=Edge&device_id=83858260b1e14cbd686069b4a5c0b8b3"
                val url = "https://api.xiaoheihe.cn$path?$baseParams&userid=$userId&hkey=$hkey&_time=$time&nonce=$nonce"

                val conn = URL(url).openConnection() as HttpURLConnection
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36 Edg/143.0.0.0")
                conn.setRequestProperty("Referer", "https://www.xiaoheihe.cn/")
                conn.setRequestProperty("Cookie", cookie)

                val jsonStr = conn.inputStream.bufferedReader().readText()
                FileLogger.logNetwork(context, url, conn.responseCode, emptyMap(), jsonStr)

                val root = JSONObject(jsonStr)
                if (root.optString("status") == "failed") throw Exception("Failed")

                val result = root.getJSONObject("result")
                val detail = result.getJSONObject("account_detail")
                val bbsInfo = detail.getJSONObject("bbs_info")
                var avatarUrl = detail.optString("avatar", "")
                if (avatarUrl.startsWith("http://")) avatarUrl = avatarUrl.replace("http://", "https://")

                val level = detail.optJSONObject("level_info")?.optInt("level", 0) ?: 0

                userProfile = UserProfile(
                    nickname = detail.optString("username", "用户"),
                    avatar = avatarUrl,
                    level = level,
                    followCount = bbsInfo.optInt("follow_num", 0),
                    fanCount = bbsInfo.optInt("fan_num", 0),
                    likeCount = bbsInfo.optInt("be_favoured_num", 0)
                )
                isLoading = false
            } catch (e: Exception) {
                e.printStackTrace()
                FileLogger.write(context, "ProfileErr", e.toString())
                isLoading = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 个人中心主页
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize().background(Color.Black),
                anchorType = ScalingLazyListAnchorType.ItemStart
            ) {
                // 头像
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AsyncImage(model = userProfile.avatar, contentDescription = "Avatar", modifier = Modifier.size(60.dp).clip(CircleShape))
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = userProfile.nickname, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(Modifier.width(6.dp))
                            if (CookieUtil.isLoggedIn(context)) {
                                Text(
                                    text = "Lv.${userProfile.level}",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.background(Color(0xFFFFD700), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            } else {
                                Text("游客", color = Color.Gray, fontSize = 10.sp)
                            }
                        }
                    }
                }
                // 2. 数据
                item {
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 4.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        DataItem("关注", userProfile.followCount.toString())
                        DataItem("粉丝", userProfile.fanCount.toString())
                        DataItem("获赞", userProfile.likeCount.toString())
                    }
                }

                // 收藏/历史
                item {
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 15.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        CompactChip(
                            onClick = { overlayState = "fav" }, // 跳转收藏
                            label = { Text("收藏", fontSize = 11.sp, color = Color.LightGray) },
                            colors = ChipDefaults.secondaryChipColors(),
                            modifier = Modifier.width(70.dp)
                        )
                        CompactChip(
                            onClick = { overlayState = "history" }, // 跳转历史
                            label = { Text("历史", fontSize = 11.sp, color = Color.LightGray) },
                            colors = ChipDefaults.secondaryChipColors(),
                            modifier = Modifier.width(70.dp)
                        )
                    }
                }

                // 我的内容
                item {
                    Text("我的内容", color = Color(0xFF00C0FF), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                }
                item {
                    CompactChip(
                        onClick = { overlayState = "my_content" }, // 跳转动态
                        label = { Text("动态 / 投稿", fontSize = 12.sp) },
                        colors = ChipDefaults.primaryChipColors(backgroundColor = Color(0xFF222222)),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                    )
                }

                item { Spacer(Modifier.height(15.dp)) }

                // 设置入口
                item {
                    Chip(
                        onClick = { overlayState = "settings" },
                        label = { Text("设置", color = Color.White) },
                        secondaryLabel = { Text("通用 / 账号 / 关于", color = Color.Gray) },
                        colors = ChipDefaults.secondaryChipColors(),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
                    )
                }

                // 5. 退出软件按钮
                item {
                    Chip(
                        onClick = { (context as? Activity)?.finish() },
                        label = { Text("退出软件", color = Color(0xFFFF4D4F)) },
                        colors = ChipDefaults.secondaryChipColors(),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
                    )
                }

                item { Spacer(Modifier.height(20.dp)) }
            }
        }

        // 顶层覆盖
        if (overlayState != null) {
            key(overlayState) {
                val swipeState = rememberSwipeToDismissBoxState()
                LaunchedEffect(swipeState.currentValue) {
                    if (swipeState.currentValue == SwipeToDismissValue.Dismissed) {
                        if (overlayState == "donate" || overlayState == "about") {
                            overlayState = "settings"
                            swipeState.snapTo(SwipeToDismissValue.Default)
                        } else {
                            overlayState = null
                        }
                    }
                }

                SwipeToDismissBox(
                    state = swipeState,
                    hasBackground = false
                ) { isBackground ->
                    if (!isBackground) {
                        when (overlayState) {
                            "settings" -> SettingsScreen(
                                onBack = { overlayState = null },
                                onLogout = onLogout,
                                onOpenDonate = { overlayState = "donate" },
                                onOpenAbout = { overlayState = "about" },
                                onSettingChanged = onRefreshSettings
                            )
                            "donate" -> DonateScreen { overlayState = "settings" }
                            "about" -> AboutScreen { overlayState = "settings" }

                            "my_content" -> UserContentScreen(
                                type = "my_post",
                                onBack = { overlayState = null },
                                onItemClick = onPostClick
                            )
                            "fav" -> UserContentScreen(
                                type = "fav",
                                onBack = { overlayState = null },
                                onItemClick = onPostClick
                            )
                            "history" -> UserContentScreen(
                                type = "history",
                                onBack = { overlayState = null },
                                onItemClick = onPostClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DataItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(label, color = Color.Gray, fontSize = 10.sp)
    }
}