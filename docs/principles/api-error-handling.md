# API 状态、签名与降级原则

## 先验证状态，再读取业务对象

标准接口只有精确返回 `ok` 才能继续读取业务对象。`show_captcha`、空状态和其他状态都转换为异常；详情接口即使状态正常但 `link` 缺失，也必须进入可恢复分支。二维码轮询使用 `wait`、`ready`、`scanned` 等专用状态，因此只复用传输层，不套用标准业务状态门。

**理由**：HTTP 200 不代表业务响应成功，验证码响应可能没有 `result.link`。

**范例**：`src/main/java/com/m16a4666/heywear/utils/HeyboxApiStatus.kt:3-39`；`src/main/java/com/m16a4666/heywear/interact/PostDetail.kt:161-211`；`src/main/java/com/m16a4666/heywear/interact/LoginScreen.kt:80-112`。

## 优先保留当前会话已有内容

详情业务状态异常或网络/解析失败时使用导航传入的 `HeyPost` 构造展示内容，并向用户显示黄色警告；异常不能再通过空对象解引用转化成崩溃式错误。该对象只存在于当前进程内，不建立 Feed 或详情的持久化缓存。

**理由**：Feed 已提供足以继续基本阅读的标题、作者、摘要和图片。

**范例**：`src/main/java/com/m16a4666/heywear/interact/MainActivity.kt:137-166,204-243`；`src/main/java/com/m16a4666/heywear/interact/PostDetail.kt:168-211,309-345`。

## 签名实现必须有固定向量保护

请求统一使用秒级时间、随机 nonce 和规范化路径生成 hkey；修改字典、路径处理、交错或校验逻辑时，必须更新并解释固定捕获向量测试。

**理由**：占位签名会让上游返回异常业务结构，而编译器无法发现这类错误。

**范例**：`src/main/java/com/m16a4666/heywear/utils/HeyboxSigner.kt:6-82`；`src/test/java/com/m16a4666/heywear/utils/HeyboxSignerTest.kt:6-16`。

## 传输层必须有硬资源上限

API 客户端必须设置连接/读取超时、拒绝非 2xx、限制响应大小并可靠断开连接。不要自动重试，也不要为 API 响应增加数据库、文件缓存或新的重型网络框架。

**理由**：手表的内存、线程、流量和电池预算都很有限；无上限响应和隐式重试会放大上游异常。

**范例**：`src/main/java/com/m16a4666/heywear/utils/HeyboxHttpClient.kt:18-88`；`src/test/java/com/m16a4666/heywear/utils/HeyboxHttpClientTest.kt:15-77`。

## 状态回归测试与最小日志

状态分类至少覆盖验证码拒绝和正常接受。网络日志去掉完整查询串，只记录接口路径与 HTTP 状态；详情降级只记录业务状态/异常类型与帖子 ID。正式构建关闭调试日志，不记录响应正文、Cookie、登录 key 或签名材料；Debug 日志的单条内容和文件总量也必须有硬上限。

**理由**：测试要固定导致 `Link is NULL!` 的控制流前提，同时避免错误恢复路径扩大敏感信息暴露。

**范例**：`src/test/java/com/m16a4666/heywear/utils/HeyboxApiStatusTest.kt:7-39`；`src/test/java/com/m16a4666/heywear/utils/LoggingPrivacyTest.kt:7-39`；`src/main/java/com/m16a4666/heywear/utils/FileLogger.kt:10-64`；`src/main/java/com/m16a4666/heywear/utils/DebugLogger.kt:9-25`。
