# Wear OS Android 客户端设计原则

## 单应用模块

项目是单个 Android application 模块，根构建脚本定义应用 ID、SDK、Compose 和依赖，`src/main` 提供唯一应用实现；`settings.gradle.kts` 没有声明其他子模块。

证据：`build.gradle.kts:1-86`、`settings.gradle.kts:1-16`、`src/main/AndroidManifest.xml:11-42`。

## UI 编排与支持能力分层

`interact` 负责 Compose 页面、导航、网络流程和展示状态；`model` 保存帖子、评论、用户与内容节点；`utils` 提供轻量 HTTP 边界、签名、Cookie、设备标识、偏好、日志和图片保存等跨页面能力。页面依赖模型与工具层，工具层不依赖页面。

证据：`src/main/java/com/m16a4666/heywear/interact/MainActivity.kt:56-293`、`src/main/java/com/m16a4666/heywear/model/HeyPost.kt:3-11`、`src/main/java/com/m16a4666/heywear/model/PostContent.kt:4-15`、`src/main/java/com/m16a4666/heywear/utils/HeyboxSigner.kt:6-82`、`src/main/java/com/m16a4666/heywear/utils/CookieUtil.kt:5-45`。

## 当前会话列表数据作为详情降级边界

首页把远端 Feed 映射为 `HeyPost` 并在导航时直接传入详情页。详情接口正常时用更完整的数据替换显示；状态异常、传输失败或缺少 `link` 时使用该内存对象中的标题、作者、摘要和图片。这个降级不写数据库、文件或持久化内容缓存，应用进程结束后自然释放。

证据：`src/main/java/com/m16a4666/heywear/interact/MainActivity.kt:137-166,204-243,248-293`、`src/main/java/com/m16a4666/heywear/interact/PostDetail.kt:137-211,309-327`。

## 轻量网络边界

标准小黑盒 API 请求统一经过基于平台 `HttpURLConnection` 的 `HeyboxHttpClient`。它设置 10 秒连接、15 秒读取超时，关闭 URLConnection 缓存和重定向，将单个响应限制在 2 MiB，只接受 HTTP 2xx，并在所有路径断开连接。客户端不自动重试，避免弱网时重复耗电、流量和占用线程。

证据：`src/main/java/com/m16a4666/heywear/utils/HeyboxHttpClient.kt:18-88`、`src/test/java/com/m16a4666/heywear/utils/HeyboxHttpClientTest.kt:15-77`。

## 低端手表资源边界

全局图片加载器禁用磁盘缓存，将内存缓存限制为可用内存的 5%，一次只执行一个 BitmapFactory 解码，并允许不透明图片使用 RGB565。设置页只允许用户主动释放当前会话的图片内存。主动保存到相册复用同一个加载器并把最长边限制为 1280 像素；编码或发布失败时删除未完成文件，避免大图内存峰值和残留占用。调试日志只在 Debug 构建写入，单条限制为 4000 字符且文件达到 128 KiB 时覆盖轮转。未使用的 Play Services Wearable 客户端库和扩展图标包不进入应用依赖图。

证据：`src/main/java/com/m16a4666/heywear/interact/MainActivity.kt:63-78`、`src/main/java/com/m16a4666/heywear/interact/SettingsScreen.kt:85-95`、`src/main/java/com/m16a4666/heywear/utils/ImageSaver.kt:22-140`、`src/main/java/com/m16a4666/heywear/utils/FileLogger.kt:10-53`、`build.gradle.kts:66-84`。

## 外部系统边界

应用通过 HTTPS 调用小黑盒接口，通过 SharedPreferences 只保存 Cookie、设备 ID、用户设置和最近一次崩溃诊断，并通过 Coil 在当前会话加载图片。用户主动保存图片时，Android 10 及以上使用 MediaStore；Android 9 及以下在运行时获得受限的写入权限后保存到公共 Pictures 目录。MediaStore 发布失败会回滚待处理条目。Manifest 声明手表硬件、联网和兼容旧系统所需权限。

证据：`src/main/java/com/m16a4666/heywear/interact/MainActivity.kt:248-263`、`src/main/java/com/m16a4666/heywear/utils/CookieUtil.kt:18-45`、`src/main/java/com/m16a4666/heywear/utils/DeviceUtil.kt:23-32`、`src/main/java/com/m16a4666/heywear/interact/ImageSaveAction.kt:21-50`、`src/main/java/com/m16a4666/heywear/utils/ImageSaver.kt:26-134`、`src/main/AndroidManifest.xml:4-30`。
