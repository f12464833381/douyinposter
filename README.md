\# DouyinPoster - 抖音投稿器 

这是一个基于 Kotlin 和 Android studio开发的抖音风格内容发布器应用。它模拟了抖音（或其他短视频平台）中用户发布作品时的关键功能，包括图片选择、封面设置、多图管理、文字描述、话题标签、@用户以及地理位置信息获取。 ## 功能概览 -   

**多图选择与预览**: 支持从相册选择多张图片，并在发布页面进行主图预览。 

- **缩略图管理**: 底部缩略图列表支持：   

- - **点击切换**: 点击缩略图切换主图预览。    -   

  - **拖拽排序**: 长按并拖拽缩略图以调整图片顺序。    -   

  - **删除操作**: 点击缩略图上的删除按钮移除图片。    -   

  - **添加更多**: 快速添加更多图片。 -   

  - **封面编辑**: 仅当第一张图片为主图时，显示“编辑封面”按钮（目前为占位功能）。 -   

  - **文字内容编辑**:    -   

  - **标题输入**: 支持标题输入，并有 `20` 字数上限提示。    -   

  - **描述输入**: 支持作品描述输入，并有 `3000` 字数上限提示。    -   

  - **实时字数统计**: 标题和描述输入框下方实时显示当前字数/总字数。 -   

  - **互动功能**:    -   

  - **话题标签**: 点击“# 话题”按钮，进入话题选择页面，选择后插入描述。    -   

  - **@用户**: 点击“@ 朋友”按钮，进入用户选择页面，选择后插入描述（显示昵称，插入 `@username`）。    -   

  - **蓝色高亮**: 描述框中的 `#话题` 和 `@username` 会自动显示为蓝色。 -   

  - **地理位置**:    -   

  - **定位获取**: 点击“你在哪里”获取当前地理位置（经纬度）。    -   

  - **反向地理编码**: 将经纬度转换为可读的城市/地点名称并显示。    -   

  - **权限处理**: 运行时动态请求定位权限。 ## 🛠 技术栈 -  

  -  **开发语言**: Kotlin -   

  - **UI 框架**: Android Jetpack (ConstraintLayout, RecyclerView, NestedScrollView) -   

  - **图片加载**: [Glide](https://github.com/bumptech/glide) -   

  - **异步操作**: Kotlin Coroutines (用于地理编码、模拟延迟) -   

  - **位置服务**: [Google Play Services Location](https://developers.google.com/location-and-context/fused-location-provider) (FusedLocationProviderClient) -   

  - **依赖管理**: Gradle Kotlin DSL ## 项目结构

 MyKotlinApps
├── app/
│ ├── src/
│ │ ├── main/
│ │ │ ├── java/com/fengjiandong/douyinposter/
│ │ │ │ ├── adapter/ ─────── # RecyclerView 适配器
│ │ │ │ │ ├── SelectionAdapter.kt # 通用列表选择适配器（话题/用户）
│ │ │ │ │ └── ThumbnailAdapter.kt # 底部缩略图列表适配器（多类型Item、拖拽、删除）
│ │ │ │ ├── data/ ────────── # 数据模型和模拟数据
│ │ │ │ │ ├── MockData.kt # 模拟数据源（话题、用户列表）
│ │ │ │ │ └── User.kt # 用户数据类
│ │ │ │ ├── MainActivity.kt ───── # 主入口，负责图片选择并跳转
│ │ │ │ ├── PostActivity.kt ───── # 核心发布页面，集成所有功能
│ │ │ │ ├── TopicSelectionActivity.kt # 话题选择页面
│ │ │ │ └── UserSelectionActivity.kt # 用户选择页面
│ │ │ ├── res/ ────────────── # 资源文件
│ │ │ │ ├── drawable/ ───── # 可绘制资源（图标、形状）
│ │ │ │ ├── layout/ ─────── # 布局文件（activity_post.xml 是核心复杂布局）
│ │ │ │ ├── mipmap/ ─────── # 应用启动图标
│ │ │ │ ├── values/ ─────── # 字符串、颜色、主题等
│ │ │ │ └── xml/ ────────── # 其他 XML 配置（如 FileProvider）
│ │ │ └── AndroidManifest.xml # 应用清单文件
│ ├── build.gradle.kts (Module: app) # 模块级 Gradle 配置
│ └── ...
└── build.gradle.kts (Project: DouyinPoster) # 项目级 Gradle 配置
