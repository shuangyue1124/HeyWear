# 内容浏览与阅读

## 浏览首页

- **Given** 用户已登录或选择游客模式
- **When** 用户进入首页并滚动到列表末尾
- **Then** 应用展示文章和动态，并继续分页加载内容
<!-- evidence: src/main/java/com/m16a4666/heywear/interact/MainActivity.kt:84-120,127-166,204-240,248-293; src/main/java/com/m16a4666/heywear/interact/PostList.kt:43-126 -->

## 阅读帖子详情

- **Given** 首页已经展示一条帖子
- **When** 用户打开该帖子且详情接口返回正常内容
- **Then** 应用按文章图文或动态多图模式呈现详情，并允许进入评论区
<!-- evidence: src/main/java/com/m16a4666/heywear/interact/MainActivity.kt:204-243; src/main/java/com/m16a4666/heywear/interact/PostDetail.kt:214-308,336-446 -->

## 安全验证时继续阅读

- **Given** 首页列表当前在内存中持有帖子的标题、作者、摘要和图片
- **When** 详情接口要求安全验证或没有返回帖子对象
- **Then** 应用显示安全提示和首页列表摘要，不再显示 `Link is NULL!`；降级内容随当前会话释放
<!-- evidence: src/main/java/com/m16a4666/heywear/interact/PostDetail.kt:161-211,309-345 -->

## 查看和保存图片

- **Given** 帖子或评论包含图片
- **When** 用户点按、缩放或长按图片
- **Then** 应用支持全屏查看、缩放，并在旧系统获得存储权限后保存最长边不超过 1280 像素的图片；写入失败时不保留未完成文件
<!-- evidence: src/main/java/com/m16a4666/heywear/interact/PostDetail.kt:99-127,442-493; src/main/java/com/m16a4666/heywear/interact/CommentScreen.kt:261-307,342-433; src/main/java/com/m16a4666/heywear/interact/ImageSaveAction.kt:21-50; src/main/java/com/m16a4666/heywear/utils/ImageSaver.kt:22-134 -->

## 浏览评论

- **Given** 用户正在查看一条帖子
- **When** 用户打开评论区并继续滚动或选择子回复
- **Then** 应用分页展示评论，并允许查看楼中楼回复
<!-- evidence: src/main/java/com/m16a4666/heywear/interact/CommentScreen.kt:47-258,261-445 -->
