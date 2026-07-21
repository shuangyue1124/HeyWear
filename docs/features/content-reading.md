# 内容浏览与阅读

## 浏览首页

- **Given** 用户已登录或选择游客模式
- **When** 用户进入首页并滚动到列表末尾
- **Then** 应用展示文章和动态，并继续分页加载内容
<!-- evidence: src/main/java/com/m16a4666/heywear/interact/MainActivity.kt:82-109,133-156,238-294; src/main/java/com/m16a4666/heywear/interact/PostList.kt:43-126 -->

## 阅读帖子详情

- **Given** 首页已经展示一条帖子
- **When** 用户打开该帖子且详情接口返回正常内容
- **Then** 应用按文章图文或动态多图模式呈现详情，并允许进入评论区
<!-- evidence: src/main/java/com/m16a4666/heywear/interact/MainActivity.kt:200-233; src/main/java/com/m16a4666/heywear/interact/PostDetail.kt:213-307,361-426 -->

## 安全验证时继续阅读

- **Given** 首页已经保存帖子的标题、作者、摘要和图片
- **When** 详情接口要求安全验证或没有返回帖子对象
- **Then** 应用显示安全提示和首页缓存内容，不再显示 `Link is NULL!` 错误
<!-- evidence: src/main/java/com/m16a4666/heywear/interact/PostDetail.kt:160-210,330-340 -->

## 查看和保存图片

- **Given** 帖子或评论包含图片
- **When** 用户点按、缩放或长按图片
- **Then** 应用支持全屏查看、缩放，并在系统权限允许时保存图片
<!-- evidence: src/main/java/com/m16a4666/heywear/interact/PostDetail.kt:106-135,437-518; src/main/java/com/m16a4666/heywear/utils/ImageSaver.kt:21-122 -->

## 浏览评论

- **Given** 用户正在查看一条帖子
- **When** 用户打开评论区并继续滚动或选择子回复
- **Then** 应用分页展示评论，并允许查看楼中楼回复
<!-- evidence: src/main/java/com/m16a4666/heywear/interact/CommentScreen.kt:45-250,255-441 -->
