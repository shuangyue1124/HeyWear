package com.m16a4666.heywear.interact

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.text.Html
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.*
import coil.compose.AsyncImage
import com.m16a4666.heywear.model.ContentNode
import com.m16a4666.heywear.model.HeyPost
import com.m16a4666.heywear.utils.CookieUtil
import com.m16a4666.heywear.utils.DeviceUtil
import com.m16a4666.heywear.utils.FileLogger
import com.m16a4666.heywear.utils.HeyboxSigner
import com.m16a4666.heywear.utils.ImageSaver
import com.m16a4666.heywear.utils.SettingsUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PostDetailScreen(post: HeyPost, onBack: () -> Unit) {
    val context = LocalContext.current
    val inlineContentMap = remember { RichTextHelper.getInlineContentMap() }
    val isGlobalNoImage = remember { SettingsUtil.isNoImageMode(context) }

    var headerInfo by remember { mutableStateOf<ContentNode.HeaderNode?>(null) }
    var contentNodes by remember { mutableStateOf(emptyList<ContentNode>()) }
    var momentImages by remember { mutableStateOf(emptyList<String>()) }
    var fullDescription by remember { mutableStateOf("") }
    var momentHasVideo by remember { mutableStateOf(false) }

    var viewMode by remember { mutableStateOf("loading") }
    var errorMsg by remember { mutableStateOf("") }
    var showFullImage by remember { mutableStateOf<String?>(null) }

    // 评论区显示状态
    var showComments by remember { mutableStateOf(false) }

    // 评论区逻辑
    if (showComments) {
        val commentSwipeState = rememberSwipeToDismissBoxState()
        LaunchedEffect(commentSwipeState.currentValue) {
            if (commentSwipeState.currentValue == SwipeToDismissValue.Dismissed) {
                showComments = false
                commentSwipeState.snapTo(SwipeToDismissValue.Default)
            }
        }

        SwipeToDismissBox(state = commentSwipeState) { isBg ->
            if (!isBg) {
                CommentScreen(linkId = post.linkId, onBack = { showComments = false })
            } else {
                Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                    Text("返回文章", color = Color.Gray)
                }
            }
        }
        return
    }

    // 全屏大图
    if (showFullImage != null) {
        Dialog(onDismissRequest = { showFullImage = null }) {
            var scale by remember { mutableFloatStateOf(1f) }
            var offsetX by remember { mutableFloatStateOf(0f) }
            var offsetY by remember { mutableFloatStateOf(0f) }
            Box(
                Modifier.fillMaxSize().background(Color.Black)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 4f)
                            if (scale > 1f) {
                                val maxOffset = (scale - 1) * 300
                                offsetX = (offsetX + pan.x).coerceIn(-maxOffset, maxOffset)
                                offsetY = (offsetY + pan.y).coerceIn(-maxOffset, maxOffset)
                            } else { offsetX = 0f; offsetY = 0f }
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = { scale = if (scale > 1f) 1f else 2.5f; offsetX = 0f; offsetY = 0f },
                            onTap = { showFullImage = null }
                        )
                    }
            ) {
                AsyncImage(model = showFullImage, contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().align(Alignment.Center).graphicsLayer(scaleX = scale, scaleY = scale, translationX = offsetX, translationY = offsetY))
            }
        }
    }

    // 联网解析
    LaunchedEffect(post.linkId) {
        withContext(Dispatchers.IO) {
            try {
                val path = "/bbs/app/link/tree"
                val cookie = CookieUtil.getCookie(context)
                val time = HeyboxSigner.getTime()
                val nonce = HeyboxSigner.getNonce(time)
                val hkey = HeyboxSigner.getHkey(path, time, nonce)

                // 使用DeviceUtil获取动态参数
                val deviceId = DeviceUtil.getDeviceId(context)
                val randomUA = DeviceUtil.getRandomUA()

                val baseParams = "os_type=web&app=heybox&client_type=web&version=999.0.4&web_version=2.5&x_client_type=web&x_app=heybox_website&heybox_id=&x_os_type=Windows&device_info=Chrome&device_id=$deviceId"
                val url = "https://api.xiaoheihe.cn$path?link_id=${post.linkId}&$baseParams&_time=$time&nonce=$nonce&hkey=$hkey"

                val conn = URL(url).openConnection() as HttpURLConnection
                // 设置 UA
                conn.setRequestProperty("User-Agent", randomUA)
                conn.setRequestProperty("Referer", "https://www.xiaoheihe.cn/")
                if (cookie.isNotEmpty()) conn.setRequestProperty("Cookie", cookie)

                val jsonStr = conn.inputStream.bufferedReader().readText()
                val root = JSONObject(jsonStr)

                // 错误处理
                if (root.optString("status") == "failed") {
                    throw Exception("API Error: ${root.optString("msg")} (${root.optString("code")})")
                }

                val resultObj = root.optJSONObject("result") ?: throw Exception("No Result: $jsonStr")
                val linkObj = resultObj.optJSONObject("link") ?: throw Exception("Link is NULL!")

                val userObj = linkObj.optJSONObject("user")
                val authorName = userObj?.optString("username") ?: post.author
                var avatarUrl = userObj?.optString("avatar") ?: post.avatar
                if (avatarUrl?.startsWith("http://") == true) avatarUrl = avatarUrl.replace("http://", "https://")
                val createTime = linkObj.optLong("create_at") * 1000
                val dateStr = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(createTime))
                headerInfo = ContentNode.HeaderNode(linkObj.optString("title", post.title), authorName, avatarUrl, dateStr)

                val useConceptType = linkObj.optInt("use_concept_type", -1)
                val hasVideo = linkObj.optInt("has_video", 0) == 1
                val textRaw = linkObj.optString("text")
                var jsonArray = JSONArray()
                if (textRaw.isNotEmpty() && textRaw.startsWith("[")) {
                    try { jsonArray = JSONArray(textRaw) } catch (e: Exception) { e.printStackTrace() }
                }

                val isArticleMode = (useConceptType == 0)

                if (isArticleMode && jsonArray.length() > 0) {
                    // 文章模式
                    val nodeList = mutableListOf<ContentNode>()
                    val firstNode = jsonArray.getJSONObject(0)
                    if (firstNode.optString("type") == "html") {
                        val htmlContent = firstNode.optString("text")
                        val doc = Jsoup.parseBodyFragment(htmlContent)
                        for (element in doc.body().children()) {
                            when (element.tagName()) {
                                "p" -> {
                                    val imgTag = element.selectFirst("img")
                                    if (imgTag != null) {
                                        var imgUrl = imgTag.attr("data-original")
                                        if (imgUrl.isEmpty()) imgUrl = imgTag.attr("src")
                                        if (imgUrl.isNotEmpty()) {
                                            if (imgUrl.startsWith("http://")) imgUrl = imgUrl.replace("http://", "https://")
                                            nodeList.add(ContentNode.ImageNode(imgUrl))
                                        }
                                    } else {
                                        val text = element.text()
                                        if (text.isNotBlank()) nodeList.add(ContentNode.TextNode(text))
                                    }
                                }
                                "h4" -> {
                                    val caption = element.text()
                                    if (caption.isNotBlank()) nodeList.add(ContentNode.TextNode("📌 $caption"))
                                }
                                "blockquote" -> {
                                    val quoteText = element.text()
                                    if (quoteText.isNotBlank()) nodeList.add(ContentNode.TextNode("QUOTE:$quoteText"))
                                }
                                "ul", "ol" -> {
                                    for (li in element.children()) {
                                        if (li.tagName() == "li") nodeList.add(ContentNode.TextNode("• ${li.text()}"))
                                    }
                                }
                                else -> {
                                    val text = element.text()
                                    if (text.isNotBlank()) nodeList.add(ContentNode.TextNode(text))
                                }
                            }
                        }
                    }
                    for (i in 0 until jsonArray.length()) {
                        val node = jsonArray.getJSONObject(i)
                        if (node.optString("type") == "video") {
                            var coverUrl = node.optString("url")
                            if (coverUrl.isNotEmpty()) nodeList.add(ContentNode.ImageNode(coverUrl + "###VIDEO###"))
                        }
                    }
                    contentNodes = nodeList
                    viewMode = if (contentNodes.isNotEmpty()) "article" else "moment"
                } else {
                    // 动态模式
                    val imgList = mutableListOf<String>()
                    val sb = StringBuilder()
                    val imgsJson = linkObj.optJSONArray("imgs")
                    if (imgsJson != null && imgsJson.length() > 0) {
                        for (i in 0 until imgsJson.length()) imgList.add(imgsJson.getString(i).replace("http://", "https:"))
                    } else if (post.images.isNotEmpty()) {
                        imgList.addAll(post.images)
                    }
                    for (i in 0 until jsonArray.length()) {
                        val node = jsonArray.getJSONObject(i)
                        val type = node.optString("type")
                        if (type == "html" || type == "text") {
                            var text = node.optString("text").replace(Regex("<img[^>]*>"), "")
                            text = Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT).toString()
                            if (text.isNotBlank()) sb.append(text).append("\n\n")
                        }
                    }
                    if (sb.isEmpty()) sb.append(linkObj.optString("description", post.description))
                    momentImages = imgList
                    fullDescription = sb.toString()
                    momentHasVideo = hasVideo
                    viewMode = "moment"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                errorMsg = e.message ?: "Error"
                viewMode = "error"
                FileLogger.write(context, "PostDetailCrash", e.stackTraceToString())
            }
        }
    }

    // UI
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (viewMode == "loading") {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (viewMode == "error") {
            ScalingLazyColumn(modifier = Modifier.fillMaxSize(), anchorType = ScalingLazyListAnchorType.ItemStart) {
                item { Button(onClick = onBack, colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF333333)), modifier = Modifier.height(30.dp)) { Text("返回", fontSize = 11.sp) } }
                item { Text(errorMsg, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(20.dp)) }
            }
        } else {
            ScalingLazyColumn(modifier = Modifier.fillMaxSize(), anchorType = ScalingLazyListAnchorType.ItemStart) {
                item { Button(onClick = onBack, colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF333333)), modifier = Modifier.height(32.dp).padding(bottom = 5.dp)) { Text("返回列表", fontSize = 11.sp) } }

                if (headerInfo != null) {
                    item {
                        Column(Modifier.padding(horizontal = 4.dp, vertical = 5.dp)) {
                            val titleText = RichTextHelper.buildSpannedString(headerInfo!!.title)
                            Text(text = titleText, inlineContent = inlineContentMap, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White, lineHeight = 20.sp)
                            Spacer(Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (headerInfo!!.avatar != null) {
                                    AsyncImage(model = headerInfo!!.avatar, contentDescription = null, modifier = Modifier.size(20.dp).clip(CircleShape))
                                    Spacer(Modifier.width(6.dp))
                                }
                                Text(headerInfo!!.author, fontSize = 12.sp, color = Color(0xFF00C0FF))
                                Spacer(Modifier.width(8.dp))
                                Text(headerInfo!!.time, fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                if (viewMode == "article") {
                    items(contentNodes) { node ->
                        when (node) {
                            is ContentNode.TextNode -> {
                                if (node.html.startsWith("QUOTE:")) {
                                    val realText = node.html.removePrefix("QUOTE:")
                                    val annotatedText = RichTextHelper.buildSpannedString(realText)
                                    Row(Modifier.padding(vertical = 4.dp)) {
                                        Box(Modifier.width(4.dp).height(20.dp).background(Color.Gray))
                                        Spacer(Modifier.width(8.dp))
                                        Text(text = annotatedText, inlineContent = inlineContentMap, fontSize = 13.sp, color = Color.LightGray, lineHeight = 18.sp)
                                    }
                                } else {
                                    val annotatedText = RichTextHelper.buildSpannedString(node.html)
                                    Text(text = annotatedText, inlineContent = inlineContentMap, fontSize = 14.sp, color = if(node.html.startsWith("📌")) Color.Gray else Color(0xFFDDDDDD), lineHeight = 18.sp, modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }
                            is ContentNode.ImageNode -> {
                                val cleanUrl = node.url.replace("###VIDEO###", "")
                                val isVideo = node.url.contains("###VIDEO###")
                                SmartImage(
                                    url = cleanUrl,
                                    isGlobalNoImage = isGlobalNoImage,
                                    isVideo = isVideo,
                                    onFullClick = { showFullImage = cleanUrl }
                                )
                            }
                            else -> {}
                        }
                    }
                } else {
                    if (momentImages.isNotEmpty()) {
                        item {
                            Box(contentAlignment = Alignment.TopEnd) {
                                val pagerState = rememberPagerState(pageCount = { momentImages.size })
                                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().height(160.dp)) { page ->
                                    val imgUrl = momentImages[page]
                                    val isVideo = (momentHasVideo && page == 0)
                                    SmartImage(
                                        url = imgUrl,
                                        isGlobalNoImage = isGlobalNoImage,
                                        isVideo = isVideo,
                                        onFullClick = { showFullImage = imgUrl.split("?")[0] }
                                    )
                                }
                                if (momentImages.size > 1) Text("${pagerState.currentPage + 1}/${momentImages.size}", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(4.dp).background(Color.Black.copy(0.6f), RoundedCornerShape(4.dp)).padding(2.dp))
                            }
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                    }
                    if (fullDescription.isNotEmpty()) {
                        item {
                            val annotatedText = RichTextHelper.buildSpannedString(fullDescription)
                            Text(text = annotatedText, inlineContent = inlineContentMap, fontSize = 14.sp, color = Color(0xFFDDDDDD), lineHeight = 18.sp, modifier = Modifier.padding(bottom = 20.dp))
                        }
                    }
                }

                item {
                    Chip(
                        onClick = { showComments = true },
                        label = { Text("查看评论区", color = Color.White) },
                        colors = ChipDefaults.secondaryChipColors(),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp)
                    )
                }

                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }
}

// 智能图片组件
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SmartImage(
    url: String,
    isGlobalNoImage: Boolean,
    isVideo: Boolean,
    onFullClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isRevealed by remember { mutableStateOf(false) }

    val thumbUrl = if (url.contains("?")) url + "/thumbnail/500x" else url + "?imageMogr2/thumbnail/500x"
    val originalUrl = url.split("?")[0]

    val shouldShow = !isGlobalNoImage || isRevealed

    // 权限请求
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scope.launch { ImageSaver.saveImage(context, originalUrl) }
        } else {
            Toast.makeText(context, "需要存储权限才能保存", Toast.LENGTH_SHORT).show()
        }
    }

    if (!shouldShow) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(vertical = 6.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF222222))
                .clickable { isRevealed = true },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isVideo) "▶️ 视频封面" else "🖼️ 点击加载图片",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Text(
                    text = "(省流模式)",
                    color = Color.DarkGray,
                    fontSize = 10.sp
                )
            }
        }
    } else {
        Box(contentAlignment = Alignment.Center) {
            AsyncImage(
                model = thumbUrl,
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .combinedClickable(
                        onClick = { onFullClick() },
                        onLongClick = {
                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                    scope.launch { ImageSaver.saveImage(context, originalUrl) }
                                } else {
                                    permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                }
                            } else {
                                scope.launch { ImageSaver.saveImage(context, originalUrl) }
                            }
                        }
                    )
            )
            if (isVideo) {
                PlayIcon()
            }
        }
    }
}

@Composable
fun PlayIcon() {
    Box(Modifier.size(40.dp).background(Color.Black.copy(0.6f), CircleShape), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val path = Path().apply {
                moveTo(size.width * 0.35f, size.height * 0.25f)
                lineTo(size.width * 0.35f, size.height * 0.75f)
                lineTo(size.width * 0.75f, size.height * 0.50f)
                close()
            }
            drawPath(path, color = Color.White)
        }
    }
}