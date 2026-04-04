package com.m16a4666.heywear.interact

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Text

@Composable
fun CrashScreen(
    errorLog: String,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF440000))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "⚠️ 程序崩溃了",
            color = Color.Red,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            "请截图发给开发者",
            color = Color.Gray,
            fontSize = 10.sp
        )

        Spacer(modifier = Modifier.height(5.dp))

        // 堆栈信息区域
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Black.copy(0.5f))
                .verticalScroll(rememberScrollState())
                .padding(4.dp)
        ) {
            Text(
                text = errorLog,
                color = Color.Yellow,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray),
            modifier = Modifier.height(30.dp)
        ) {
            Text("忽略并重启", fontSize = 12.sp)
        }
    }
}