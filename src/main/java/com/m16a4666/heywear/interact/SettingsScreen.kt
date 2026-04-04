package com.m16a4666.heywear.interact

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import androidx.wear.compose.material.*
import coil.imageLoader
import com.m16a4666.heywear.utils.CookieUtil
import com.m16a4666.heywear.utils.SettingsUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onOpenDonate: () -> Unit,
    onOpenAbout: () -> Unit,
    onSettingChanged: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isNoImage by remember { mutableStateOf(SettingsUtil.isNoImageMode(context)) }
    var isShowTime by remember { mutableStateOf(SettingsUtil.isShowTime(context)) }
    var isSwipeDisabled by remember { mutableStateOf(SettingsUtil.isSwipeDisabled(context)) }

    var cacheSizeText by remember { mutableStateOf("计算中...") }

    LaunchedEffect(Unit) {
        val sizeBytes = context.cacheDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
        cacheSizeText = "${sizeBytes / 1024 / 1024}MB"
    }

    ScalingLazyColumn(modifier = Modifier.fillMaxSize().background(Color(0xFF111111)), anchorType = ScalingLazyListAnchorType.ItemStart) {
        item { Text("设置", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 10.dp)) }

        // 无图模式
        item {
            ToggleChip(
                checked = isNoImage,
                onCheckedChange = {
                    isNoImage = it
                    SettingsUtil.setNoImageMode(context, it)
                    onSettingChanged()
                },
                label = { Text("无图模式") },
                secondaryLabel = { Text("仅加载文字 (省流)", fontSize = 10.sp) },
                toggleControl = { Switch(checked = isNoImage) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
            )
        }

        // 显示时间开关
        item {
            ToggleChip(
                checked = isShowTime,
                onCheckedChange = {
                    isShowTime = it
                    SettingsUtil.setShowTime(context, it)
                    onSettingChanged()
                },
                label = { Text("显示顶部时间") },
                secondaryLabel = { Text("跟随滚动自动隐藏", fontSize = 10.sp) },
                toggleControl = { Switch(checked = isShowTime) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
            )
        }

        // 禁用滑动返回
        item {
            ToggleChip(
                checked = isSwipeDisabled,
                onCheckedChange = {
                    isSwipeDisabled = it
                    SettingsUtil.setDisableSwipe(context, it)
                    onSettingChanged() // 通知主页刷新 SwipeBox 配置
                },
                label = { Text("禁用滑动返回") },
                secondaryLabel = { Text("开启后只能点按钮返回", fontSize = 10.sp) },
                toggleControl = { Switch(checked = isSwipeDisabled) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
            )
        }

        item {
            Chip(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        context.imageLoader.memoryCache?.clear()
                        context.imageLoader.diskCache?.clear()
                        context.cacheDir.deleteRecursively()
                        withContext(Dispatchers.Main) {
                            cacheSizeText = "0MB"
                            Toast.makeText(context, "缓存已清空", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                label = { Text("清空缓存") },
                secondaryLabel = { Text(cacheSizeText, fontSize = 10.sp) },
                colors = ChipDefaults.secondaryChipColors(),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        item {
            Chip(
                onClick = onOpenDonate,
                label = { Text("支持开发者", color = Color(0xFFFFD700)) },
                colors = ChipDefaults.secondaryChipColors(),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
        item {
            Chip(
                onClick = onOpenAbout,
                label = { Text("关于 HeyWear") },
                colors = ChipDefaults.secondaryChipColors(),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        item {
            Chip(
                onClick = {
                    CookieUtil.clear(context)
                    onLogout()
                },
                label = { Text("退出登录", color = Color(0xFFFF4D4F)) },
                colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF2A1215)),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 15.dp)
            )
        }
        item {
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF333333)), modifier = Modifier.height(30.dp)) { Text("返回", fontSize = 12.sp) }
        }
    }
}