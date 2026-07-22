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
import com.m16a4666.heywear.BuildConfig

@Composable
fun AboutScreen(onBack: () -> Unit) {
    ScalingLazyColumn(modifier = Modifier.fillMaxSize().background(Color(0xFF111111)), anchorType = ScalingLazyListAnchorType.ItemStart) {
        item { Text("HeyWear", color = Color(0xFF00C0FF), fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        item { Text("v${BuildConfig.VERSION_NAME}", color = Color.Gray, fontSize = 12.sp) }
        item { Spacer(Modifier.height(10.dp)) }
        item { Text("原作者", color = Color.White, fontSize = 12.sp) }
        item { Text("B站@m16a4666 / 黑盒@゚忍野志乃", color = Color.LightGray, fontSize = 11.sp) }
        item { Spacer(Modifier.height(6.dp)) }
        item { Text("当前维护", color = Color.White, fontSize = 12.sp) }
        item { Text("GitHub@shuangyue1124", color = Color.LightGray, fontSize = 11.sp) }
        item { Spacer(Modifier.height(10.dp)) }
        item {
            Text("第三方腕上小黑盒客户端。\n非官方应用，仅供学习交流。", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 10.dp))
        }
        item {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF333333)),
                modifier = Modifier.height(30.dp)
            ) {
                Text("返回", fontSize = 12.sp)
            }
        }
    }
}
