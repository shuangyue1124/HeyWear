package com.m16a4666.heywear.interact

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.Text
import com.m16a4666.heywear.R

@Composable
fun DonateScreen(onBack: () -> Unit) {
    var isWechat by remember { mutableStateOf(true) }
    ScalingLazyColumn(modifier = Modifier.fillMaxSize().background(Color.Black), anchorType = ScalingLazyListAnchorType.ItemStart) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("支持HeyWear开发", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("感谢您的支持!", color = Color.Gray, fontSize = 10.sp)
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.Center) {
                CompactChip(
                    onClick = { isWechat = true },
                    label = { Text("微信", fontSize = 10.sp) },
                    colors = if (isWechat) ChipDefaults.primaryChipColors(backgroundColor = Color(0xFF07C160)) else ChipDefaults.secondaryChipColors(),
                    modifier = Modifier.padding(end = 4.dp)
                )
                CompactChip(
                    onClick = { isWechat = false },
                    label = { Text("支付宝", fontSize = 10.sp) },
                    colors = if (!isWechat) ChipDefaults.primaryChipColors(backgroundColor = Color(0xFF1677FF)) else ChipDefaults.secondaryChipColors()
                )
            }
        }
        item {
            val qrRes = if (isWechat) R.drawable.qr_wechat else R.drawable.qr_alipay
            Box(modifier = Modifier.size(140.dp).clip(RoundedCornerShape(12.dp)).background(Color.White).padding(4.dp), contentAlignment = Alignment.Center) {
                Image(painter = painterResource(id = qrRes), contentDescription = "QRCode", modifier = Modifier.fillMaxSize())
            }
        }
        item { Text("截图或拍照后扫码", color = Color.DarkGray, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)) }
        item { Button(onClick = onBack, colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF333333)), modifier = Modifier.height(30.dp)) { Text("返回", fontSize = 12.sp) } }
    }
}