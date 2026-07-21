package com.m16a4666.heywear.interact

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Text
import coil.compose.AsyncImage
import com.m16a4666.heywear.utils.CookieUtil
import com.m16a4666.heywear.utils.DebugLogger
import com.m16a4666.heywear.utils.DeviceUtil
import com.m16a4666.heywear.utils.FileLogger
import com.m16a4666.heywear.utils.HeyboxHttpClient
import com.m16a4666.heywear.utils.HeyboxSigner
import com.m16a4666.heywear.utils.QrCodeUtil
import com.m16a4666.heywear.utils.requireHeyboxApiOk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.json.JSONObject

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current // 获取Context
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var statusText by remember { mutableStateOf("正在获取二维码...") }
    var isScanned by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                // 获取二维码
                val getQrPath = "/account/get_qrcode_url/"
                val deviceId = DeviceUtil.getDeviceId(context)
                val userAgent = DeviceUtil.getRandomUA()
                val baseParams = "os_type=web&app=web&client_type=web&version=999.0.4&web_version=2.5&x_client_type=web&x_app=heybox_website&heybox_id=&x_os_type=Windows&device_info=Edge&device_id=$deviceId"

                var time = HeyboxSigner.getTime()
                var nonce = HeyboxSigner.getNonce(time)
                var hkey = HeyboxSigner.getHkey(getQrPath, time, nonce)
                val getQrUrl = "https://api.xiaoheihe.cn$getQrPath?$baseParams&_time=$time&nonce=$nonce&hkey=$hkey&_notip=true"

                val qrResponse = HeyboxHttpClient.get(getQrUrl, userAgent)
                val root1 = JSONObject(qrResponse.body)
                requireHeyboxApiOk(root1.optString("status"), root1.optString("msg"))

                val resultObj = root1.optJSONObject("result")
                val rawQrContent = resultObj?.optString("qr_url") ?: ""
                if (rawQrContent.isEmpty()) throw Exception("No qr_url found")

                // 提取Key
                val uri = Uri.parse(rawQrContent)
                val loginKey = uri.getQueryParameter("qr")
                if (loginKey.isNullOrEmpty()) throw Exception("Key提取失败")

                // 生成图片
                val bitmap = QrCodeUtil.generateBitmap(rawQrContent, 300)
                qrBitmap = bitmap
                statusText = "请使用小黑盒App扫码"
                DebugLogger.log("Login", "QR ready")

                // 2. 轮询检查状态
                val checkPath = "/account/qr_state/"

                while (isActive) {
                    time = HeyboxSigner.getTime()
                    nonce = HeyboxSigner.getNonce(time)
                    hkey = HeyboxSigner.getHkey(checkPath, time, nonce)

                    val checkUrl = "https://api.xiaoheihe.cn$checkPath?$baseParams&qr=$loginKey&_time=$time&nonce=$nonce&hkey=$hkey"
                    val checkResponse = HeyboxHttpClient.get(checkUrl, userAgent)
                    val jsonCheck = checkResponse.body

                    // 记录轮询结果
                    if (!jsonCheck.contains("wait")) {
                        FileLogger.logNetwork(context, checkUrl, checkResponse.code)
                    }

                    // 检查Cookie(成功)
                    val cookies = CookieUtil.parseAndClean(checkResponse.setCookies)
                    if (cookies.contains("user_pkey")) {
                        FileLogger.write(context, "Login", "SUCCESS! Cookie obtained.")
                        DebugLogger.log("Login", "Success! Cookie Found.")
                        withContext(Dispatchers.Main) {
                            com.m16a4666.heywear.interact.MainActivity.GlobalCookie = cookies
                            onLoginSuccess()
                        }
                        break
                    }

                    // 检查状态
                    if (jsonCheck.contains("ready") || jsonCheck.contains("scanned") || jsonCheck.contains("\"status\":1")) {
                        if (!isScanned) {
                            FileLogger.write(context, "Login", "Scanned detected")
                            DebugLogger.log("Login", "Scanned detected")
                        }
                        statusText = "已扫码，请在手机确认"
                        isScanned = true
                    }

                    delay(2000)
                }

            } catch (e: Exception) {
                FileLogger.write(
                    context,
                    "LoginErr",
                    "${e.javaClass.simpleName}: ${e.message ?: "Unknown"}"
                )
                DebugLogger.log("LoginErr", e.message ?: "Unknown")
                statusText = "Err: ${e.message?.take(20)}"
                delay(5000)
            }
        }
    }

    // UI
    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black).padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (qrBitmap != null) {
            Box(contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = qrBitmap,
                    contentDescription = "二维码",
                    modifier = Modifier.size(110.dp).clip(RoundedCornerShape(8.dp))
                )
                if (isScanned) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(30.dp), strokeWidth = 3.dp)
                    }
                }
            }
        } else {
            CircularProgressIndicator(modifier = Modifier.size(30.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = statusText,
            color = Color.White,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onSkip,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF333333)),
            modifier = Modifier.height(32.dp).width(120.dp)
        ) {
            Text("暂不登录", fontSize = 11.sp)
        }
    }
}
