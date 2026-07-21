# Wear OS Android 客户端设计原则

## 单应用模块

项目是单个 Android application 模块，根构建脚本定义应用 ID、SDK、Compose 和依赖，`src/main` 提供唯一应用实现；`settings.gradle.kts` 没有声明其他子模块。

证据：`build.gradle.kts:1-86`、`settings.gradle.kts:1-16`、`src/main/AndroidManifest.xml:9-42`。

## UI 编排与支持能力分层

`interact` 负责 Compose 页面、导航、网络流程和展示状态；`model` 保存帖子、评论、用户与内容节点；`utils` 提供签名、Cookie、设备标识、偏好、日志和图片保存等跨页面能力。页面依赖模型与工具层，工具层不依赖页面。

证据：`src/main/java/com/m16a4666/heywear/interact/MainActivity.kt:54-294`、`src/main/java/com/m16a4666/heywear/model/HeyPost.kt:3-11`、`src/main/java/com/m16a4666/heywear/model/PostContent.kt:4-15`、`src/main/java/com/m16a4666/heywear/utils/HeyboxSigner.kt:6-82`、`src/main/java/com/m16a4666/heywear/utils/CookieUtil.kt:5-51`。

## 列表数据作为详情降级边界

首页把远端 Feed 映射为完整的 `HeyPost` 并将该对象传入详情页。详情接口正常时用更完整的数据替换显示；状态异常或缺少 `link` 时保留列表对象中的标题、作者、摘要和图片，使第三方接口的验证码状态不会中断核心阅读流程。

证据：`src/main/java/com/m16a4666/heywear/interact/MainActivity.kt:238-293`、`src/main/java/com/m16a4666/heywear/interact/PostDetail.kt:138-210`。

## 外部系统边界

应用通过 HTTPS 调用小黑盒接口，通过 SharedPreferences 保存 Cookie、设备 ID 与用户设置，通过 Coil/MediaStore 读取和保存图片。Manifest 声明手表硬件、联网和兼容旧系统所需权限。

证据：`src/main/java/com/m16a4666/heywear/interact/MainActivity.kt:238-257`、`src/main/java/com/m16a4666/heywear/utils/CookieUtil.kt:22-49`、`src/main/java/com/m16a4666/heywear/utils/DeviceUtil.kt:23-32`、`src/main/java/com/m16a4666/heywear/utils/ImageSaver.kt:21-115`、`src/main/AndroidManifest.xml:4-30`。
