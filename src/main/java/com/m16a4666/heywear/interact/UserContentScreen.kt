package com.m16a4666.heywear.interact

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.*
import com.m16a4666.heywear.model.HeyPost
import com.m16a4666.heywear.utils.CookieUtil
import com.m16a4666.heywear.utils.DeviceUtil
import com.m16a4666.heywear.utils.HeyboxHttpClient
import com.m16a4666.heywear.utils.HeyboxSigner
import com.m16a4666.heywear.utils.requireHeyboxApiOk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

@Composable
fun UserContentScreen(
    type: String,
    onBack: () -> Unit,
    onItemClick: (HeyPost) -> Unit
) {
    val context = LocalContext.current
    val listState = rememberScalingLazyListState()

    val postList = remember { mutableStateListOf<HeyPost>() }
    var statusText by remember { mutableStateOf("加载中...") }
    var isLoading by remember { mutableStateOf(false) }
    var isEnd by remember { mutableStateOf(false) }
    var offset by remember { mutableIntStateOf(0) }

    val titleStr = when(type) {
        "history" -> "浏览历史"
        "fav" -> "我的收藏"
        else -> "我的内容"
    }

    val scope = rememberCoroutineScope()

    fun loadData() {
        if (isLoading || isEnd) return
        isLoading = true

        scope.launch(Dispatchers.IO) {
            try {
                val userId = CookieUtil.getUserId(context)
                val cookie = CookieUtil.getCookie(context)
                val deviceId = DeviceUtil.getDeviceId(context)
                val ua = DeviceUtil.getRandomUA()

                var finalUrl = ""

                // 构建URL
                if (type == "fav") {
                    // 收藏夹 （没写好）
                    // Step A: 获取列表
                    val folderPath = "/bbs/app/profile/fav/folders"
                    val t1 = HeyboxSigner.getTime()
                    val n1 = HeyboxSigner.getNonce(t1)
                    val k1 = HeyboxSigner.getHkey(folderPath, t1, n1)
                    val p1 = "os_type=web&app=heybox&client_type=web&version=999.0.4&web_version=2.5&x_client_type=web&x_app=heybox_website&x_os_type=Windows&heybox_id=$userId&userid=$userId&enable_new_style_collect=1"
                    val url1 = "https://api.xiaoheihe.cn$folderPath?$p1&hkey=$k1&_time=$t1&nonce=$n1"

                    val folderResponse = HeyboxHttpClient.get(url1, ua, cookie)
                    val root1 = JSONObject(folderResponse.body)
                    requireHeyboxApiOk(root1.optString("status"), root1.optString("msg"))

                    val folders = root1.optJSONObject("result")?.optJSONArray("folders")
                    if (folders == null || folders.length() == 0) throw Exception("没有找到收藏夹")
                    val folderId = folders.getJSONObject(0).optString("id")

                    // 获取内容
                    val listPath = "/bbs/app/profile/fav/folder/v2/links"
                    val t2 = HeyboxSigner.getTime()
                    val n2 = HeyboxSigner.getNonce(t2)
                    val k2 = HeyboxSigner.getHkey(listPath, t2, n2)
                    val p2 = "os_type=web&app=heybox&client_type=web&version=999.0.4&web_version=2.5&x_client_type=web&x_app=heybox_website&heybox_id=$userId&userid=$userId&x_os_type=Windows&folder_id=$folderId&offset=$offset&limit=20&enable_new_style_collect=1&dw=604&no_more=false&device_info=Edge&device_id=$deviceId"
                    finalUrl = "https://api.xiaoheihe.cn$listPath?$p2&hkey=$k2&_time=$t2&nonce=$n2"

                } else if (type == "history") {
                    // 历史记录
                    val path = "/bbs/app/profile/history/visit"
                    val t = HeyboxSigner.getTime()
                    val n = HeyboxSigner.getNonce(t)
                    val k = HeyboxSigner.getHkey(path, t, n)
                    val p = "os_type=web&app=heybox&client_type=web&version=999.0.4&web_version=2.5&x_client_type=web&x_app=heybox_website&heybox_id=$userId&userid=$userId&x_os_type=Windows&offset=$offset&limit=20&type=all&dw=636&no_more=false&device_info=Edge&device_id=$deviceId"
                    finalUrl = "https://api.xiaoheihe.cn$path?$p&hkey=$k&_time=$t&nonce=$n"

                } else {
                    // 我的动态
                    val path = "/bbs/app/profile/user/link/list"
                    val t = HeyboxSigner.getTime()
                    val n = HeyboxSigner.getNonce(t)
                    val k = HeyboxSigner.getHkey(path, t, n)
                    val p = "os_type=web&app=heybox&client_type=web&version=999.0.4&web_version=2.5&x_client_type=web&x_app=heybox_website&heybox_id=$userId&userid=$userId&x_os_type=Windows&offset=$offset&limit=20&device_info=Edge&device_id=$deviceId"
                    finalUrl = "https://api.xiaoheihe.cn$path?$p&hkey=$k&_time=$t&nonce=$n"
                }

                // 发起请求
                val response = HeyboxHttpClient.get(finalUrl, ua, cookie)
                val root = JSONObject(response.body)
                requireHeyboxApiOk(root.optString("status"), root.optString("msg"))

                // 解析列表
                val newPosts = mutableListOf<HeyPost>()

                // 历史记录部分(数据在result.history_visit, 内容在content字段)
                if (type == "history") {
                    val historyArray = root.optJSONObject("result")?.optJSONArray("history_visit")
                    if (historyArray != null && historyArray.length() > 0) {
                        for (i in 0 until historyArray.length()) {
                            val wrapper = historyArray.getJSONObject(i)
                            val item = wrapper.optJSONObject("content") ?: wrapper

                            val linkId = item.optString("linkid")
                            val title = item.optString("title")

                            val author = "历史记录"
                            val avatar = null

                            // 图片解析
                            val imgsList = mutableListOf<String>()
                            val singleImg = item.optString("image")
                            if (singleImg.isNotEmpty()) {
                                var u = singleImg
                                if (u.startsWith("http://")) u = u.replace("http://", "https://")
                                imgsList.add(u)
                            }

                            val isArticle = item.optInt("use_concept_type", 1) == 0
                            newPosts.add(HeyPost(linkId, title, author, avatar, "", imgsList, isArticle))
                        }
                    }
                }
                // 场景 B: 我的动态 (数据在根节点 post_links, 内容就是 item 本身)
                else if (type == "my_post") {
                    // 注意post_links在根节点，不在result里
                    val postLinks = root.optJSONArray("post_links")
                    if (postLinks != null && postLinks.length() > 0) {
                        for (i in 0 until postLinks.length()) {
                            val item = postLinks.getJSONObject(i)

                            val linkId = item.optString("linkid")
                            val title = item.optString("title").ifEmpty { item.optString("description").take(20) }
                            val desc = item.optString("description")

                            val author = "我"
                            val rootUser = root.optJSONObject("user")
                            var avatar = rootUser?.optString("avatar")
                            if (avatar != null && avatar.startsWith("http://")) avatar = avatar.replace("http://", "https://")

                            val imgsList = mutableListOf<String>()
                            val imgsJson = item.optJSONArray("imgs") // 这里的imgs是String数组
                            if (imgsJson != null) {
                                for (j in 0 until imgsJson.length()) {
                                    var u = imgsJson.getString(j)
                                    if (u.startsWith("http://")) u = u.replace("http://", "https://")
                                    imgsList.add(u)
                                }
                            }

                            val isArticle = item.optInt("use_concept_type", 1) == 0
                            newPosts.add(HeyPost(linkId, title, author, avatar, desc, imgsList, isArticle))
                        }
                    }
                }
                // 收藏夹（没写好）
                else {
                    val linksArray = root.optJSONObject("result")?.optJSONArray("links")
                    if (linksArray != null && linksArray.length() > 0) {
                        for (i in 0 until linksArray.length()) {
                            val wrapper = linksArray.getJSONObject(i)
                            val item = wrapper.optJSONObject("link") ?: wrapper 

                            val linkId = item.optString("linkid")
                            val title = item.optString("title")
                            val desc = item.optString("description")

                            val userObj = item.optJSONObject("user")
                            val author = userObj?.optString("username") ?: "未知"
                            var avatar = userObj?.optString("avatar")
                            if (avatar != null && avatar.startsWith("http://")) avatar = avatar.replace("http://", "https://")

                            val imgsList = mutableListOf<String>()
                            val thumbs = item.optJSONArray("thumbs")
                            if (thumbs != null) {
                                for (j in 0 until thumbs.length()) {
                                    var u = thumbs.getString(j)
                                    if (u.startsWith("http://")) u = u.replace("http://", "https://")
                                    imgsList.add(u)
                                }
                            } else {
                                val imgsJson = item.optJSONArray("imgs")
                                if (imgsJson != null) {
                                    for (j in 0 until imgsJson.length()) {
                                        var u = imgsJson.getString(j)
                                        if (u.startsWith("http://")) u = u.replace("http://", "https://")
                                        imgsList.add(u)
                                    }
                                }
                            }

                            val isArticle = item.optInt("use_concept_type", 1) == 0
                            newPosts.add(HeyPost(linkId, title, author, avatar, desc, imgsList, isArticle))
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    if (newPosts.isEmpty()) {
                        isEnd = true
                        if (offset == 0) statusText = "暂无数据" else statusText = ""
                    } else {
                        postList.addAll(newPosts)
                        offset += newPosts.size
                        statusText = ""
                    }
                    isLoading = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { statusText = "加载失败: ${e.message}"; isLoading = false }
            }
        }
    }

    LaunchedEffect(Unit) { loadData() }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        // PostListScreenUI
        PostListScreen(
            posts = postList,
            status = if(isLoading && postList.isEmpty()) "" else statusText,
            listState = listState,
            onItemClick = onItemClick,
            onLoadMore = { loadData() }
        )

        if (!isLoading || postList.isNotEmpty()) {
            Text(
                text = titleStr,
                color = Color(0xFF00C0FF),
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp)
            )
        }

        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF333333)),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp).height(30.dp)
        ) {
            Text("返回", fontSize = 11.sp)
        }

        if (isLoading && postList.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(30.dp))
            }
        }

        if (!isLoading && postList.isEmpty() && statusText == "暂无数据") {
            Text(
                "暂无内容",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
