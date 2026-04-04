package com.m16a4666.heywear.interact

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Text

@Composable
fun AboutScreen(onBack: () -> Unit) {
    ScalingLazyColumn(modifier = Modifier.fillMaxSize().background(Color(0xFF111111)), anchorType = ScalingLazyListAnchorType.ItemStart) {
        item { Text("HeyWear", color = Color(0xFF00C0FF), fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        item { Text("Beta v0.3.0", color = Color.Gray, fontSize = 12.sp) }
        item { Spacer(Modifier.height(10.dp)) }
        item { Text("开发者:", color = Color.White, fontSize = 12.sp) }
        item { Text("黑盒@゚忍野志乃", color = Color.White, fontSize = 12.sp) }
        item { Text("B站@m16a4666", color = Color.White, fontSize = 12.sp) }
        item { Text("(其实都是一个人的说)", color = Color.LightGray, fontSize = 12.sp) }
        item { Text("api逆向:爱来自Gemini", color = Color.LightGray, fontSize = 12.sp) }
        item { Spacer(Modifier.height(10.dp)) }
        item {
            Text("第三方腕上小黑盒客户端。\n非官方应用，仅供学习交流。", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 10.dp))
        }
        item { Text("交流群:1072594702", color = Color.LightGray, fontSize = 10.sp) }
    }
}