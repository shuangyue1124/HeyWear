# 自动构建与产物验证原则

## 统一质量门

本地提交前验证与 GitHub Actions 必须使用根应用模块的同一组任务：`testDebugUnitTest lintDebug assembleDebug assembleRelease`。任何任务失败都应阻止 APK artifact 上传，避免测试、静态检查与实际交付产物使用不同标准。

**理由**：仓库没有 `app` 子模块，拆分命令或使用 `:app:*` 会偏离实际项目结构，也可能让未经过完整验证的 APK 被误当成可交付产物。

**范例**：`.github/workflows/android-build.yml:25-67`；`settings.gradle.kts:1-17`；`build.gradle.kts:1-64`。

## 固定工具链

自动构建固定使用 Temurin JDK 17 和 Gradle 8.9；仓库没有 Gradle Wrapper，因此复现 CI 时也应显式使用这组工具链版本。工具链升级必须与 Android Gradle Plugin 兼容，并同时更新工作流、README 与代理约束。

**理由**：固定工具链可以减少本地与 CI 的解析、编译和打包差异，使失败结果可复现。

**范例**：`.github/workflows/android-build.yml:28-49`；`build.gradle.kts:1-4,41-48`；`README.md:41-64`。

## 签名分离

自动构建只上传默认调试签名的 Debug APK 和 unsigned Release APK。发布签名必须在受控的发布流程中单独完成；不得把 keystore、密码或签名凭据写入仓库、工作流或普通构建 artifact。

**理由**：持续集成负责验证与产出可检查的二进制，发布身份凭据不应进入日常源码构建边界。

**范例**：`.github/workflows/android-build.yml:18-19,31-35,51-67`；`build.gradle.kts:30-38`。
