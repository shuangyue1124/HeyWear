package com.m16a4666.heywear.interact

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.SwipeToDismissBox
import androidx.wear.compose.material.SwipeToDismissValue
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.rememberSwipeToDismissBoxState
import androidx.wear.compose.material.scrollAway
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.m16a4666.heywear.model.HeyPost
import com.m16a4666.heywear.utils.CookieUtil
import com.m16a4666.heywear.utils.CrashHandler
import com.m16a4666.heywear.utils.DebugLogger
import com.m16a4666.heywear.utils.DebugOverlay
import com.m16a4666.heywear.utils.DeviceUtil
import com.m16a4666.heywear.utils.FileLogger
import com.m16a4666.heywear.utils.HeyboxSigner
import com.m16a4666.heywear.utils.SettingsUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {
    companion object { var GlobalCookie = "" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CrashHandler.init(this)
        FileLogger.write(this, "System", "App Created (v0.3.5)")
        val imageLoader = ImageLoader.Builder(this)
            .components {
                if (Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory())
                else add(GifDecoder.Factory())
            }
            .build()
        Coil.setImageLoader(imageLoader)
        setContent { WearApp() }
    }
}

@Composable
fun WearApp() {
    val context = LocalContext.current
    var crashLog by remember { mutableStateOf(CrashHandler.getAndClearCrashLog(context)) }

    if (crashLog != null) {
        CrashScreen(errorLog = crashLog!!, onDismiss = { CrashHandler.clearLog(context); crashLog = null })
        return
    }

    var isLoggedIn by remember {
        mutableStateOf(try { CookieUtil.isLoggedIn(context) } catch (e: Exception) { false })
    }
    var isGuest by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoggedIn || isGuest) {
            MainContent(
                context = context,
                onLogout = {
                    isLoggedIn = false
                    isGuest = false
                    MainActivity.GlobalCookie = ""
                    DebugLogger.log("User", "Logged out")
                }
            )
        } else {
            LoginScreen(
                onLoginSuccess = {
                    val newCookie = MainActivity.GlobalCookie
                    if (newCookie.isNotEmpty()) {
                        CookieUtil.saveCookie(context, newCookie)
                        isLoggedIn = true
                    }
                },
                onSkip = { isGuest = true }
            )
        }
        DebugOverlay()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainContent(
    context: android.content.Context,
    onLogout: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    var showTime by remember { mutableStateOf(SettingsUtil.isShowTime(context)) }

    // 读取禁用滑动设置
    var isSwipeDisabled by remember { mutableStateOf(SettingsUtil.isSwipeDisabled(context)) }

    var currentPost by remember { mutableStateOf<HeyPost?>(null) }
    val postList = remember { mutableStateListOf<HeyPost>() }
    var statusText by remember { mutableStateOf("加载中...") }
    var pageOffset by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun loadData(isRefresh: Boolean = false) {
        if (isLoading) return
        isLoading = true
        if (isRefresh) statusText = "刷新中..."

        scope.launch(Dispatchers.IO) {
            try {
                val cookie = CookieUtil.getCookie(context)
                val newPosts = fetchFeedData(context, cookie, if(isRefresh) 0 else pageOffset)
                withContext(Dispatchers.Main) {
                    if (isRefresh) { postList.clear(); pageOffset = 0 }
                    postList.addAll(newPosts)
                    pageOffset += newPosts.size
                    statusText = ""
                    isLoading = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { statusText = if(postList.isEmpty()) "加载失败" else ""; isLoading = false }
            }
        }
    }

    LaunchedEffect(Unit) { if (postList.isEmpty()) loadData(true) }

    val pagerState = rememberPagerState(pageCount = { 2 })

    Scaffold(
        timeText = {
            if (showTime) {
                if (currentPost == null && pagerState.currentPage == 0)
                    TimeText(modifier = Modifier.scrollAway(listState))
                else TimeText()
            }
        }
    ) {
        // 通过confirmStateChange拦截滑动
        val swipeState = rememberSwipeToDismissBoxState(
            confirmStateChange = { destValue ->
                if (destValue == SwipeToDismissValue.Dismissed) {
                    if (isSwipeDisabled) {
                        false
                    } else {
                        currentPost = null
                        true
                    }
                } else {
                    true
                }
            }
        )

        // 只有当非禁用模式，且确实触发了Dismissed时，才重置状态
        LaunchedEffect(swipeState.currentValue) {
            if (swipeState.currentValue == SwipeToDismissValue.Dismissed) {
                // 清空currentPost
                currentPost = null
                swipeState.snapTo(SwipeToDismissValue.Default)
            }
        }

        SwipeToDismissBox(
            state = swipeState,
            backgroundKey = "main_pager",
            contentKey = currentPost?.linkId ?: "pager",
            hasBackground = true
        ) { isBackground ->
            if (isBackground || currentPost == null) {
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                    if (page == 0) {
                        Box(Modifier.fillMaxSize()) {
                            PostListScreen(
                                posts = postList,
                                status = if (isLoading) "" else statusText,
                                listState = listState,
                                onItemClick = { currentPost = it },
                                onLoadMore = { loadData(false) }
                            )
                            if (isLoading && postList.isEmpty()) {
                                Column(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.4f)), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(modifier = Modifier.size(30.dp), strokeWidth = 3.dp)
                                    Spacer(Modifier.height(8.dp))
                                    Text("加载中...", color = Color.White, fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        PersonalCenterScreen(
                            context = context,
                            onLogout = onLogout,
                            onRefreshSettings = {
                                showTime = SettingsUtil.isShowTime(context)
                                isSwipeDisabled = SettingsUtil.isSwipeDisabled(context)
                            },
                            onPostClick = { post -> currentPost = post }
                        )
                    }
                }
            } else {
                PostDetailScreen(post = currentPost!!, onBack = { currentPost = null })
            }
        }
    }
}

private fun fetchFeedData(context: android.content.Context, cookie: String, offset: Int): List<HeyPost> {
    val urlPath = "/bbs/app/feeds"
    val deviceId = DeviceUtil.getDeviceId(context)
    val randomUA = DeviceUtil.getRandomUA()
    val baseParams = "pull=0&offset=$offset&dw=304&os_type=web&app=web&client_type=web&version=999.0.4&web_version=2.5&x_client_type=web&x_app=heybox_website&heybox_id=&x_os_type=Windows&device_id=$deviceId"

    val time = HeyboxSigner.getTime()
    val nonce = HeyboxSigner.getNonce(time)
    val hkey = HeyboxSigner.getHkey(urlPath, time, nonce)
    val finalUrl = "https://api.xiaoheihe.cn$urlPath?$baseParams&_time=$time&nonce=$nonce&hkey=$hkey"

    val conn = URL(finalUrl).openConnection() as java.net.HttpURLConnection
    conn.requestMethod = "GET"
    conn.setRequestProperty("User-Agent", randomUA)
    conn.setRequestProperty("Referer", "https://www.xiaoheihe.cn/")
    if (cookie.isNotEmpty()) conn.setRequestProperty("Cookie", cookie)

    conn.connect()
    val jsonStr = conn.inputStream.bufferedReader().readText()
    FileLogger.logNetwork(context, finalUrl, conn.responseCode, emptyMap(), jsonStr.take(500))

    val root = JSONObject(jsonStr)
    if (root.optString("status") == "failed") {
        val msg = root.optString("msg")
        DebugLogger.log("API_FAIL", msg)
        throw Exception("API: $msg")
    }

    val linksArray = root.optJSONObject("result")?.optJSONArray("links") ?: return emptyList()
    val list = mutableListOf<HeyPost>()

    for (i in 0 until linksArray.length()) {
        val item = linksArray.getJSONObject(i)
        val isArticle = item.optInt("use_concept_type", 1) == 0
        val linkId = item.optString("linkid")
        val desc = item.optString("description", "")
        var title = item.optString("title", "")
        if (title.isEmpty()) title = desc.take(20)

        val userObj = item.optJSONObject("user")
        val author = userObj?.optString("username") ?: "匿名"
        var avatar = userObj?.optString("avatar")
        if (avatar != null && avatar.startsWith("http://")) avatar = avatar.replace("http://", "https://")

        val imgsList = mutableListOf<String>()
        val imgsJson = item.optJSONArray("imgs")
        if (imgsJson != null) {
            for (j in 0 until imgsJson.length()) {
                var url = imgsJson.getString(j)
                if (url.startsWith("http://")) url = url.replace("http://", "https://")
                imgsList.add(url)
            }
        }
        list.add(HeyPost(linkId, title, author, avatar, desc, imgsList, isArticle))
    }
    return list
}