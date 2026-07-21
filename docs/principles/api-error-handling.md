# API 状态、签名与降级原则

## 先验证状态，再读取业务对象

详情接口只有精确返回 `ok` 才能读取 `result.link`。`show_captcha`、空状态和其他状态都转换为拒绝结果；即使状态正常但 `link` 缺失，也必须进入可恢复分支。

**理由**：HTTP 200 不代表业务响应成功，验证码响应可能没有 `result.link`。

**范例**：`src/main/java/com/m16a4666/heywear/utils/HeyboxApiStatus.kt:3-22`；`src/main/java/com/m16a4666/heywear/interact/PostDetail.kt:160-210`。

## 优先保留已有可用内容

详情业务状态异常时使用传入的 `HeyPost` 构造展示内容，并向用户显示黄色警告；异常不能再通过空对象解引用转化成崩溃式错误。

**理由**：Feed 已提供足以继续基本阅读的标题、作者、摘要和图片。

**范例**：`src/main/java/com/m16a4666/heywear/interact/MainActivity.kt:269-293`；`src/main/java/com/m16a4666/heywear/interact/PostDetail.kt:167-210,330-340`。

## 签名实现必须有固定向量保护

请求统一使用秒级时间、随机 nonce 和规范化路径生成 hkey；修改字典、路径处理、交错或校验逻辑时，必须更新并解释固定捕获向量测试。

**理由**：占位签名会让上游返回异常业务结构，而编译器无法发现这类错误。

**范例**：`src/main/java/com/m16a4666/heywear/utils/HeyboxSigner.kt:6-82`；`src/test/java/com/m16a4666/heywear/utils/HeyboxSignerTest.kt:6-16`。

## 状态回归测试与最小日志

状态分类至少覆盖验证码拒绝和正常接受。详情降级日志只记录状态与帖子 ID，不记录响应正文或认证材料。

**理由**：测试要固定导致 `Link is NULL!` 的控制流前提，同时避免错误恢复路径扩大敏感信息暴露。

**范例**：`src/test/java/com/m16a4666/heywear/utils/HeyboxApiStatusTest.kt:7-26`；`src/main/java/com/m16a4666/heywear/interact/PostDetail.kt:181-185,205-209`。
