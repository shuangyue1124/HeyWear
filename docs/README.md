# 项目文档

本目录按用户功能、应用架构和代码原则组织。文档以仓库中的生产代码为唯一事实来源；功能文档中的 HTML 注释保存对应的文件与行号证据，不影响用户视角的正文。

## 维护规则

- **证据追溯**：每条行为或设计结论都要能定位到源码路径和行号；只能间接证明的内容标记为 `[INFERRED]`。
- **LLM 安全**：交给外部模型时优先提供文件列表、模块边界、函数签名和数据流摘要，不传输 Cookie、日志内容或完整私有数据。
- **增量更新**：代码变更后先用 `git diff` 确定受影响功能，只更新相关文档段落及证据行号。
- **漂移检查**：每月或重要发布前复核文档与代码；若 `apltk` 可用，先运行 `apltk codegraph --help`，再依据实时帮助选择索引命令。

## 索引

- `features/content-reading.md` — 内容浏览与详情降级。
- `features/account-and-settings.md` — 登录、个人内容和设置。
- `architecture/android-app.md` — Android 客户端模块边界与数据流。
- `principles/api-error-handling.md` — API 状态、签名和降级原则。
