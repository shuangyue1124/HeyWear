<p align="center">腕上黑盒 (HeyWear)</p>

![alt text](https://img.shields.io/badge/Wear_OS-3.0%2B-blue)
![alt text](https://img.shields.io/badge/Kotlin-2.0-purple)
![alt text](https://img.shields.io/badge/License-MIT-green)

本仓库基于 bilibili_m16a4666 原版 HeyWear（MIT License）持续维护，当前由 shuangyue1124 维护。
它是面向 Wear OS 的第三方小黑盒客户端，优先控制 APK 体积、内存占用、磁盘写入和后台工作量。

实现了哪些功能？
**1. 浏览与阅读**
* **刷首页**：支持瀑布流加载，能分别显示“文章”和“动态”
* **看帖子**：文章详情页支持图文混排（HTML 解析）；动态支持多图轮播显示
* **看图片**：内置了全屏图片查看器，支持双击放大/双指缩放，长按还能把图片直接保存到手表本地相册

**2. 账号与个人中心**
* **扫码登录**：手表端生成二维码，用手机小黑盒扫码确认即可登录
* **个人数据**：可以查看自己的头像、粉丝数等基本信息
* **内容管理**：支持查看我的“历史浏览记录”、“我的收藏”以及“我的动态”

**3. 设置与调试功能**
* **无图模式**：开启后不加载图片，节省手表流量
* **基础设置**：支持释放当前会话的图片内存、显示系统时间开关、一键退出登录
* **防风控伪装**：内置了随机生成设备 Device-ID 和 User-Agent 的逻辑
* **崩溃拦截**：遇到 Bug 闪退时会有“红屏报错”提示，并保留最近一次崩溃信息，方便排查问题
* **低占用策略**：不持久化 Feed/详情内容，不建立磁盘图片缓存；图片仅使用受限的当前会话内存缓存，主动保存时最长边限制为 1280 像素，调试日志上限为 128 KiB

📂 项目结构

<img width="1393" height="1345" alt="image" src="https://github.com/user-attachments/assets/01832428-f227-426e-a7d1-008736b2652b" />


🚨 关于接口可用性

源码已包含 `HeyboxSigner.kt` 的请求签名实现，并使用回归向量防止算法被误改。
小黑盒接口仍可能根据网络环境返回 `show_captcha`。Wear OS 端无法可靠完成网页验证码时，
帖子详情会显示首页当前已在内存中持有的标题、摘要和图片，并给出安全验证提示，不再报 `Link is NULL!`，也不会为此建立持久化内容缓存。



环境要求：Android Studio Koala (或更新版本)，JDK 17+。

SDK 版本：compileSdk 34，minSdk 25 (兼容 TicWatch Pro 3 等老设备至 Pixel Watch 3)。

仓库当前不包含 Gradle Wrapper。使用已安装的 Gradle 可运行：

```powershell
gradle --no-daemon testDebugUnitTest assembleDebug
```

发布构建可使用 `gradle --no-daemon assembleRelease`，产物默认未签名。

### GitHub Actions 自动构建与预发布

向 `main` 分支推送 `src/**`、Gradle/ProGuard 配置或工作流本身的变更时，或者在 GitHub Actions 页面手动运行 `Android Build` 时，仓库会使用 JDK 17 和 Gradle 8.9 执行完整质量门：

```text
testDebugUnitTest lintDebug assembleDebug assembleRelease
```

构建成功后会上传两个保留 14 天的 artifact：

- `HeyWear-debug-<commit SHA>`：使用 Android 默认调试签名的 Debug APK。
- `HeyWear-release-unsigned-<commit SHA>`：经过 Release 混淆与资源压缩、但未签名的 APK。

`main` 分支推送触发的构建成功后，还会自动创建长期保留的 `build-<运行号>-<短提交号>` GitHub prerelease，附带以上两个 APK 和 `SHA256SUMS.txt`；手动运行只生成 14 天 artifact，不发布 Release。Debug APK 可以直接安装但体积较大，且 CI 临时调试签名可能导致不同构建之间无法覆盖安装；unsigned Release APK 体积较小，但必须使用项目专用签名后才能安装。自动构建不会接触或保存发布签名密钥。

预发布产物可在 [GitHub Releases](https://github.com/shuangyue1124/HeyWear/releases) 下载。



📜 免责声明
本项目仅供编程学习与技术交流使用
App 内展示的所有游戏资讯、帖子、图片、评论等数据版权均归 清枫（北京）科技有限公司 (小黑盒) 所有
开发者不对因使用本软件导致的任何账号异常、数据泄露等问题负责

项目沿用原版 HeyWear 的设计与实现，感谢原作者 bilibili_m16a4666 和 HeyWear 社区的贡献。
当前 fork 由 shuangyue1124 继续维护。


<img width="1345" height="1569" alt="image" src="https://github.com/user-attachments/assets/c2b0e475-6fa8-4fdf-a609-99475311ab5c" />
<img width="1335" height="1555" alt="image" src="https://github.com/user-attachments/assets/6d0246d5-c258-41f7-b237-4942a3d38e41" />
<img width="1341" height="1583" alt="image" src="https://github.com/user-attachments/assets/88737cbe-f8e6-468a-965a-bc6fb8c1b274" />
