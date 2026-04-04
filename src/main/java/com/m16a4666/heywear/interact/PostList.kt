package com.m16a4666.heywear.interact

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.CardDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Text
import coil.compose.AsyncImage
import com.m16a4666.heywear.model.HeyPost
import com.m16a4666.heywear.utils.SettingsUtil
import kotlinx.coroutines.launch

@Composable
fun PostListScreen(
    posts: List<HeyPost>,
    status: String,
    listState: ScalingLazyListState,
    onItemClick: (HeyPost) -> Unit,
    onLoadMore: () -> Unit
) {
    val config = LocalConfiguration.current
    val isRound = config.isScreenRound

    // 🔥 表冠支持核心代码
    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()

    // 自动请求焦点，否则旋钮没反应
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            // 🔥 监听旋钮事件
            .onRotaryScrollEvent {
                coroutineScope.launch {
                    // 圆屏滚动
                    if (isRound) {
                        listState.scrollBy(it.verticalScrollPixels)
                    }
                    // 方屏逻辑暂不处理(需要传LazyListState进来，这里主要优化圆表)
                }
                true // 事件已消费
            }
            .focusRequester(focusRequester)
            .focusable() // 必须可聚焦
    ) {
        if (isRound) {
            ScalingLazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                anchorType = ScalingLazyListAnchorType.ItemStart
            ) {
                itemsIndexed(posts) { index, post ->
                    if (index >= posts.size - 3) {
                        LaunchedEffect(Unit) { onLoadMore() }
                    }
                    PostCard(post, onItemClick)
                }

                if (status.isEmpty() && posts.isNotEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        }
                    }
                }
            }
        } else {
            val flatListState = rememberLazyListState()
            LazyColumn(
                state = flatListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp)
            ) {
                itemsIndexed(posts) { index, post ->
                    if (index >= posts.size - 3) {
                        LaunchedEffect(Unit) { onLoadMore() }
                    }
                    PostCard(post, onItemClick)
                }
            }
        }

        if (posts.isEmpty() && status.isNotEmpty()) {
            Text(
                text = status,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Center).padding(20.dp)
            )
        }
    }
}

@Composable
fun PostCard(post: HeyPost, onClick: (HeyPost) -> Unit) {
    val inlineContentMap = remember { RichTextHelper.getInlineContentMap() }
    val context = LocalContext.current
    val isNoImage = remember { SettingsUtil.isNoImageMode(context) }

    Card(
        onClick = { onClick(post) },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
        backgroundPainter = CardDefaults.cardBackgroundPainter(Color(0xFF222222))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (post.images.isNotEmpty() && !isNoImage) {
                val thumbUrl = if (post.images[0].contains("?")) post.images[0] + "/thumbnail/180x" else post.images[0] + "?imageMogr2/thumbnail/180x"
                AsyncImage(
                    model = thumbUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).padding(end = 10.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (post.isArticle) {
                        Text(
                            text = "文",
                            color = Color.Black,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(end = 4.dp).background(Color(0xFF00C0FF), RoundedCornerShape(2.dp)).padding(horizontal = 3.dp)
                        )
                    }

                    val titleText = RichTextHelper.buildSpannedString(post.title)
                    Text(
                        text = titleText,
                        inlineContent = inlineContentMap,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(post.author, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}