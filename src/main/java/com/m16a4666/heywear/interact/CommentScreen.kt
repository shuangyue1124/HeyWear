package com.m16a4666.heywear.interact

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.wear.compose.foundation.SwipeToDismissValue
import androidx.wear.compose.foundation.rememberSwipeToDismissBoxState
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.*
import coil.compose.AsyncImage
import com.m16a4666.heywear.model.HeyComment
import com.m16a4666.heywear.utils.CookieUtil
import com.m16a4666.heywear.utils.DeviceUtil
import com.m16a4666.heywear.utils.HeyboxHttpClient
import com.m16a4666.heywear.utils.HeyboxSigner
import com.m16a4666.heywear.utils.requireHeyboxApiOk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CommentScreen(linkId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val listState = rememberScalingLazyListState()

    // 数据状态
    val commentList = remember { mutableStateListOf<HeyComment>() }
    var page by remember { mutableIntStateOf(1) }
    var isLoading by remember { mutableStateOf(false) }
    var isEnd by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("加载评论中...") }

    // 交互状态
    var focusedComment by remember { mutableStateOf<HeyComment?>(null) }
    var showFullImage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val saveImage = rememberImageSaveAction()

    // 解析
    fun parseJsonToComment(c: JSONObject): HeyComment {
        val user = c.optJSONObject("user")
        val createTime = c.optLong("create_at") * 1000
        val dateStr = SimpleDateFormat("MM-dd", Locale.getDefault()).format(Date(createTime))

        var avatarUrl = user?.optString("avatar") ?: ""
        if (avatarUrl.startsWith("http://")) avatarUrl = avatarUrl.replace("http://", "https://")

        // 解析评论图片
        var commentImgUrl: String? = null
        val imgsArray = c.optJSONArray("imgs")
        if (imgsArray != null && imgsArray.length() > 0) {
            val imgObj = imgsArray.getJSONObject(0)
            commentImgUrl = imgObj.optString("url")
            if (commentImgUrl.startsWith("http://")) commentImgUrl = commentImgUrl.replace("http://", "https://")
        }

        return HeyComment(
            id = c.optString("commentid"),
            userId = c.optString("userid"),
            userName = user?.optString("username") ?: "匿名",
            avatar = avatarUrl,
            content = c.optString("text"),
            time = dateStr,
            location = c.optString("ip_location", ""),
            likeCount = c.optInt("up", 0),
            isOwner = c.optInt("is_link_owner", 0) == 1,
            childNum = c.optInt("child_num", 0),
            children = emptyList(), 
            imgUrl = commentImgUrl
        )
    }

    // 加载部分
    fun loadComments() {
        if (isLoading || isEnd) return
        isLoading = true
        scope.launch(Dispatchers.IO) {
            try {
                val path = "/bbs/app/link/tree"
                val cookie = CookieUtil.getCookie(context)
                val time = HeyboxSigner.getTime()
                val nonce = HeyboxSigner.getNonce(time)
                val hkey = HeyboxSigner.getHkey(path, time, nonce)
                val baseParams = "os_type=web&app=heybox&client_type=web&version=999.0.4&web_version=2.5&x_client_type=web&x_app=heybox_website&heybox_id=&x_os_type=Windows&limit=20&owner_only=0"
                val url = "https://api.xiaoheihe.cn$path?link_id=$linkId&page=$page&$baseParams&_time=$time&nonce=$nonce&hkey=$hkey"

                val response = HeyboxHttpClient.get(
                    url = url,
                    userAgent = DeviceUtil.getRandomUA(),
                    cookie = cookie
                )
                val root = JSONObject(response.body)
                requireHeyboxApiOk(root.optString("status"), root.optString("msg"))

                val result = root.optJSONObject("result")
                val commentsArray = result?.optJSONArray("comments")

                val newComments = mutableListOf<HeyComment>()
                if (commentsArray != null) {
                    for (i in 0 until commentsArray.length()) {
                        val floorObj = commentsArray.getJSONObject(i)
                        val groupList = floorObj.optJSONArray("comment")

                        if (groupList != null && groupList.length() > 0) {
                            // 解析楼主(Index0)
                            val parentJson = groupList.getJSONObject(0)
                            var parentComment = parseJsonToComment(parentJson)

                            // 解析子回复(Index 1到N)
                            val childrenList = mutableListOf<HeyComment>()
                            for (k in 1 until groupList.length()) {
                                childrenList.add(parseJsonToComment(groupList.getJSONObject(k)))
                            }

                            parentComment = parentComment.copy(children = childrenList)
                            newComments.add(parentComment)
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    if (newComments.isEmpty()) isEnd = true
                    else { commentList.addAll(newComments); page++ }
                    isLoading = false; statusText = ""
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    statusText = "加载失败: ${e.message ?: "网络异常"}"
                    isLoading = false
                }
            }
        }
    }

    LaunchedEffect(Unit) { loadComments() }

    // 全屏看图部分
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

    // 楼中楼详情页跳转（画饼）
    if (focusedComment != null) {
        val subSwipeState = rememberSwipeToDismissBoxState()
        LaunchedEffect(subSwipeState.currentValue) {
            if (subSwipeState.currentValue == SwipeToDismissValue.Dismissed) {
                focusedComment = null
                subSwipeState.snapTo(SwipeToDismissValue.Default)
            }
        }
        SwipeToDismissBox(state = subSwipeState) { isBg ->
            if (!isBg) {
                ReplyDetailScreen(
                    parent = focusedComment!!,
                    onBack = { focusedComment = null },
                    onImageClick = { url -> showFullImage = url },
                    onImageLongClick = saveImage
                )
            }
        }
        return
    }

    // 主列表界面
    Box(Modifier.fillMaxSize().background(Color.Black)) {

        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            anchorType = ScalingLazyListAnchorType.ItemStart,
            contentPadding = PaddingValues(top = 20.dp, bottom = 40.dp, start = 10.dp, end = 10.dp)
        ) {
            item {
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF333333)), modifier = Modifier.height(30.dp).padding(bottom = 5.dp)) { Text("返回详情", fontSize = 11.sp) }
            }

            itemsIndexed(commentList) { index, comment ->
                if (index >= commentList.size - 3) LaunchedEffect(Unit) { loadComments() }

                CommentCard(
                    comment = comment,
                    onClick = { focusedComment = comment },
                    onImageClick = { url -> showFullImage = url },
                    onImageLongClick = saveImage
                )
            }

            if (isEnd && commentList.isNotEmpty()) {
                item { Text("--- 到底了 ---", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(10.dp)) }
            }
        }

        if (isLoading && commentList.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(30.dp), strokeWidth = 3.dp)
            }
        }

        if (statusText.isNotEmpty() && !isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(statusText, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

// 评论卡片组件
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CommentCard(
    comment: HeyComment,
    onClick: () -> Unit,
    onImageClick: (String) -> Unit,
    onImageLongClick: (String) -> Unit
) {
    val inlineContentMap = remember { RichTextHelper.getInlineContentMap() }
    val richContent = remember(comment.content) { RichTextHelper.buildSpannedString(comment.content) }

    Card(
        onClick = onClick,
        backgroundPainter = CardDefaults.cardBackgroundPainter(Color(0xFF1A1A1A)),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Column {
            // 头部
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(model = comment.avatar, contentDescription = null, modifier = Modifier.size(16.dp).clip(CircleShape))
                Spacer(Modifier.width(6.dp))
                Text(comment.userName, fontSize = 11.sp, color = Color(0xFF00C0FF), fontWeight = FontWeight.Bold)
                if (comment.isOwner) {
                    Text("楼主", color = Color.Black, fontSize = 8.sp, modifier = Modifier.padding(start=4.dp).background(Color(0xFFFFD700), RoundedCornerShape(2.dp)).padding(horizontal = 2.dp))
                }
                Spacer(Modifier.weight(1f))
                Text(comment.time, fontSize = 9.sp, color = Color.Gray)
            }

            Spacer(Modifier.height(4.dp))
            Text(text = richContent, inlineContent = inlineContentMap, fontSize = 12.sp, color = Color.White)

            // 评论配图
            if (comment.imgUrl != null) {
                val originalUrl = comment.imgUrl.split("?")[0]
                Spacer(Modifier.height(4.dp))
                AsyncImage(
                    model = comment.imgUrl + "/thumbnail/300x",
                    contentDescription = "评论图片",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .combinedClickable(
                            onClick = { onImageClick(originalUrl) },
                            onLongClick = { onImageLongClick(originalUrl) }
                        )
                )
            }

            // 子评论预览
            if (comment.children.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)).background(Color.White.copy(alpha = 0.08f)).padding(6.dp)
                ) {
                    comment.children.take(3).forEach { child ->
                        Row(modifier = Modifier.padding(bottom = 2.dp)) {
                            if (child.isOwner) Text("楼主 ", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("${child.userName}: ", color = Color.Gray, fontSize = 10.sp)
                            val childRich = RichTextHelper.buildSpannedString(child.content)
                            Text(text = childRich, inlineContent = inlineContentMap, fontSize = 10.sp, color = Color.LightGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    if (comment.childNum > 3) {
                        Text("共 ${comment.childNum} 条回复 >", fontSize = 10.sp, color = Color.LightGray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(comment.location, fontSize = 9.sp, color = Color.DarkGray)
                Text("👍 ${comment.likeCount}", fontSize = 9.sp, color = Color.DarkGray)
            }
        }
    }
}

// 楼中楼
@OptIn(ExperimentalFoundationApi::class) 
@Composable
fun ReplyDetailScreen(
    parent: HeyComment,
    onBack: () -> Unit,
    onImageClick: (String) -> Unit,
    onImageLongClick: (String) -> Unit
) {
    val inlineContentMap = remember { RichTextHelper.getInlineContentMap() }

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        anchorType = ScalingLazyListAnchorType.ItemStart,
        contentPadding = PaddingValues(top = 20.dp, bottom = 40.dp, start = 10.dp, end = 10.dp)
    ) {
        item { Button(onClick = onBack, colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF333333)), modifier = Modifier.height(30.dp)) { Text("返回评论区", fontSize = 11.sp) } }

        // 层主详情
        item {
            Column(Modifier.padding(8.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(model = parent.avatar, contentDescription = null, modifier = Modifier.size(24.dp).clip(CircleShape))
                    Spacer(Modifier.width(8.dp))
                    Text(parent.userName, fontSize = 13.sp, color = Color(0xFF00C0FF))
                    if (parent.isOwner) {
                        Text("楼主", color = Color.Black, fontSize = 9.sp, modifier = Modifier.padding(start=4.dp).background(Color(0xFFFFD700), RoundedCornerShape(2.dp)).padding(horizontal = 2.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                val richContent = remember(parent.content) { RichTextHelper.buildSpannedString(parent.content) }
                Text(text = richContent, inlineContent = inlineContentMap, fontSize = 14.sp, color = Color.White)

                // 层主图片
                if (parent.imgUrl != null) {
                    val originalUrl = parent.imgUrl.split("?")[0]
                    Spacer(Modifier.height(8.dp))
                    AsyncImage(
                        model = parent.imgUrl + "/thumbnail/500x",
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .combinedClickable(
                                onClick = { onImageClick(originalUrl) },
                                onLongClick = { onImageLongClick(originalUrl) }
                            )
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${parent.time} · ${parent.location}", fontSize = 10.sp, color = Color.Gray)
                    Text("👍 ${parent.likeCount}", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }

        item { Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.DarkGray).padding(vertical = 8.dp)) }

        // 子回复列表
        if (parent.children.isEmpty()) {
            item { Text("暂无回复", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(10.dp)) }
        } else {
            itemsIndexed(parent.children) { index, child ->
                Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(model = child.avatar, contentDescription = null, modifier = Modifier.size(16.dp).clip(CircleShape))
                        Spacer(Modifier.width(6.dp))
                        Text(child.userName, fontSize = 11.sp, color = Color(0xFF00C0FF))
                        if (child.isOwner) Text("楼主", color = Color.Black, fontSize = 8.sp, modifier = Modifier.padding(start=4.dp).background(Color(0xFFFFD700), RoundedCornerShape(2.dp)).padding(horizontal = 2.dp))
                        Spacer(Modifier.weight(1f))
                        Text("👍 ${child.likeCount}", fontSize = 10.sp, color = Color.Gray)
                    }

                    val childRich = remember(child.content) { RichTextHelper.buildSpannedString(child.content) }
                    Text(text = childRich, inlineContent = inlineContentMap, fontSize = 12.sp, color = Color.LightGray, modifier = Modifier.padding(start = 22.dp, top = 2.dp))

                    // 子回复图片
                    if (child.imgUrl != null) {
                        val originalUrl = child.imgUrl.split("?")[0]
                        AsyncImage(
                            model = child.imgUrl + "/thumbnail/300x",
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .padding(start = 22.dp, top = 4.dp)
                                .height(80.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .combinedClickable(
                                    onClick = { onImageClick(originalUrl) },
                                    onLongClick = { onImageLongClick(originalUrl) }
                                )
                        )
                    }

                    Text(text = "${child.time} · ${child.location}", fontSize = 9.sp, color = Color.DarkGray, modifier = Modifier.padding(start = 22.dp, top = 2.dp))

                    if (index < parent.children.lastIndex) {
                        Box(Modifier.padding(top=8.dp).fillMaxWidth().height(0.5.dp).background(Color.DarkGray.copy(alpha=0.5f)))
                    }
                }
            }
        }
    }
}
