# FCH Software - 园区监控 App

一款 Android 园区监控管理应用，支持多路摄像头实时预览、云台控制（PTZ）、截图录屏等功能。

## 功能特性

- **登录系统** - 用户名/密码登录，支持记住密码，MD5加密传输
- **网络请求** - 使用 Retrofit + OkHttp + RxJava 对接服务器API
- **摄像头监控** - 三列网格展示所有摄像头，支持按区域/状态分类筛选
- **实时预览** - 点击摄像头进入全屏预览
- **云台控制（PTZ）** - 方向盘控制摄像头上下左右旋转，支持缩放
- **底部工具栏** - 截图、录屏、对讲等快捷操作
- **搜索功能** - 快速搜索定位摄像头
- **地点筛选** - 支持按地点筛选摄像头（行政办公楼、公共教学楼、1号主广场）
- **筛选菜单** - 点击筛选图标展开地点列表快速筛选
- **白色主题** - 监控页面采用白色UI设计
- **悬浮对讲按钮** - 底部工具栏对讲按钮悬浮突出效果

## 技术栈

| 项目 | 技术 |
|------|------|
| 语言 | Java 11 |
| 最低版本 | Android 7.0 (API 24) |
| 目标版本 | Android 14 (API 36) |
| 架构 | Activity + Fragment |
| 网络请求 | Retrofit 2.9.0 + OkHttp 4.12.0 + RxJava 2 |
| 图片加载 | Glide 4.16.0 |
| UI 组件 | Material Design, RecyclerView, CardView |
| 布局 | ConstraintLayout, LinearLayout |

## 项目结构

```
app/src/main/java/com/example/myapplication/
├── adapter/              # RecyclerView适配器 (VideoAdapter)
├── data/
│   ├── model/            # 数据模型 (Camera, Cameras, BaseResponse, LoginResponse, CameraListResponse)
│   ├── mock/             # Mock数据
│   └── repository/       # 数据仓库层
├── network/              # 网络请求层
│   ├── ApiService.java   # Retrofit接口定义
│   └── RetrofitClient.java # 网络客户端单例
├── ui/
│   ├── login/            # 登录页面
│   ├── home/             # 首页（摄像头网格列表）
│   ├── camera/           # 摄像头预览页面
│   ├── video/            # 视频列表页面
│   └── widget/           # 自定义控件 (PtzPadView)
└── util/                 # 工具类 (PreferencesManager)
```

## 页面导航

```
LoginActivity ──▶ HomeActivity ──▶ CameraActivity
  登录页            首页/摄像头列表      摄像头预览/云台控制
```

## API接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/app/login` | POST | 用户登录 |
| `/api/v1/camera/info2` | POST | 获取摄像头列表 |

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

- 手机号：`18500000009`
- 密码：`123456`（需MD5加密传输）

## License

MIT
