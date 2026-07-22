# 账号与设置

## 扫码登录或游客访问

- **Given** 本地没有有效登录 Cookie
- **When** 用户扫描手表二维码确认登录，或选择暂不登录
- **Then** 应用进入登录态主页或游客主页
<!-- evidence: src/main/java/com/m16a4666/heywear/interact/LoginScreen.kt:37-180; src/main/java/com/m16a4666/heywear/interact/MainActivity.kt:84-120 -->

## 查看个人内容

- **Given** 用户已经登录
- **When** 用户打开个人中心、收藏、历史或动态/投稿
- **Then** 应用展示对应资料和帖子列表，并可复用帖子详情页阅读内容
<!-- evidence: src/main/java/com/m16a4666/heywear/interact/PersonalCenter.kt:35-249; src/main/java/com/m16a4666/heywear/interact/UserContentScreen.kt:27-277 -->

## 调整手表阅读设置

- **Given** 用户进入设置页
- **When** 用户切换无图模式、顶部时间或滑动返回选项
- **Then** 应用持久化设置并在相应界面应用它们
<!-- evidence: src/main/java/com/m16a4666/heywear/interact/SettingsScreen.kt:21-121; src/main/java/com/m16a4666/heywear/utils/SettingsUtil.kt:5-44; src/main/java/com/m16a4666/heywear/interact/MainActivity.kt:127-135,170-202,230-238 -->

## 释放当前会话图片内存

- **Given** 用户已经浏览过多张图片
- **When** 用户在设置页选择释放图片内存
- **Then** 应用释放当前会话占用的图片内存，手表存储空间不会新增图片缓存
<!-- evidence: src/main/java/com/m16a4666/heywear/interact/MainActivity.kt:63-78; src/main/java/com/m16a4666/heywear/interact/SettingsScreen.kt:85-95 -->

## 查看项目归属

- **Given** 用户进入关于页
- **When** 用户查看版本与开发信息
- **Then** 应用显示构建版本、原作者、当前维护者及非官方应用声明，不展示旧赞助入口或过期交流群
<!-- evidence: src/main/java/com/m16a4666/heywear/interact/AboutScreen.kt:19-41; src/main/java/com/m16a4666/heywear/interact/SettingsScreen.kt:97-104 -->

## 恢复崩溃信息

- **Given** 上次运行发生未捕获异常
- **When** 用户重新打开应用
- **Then** 应用先显示可查看的错误信息，再允许返回正常流程
<!-- evidence: src/main/java/com/m16a4666/heywear/interact/MainActivity.kt:59-90; src/main/java/com/m16a4666/heywear/interact/CrashScreen.kt:20-74; src/main/java/com/m16a4666/heywear/utils/CrashHandler.kt:7-58 -->
