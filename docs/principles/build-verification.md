# 自动构建与产物验证原则

## 统一质量门

本地提交前验证与 GitHub Actions 必须使用根应用模块的同一组任务：`testDebugUnitTest lintDebug assembleDebug assembleRelease`。任何任务失败都应阻止 APK artifact 上传，避免测试、静态检查与实际交付产物使用不同标准。

**理由**：仓库没有 `app` 子模块，拆分命令或使用 `:app:*` 会偏离实际项目结构，也可能让未经过完整验证的 APK 被误当成可交付产物。

**范例**：`.github/workflows/android-build.yml:25-68`；`settings.gradle.kts:1-17`；`build.gradle.kts:1-64`。

## 固定工具链

自动构建固定使用 Temurin JDK 17 和 Gradle 8.9；仓库没有 Gradle Wrapper，因此复现 CI 时也应显式使用这组工具链版本。工具链升级必须与 Android Gradle Plugin 兼容，并同时更新工作流、README 与代理约束。

**理由**：固定工具链可以减少本地与 CI 的解析、编译和打包差异，使失败结果可复现。

**范例**：`.github/workflows/android-build.yml:28-49`；`build.gradle.kts:1-4,41-48`；`README.md:41-68`。

## 预发布权限隔离

构建任务保持仓库内容只读；只有依赖成功构建且由 `main` 推送触发的发布任务可以取得仓库内容写权限，手动运行不发布。发布任务只能复用本次构建上传的 APK，生成 SHA-256 校验文件，并创建以运行号和提交号命名的 GitHub prerelease；同一次运行重试时必须验证已有标签和附件完全匹配，不覆盖已发布附件。

**理由**：把写权限与编译步骤隔离可以缩小供应链风险；唯一且可重跑的标签让每个预发布产物都能追溯到确定提交，同时不会占用正式语义化版本标签。

**范例**：`.github/workflows/android-build.yml:18-23,69-143`；`README.md:53-68`。

## 签名分离

自动构建及连续预发布只分发默认调试签名的 Debug APK 和 unsigned Release APK。发布签名必须在受控的正式发布流程中单独完成；不得把 keystore、密码或签名凭据写入仓库、工作流、普通构建 artifact 或连续预发布附件。

**理由**：持续集成负责验证与产出可检查的二进制，发布身份凭据不应进入日常源码构建边界。

**范例**：`.github/workflows/android-build.yml:18-19,31-35,51-68,69-143`；`build.gradle.kts:30-38`。
