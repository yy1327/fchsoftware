# FCH Software - 园区监控 App

一款 Android 园区监控管理应用，支持多路摄像头实时预览、云台控制（PTZ）、截图录屏等功能。

## 功能特性

- **登录系统** - 用户名/密码登录，支持记住密码
- **摄像头监控** - 三列网格展示所有摄像头，支持按区域/状态分类筛选
- **实时预览** - 点击摄像头进入全屏预览
- **云台控制（PTZ）** - 方向盘控制摄像头上下左右旋转，支持缩放
- **底部工具栏** - 截图、录屏等快捷操作
- **搜索功能** - 快速搜索定位摄像头

## 技术栈

| 项目 | 技术 |
|------|------|
| 语言 | Java 11 |
| 最低版本 | Android 7.0 (API 24) |
| 目标版本 | Android 14 (API 36) |
| 架构 | Activity + Fragment |
| 图片加载 | Glide |
| UI 组件 | Material Design, RecyclerView, CardView |
| 布局 | ConstraintLayout |

## 项目结构

```
app/src/main/java/com/example/myapplication/
├── data/
│   ├── model/           # 数据模型 (Camera, TabFilter, BottomToolbarItem)
│   ├── mock/            # Mock 数据
│   └── repository/      # 数据仓库层
├── ui/
│   ├── login/           # 登录页面
│   ├── home/            # 首页（摄像头网格列表）
│   ├── camera/          # 摄像头预览页面
│   └── widget/          # 自定义控件 (PtzPadView)
└── util/                # 工具类 (PreferencesManager)
```

## 页面导航

```
LoginActivity ──▶ HomeActivity ──▶ CameraActivity
  登录页            首页/摄像头列表      摄像头预览/云台控制
```

## 如何使用

### 环境要求

- Android Studio Hedgehog 或更高版本
- JDK 11+
- Android SDK 36

### 构建与运行

1. 克隆仓库：
   ```bash
   git clone https://github.com/yy1327/fchsoftware.git
   ```

2. 使用 Android Studio 打开项目

3. 等待 Gradle 同步完成

4. 选择设备或模拟器，点击 **Run** 运行

### 测试账号

当前使用 Mock 数据，直接点击登录即可进入首页。

## License

MIT
