# 账号与设置

## 扫码登录或游客访问

- **Given** 本地没有有效登录 Cookie
- **When** 用户扫描手表二维码确认登录，或选择暂不登录
- **Then** 应用进入登录态主页或游客主页
<!-- evidence: src/main/java/com/m16a4666/heywear/interact/LoginScreen.kt:36-143,185-191; src/main/java/com/m16a4666/heywear/interact/MainActivity.kt:82-109 -->

## 查看个人内容

- **Given** 用户已经登录
- **When** 用户打开个人中心、收藏、历史或动态/投稿
- **Then** 应用展示对应资料和帖子列表，并可复用帖子详情页阅读内容
<!-- evidence: src/main/java/com/m16a4666/heywear/interact/PersonalCenter.kt:32-249; src/main/java/com/m16a4666/heywear/interact/UserContentScreen.kt:28-292 -->

## 调整手表阅读设置

- **Given** 用户进入设置页
- **When** 用户切换无图模式、顶部时间或滑动返回选项
- **Then** 应用持久化设置并在相应界面应用它们
<!-- evidence: src/main/java/com/m16a4666/heywear/interact/SettingsScreen.kt:23-147; src/main/java/com/m16a4666/heywear/utils/SettingsUtil.kt:5-44; src/main/java/com/m16a4666/heywear/interact/MainActivity.kt:121-125,169-191 -->

## 恢复崩溃信息

- **Given** 上次运行发生未捕获异常
- **When** 用户重新打开应用
- **Then** 应用先显示可查看和复制的错误信息，再允许返回正常流程
<!-- evidence: src/main/java/com/m16a4666/heywear/interact/MainActivity.kt:57-79; src/main/java/com/m16a4666/heywear/interact/CrashScreen.kt:20-74; src/main/java/com/m16a4666/heywear/utils/CrashHandler.kt:7-58 -->
