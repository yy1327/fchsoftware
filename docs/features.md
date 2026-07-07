# FCH Software - 功能特性文档

## 项目概述

园区监控管理 Android 应用，支持多路摄像头实时预览、云台控制、SIP 语音/视频通话等功能。

---

## 1. SIP 语音/视频通话

### 架构设计

三层架构：

| 层级 | 类 | 职责 |
|------|-----|------|
| 配置层 | `SipConfig` | SIP 服务器地址、端口、用户名、密码 |
| 引擎层 | `SipService` | SIP 注册、呼叫、接听、挂断核心逻辑 |
| 门面层 | `SipCallManager` | 简化接口，供 UI 层调用 |

### 通话流程

**发起呼叫：**
```
FragmentHome (输入号码) → VideoCallActivity.start(target)
  → SipCallManager.startCall(target)
  → SipService.makeCall(target)
  → SipManager.makeAudioCall()
```

**来电接听：**
```
SipService.callListener.onCallIncoming
  → SipCallback.onCallIncoming(caller)
  → IncomingCallActivity.start(callerNumber)
  → 用户点击接听 → SipCallManager.answerCall()
  → VideoCallActivity.start(caller)
```

### 通话状态管理

`CallState` 枚举：
- `IDLE` - 空闲
- `CALLING` - 呼叫中
- `RINGING` - 振铃中
- `CONNECTED` - 已连接
- `ENDED` - 已结束
- `FAILED` - 失败

### VideoCallActivity 功能

- 双 SurfaceView（远端/本地视频流）
- 通话时长计时器（MM:SS 格式）
- 静音切换（`MediaManager.setAudioEnabled()`）
- 视频开关（`MediaManager.setVideoEnabled()`）
- 免提按钮（待实现）
- Activity 销毁时自动挂断

### IncomingCallActivity 功能

- 系统铃声播放
- 30 秒超时自动拒绝
- 接听/拒绝按钮
- 锁屏显示（`showWhenLocked="true"`）
- 接听后跳转 VideoCallActivity

---

## 2. RTSP 摄像头监控

### 技术实现

使用 **ExoPlayer 2.19.1** + RTSP 扩展：

```java
RtspMediaSource mediaSource = new RtspMediaSource.Factory()
    .createMediaSource(MediaItem.fromUri(Uri.parse(rtspUrl)));
exoPlayer.setMediaSource(mediaSource);
exoPlayer.prepare();
```

### 控制功能

| 功能 | 状态 | 说明 |
|------|------|------|
| 缩放 | ✅ UI | 1x-20x 文本显示 |
| 云台控制 | ⚠️ 占位 | 方向按钮 + Toast 提示 |
| 截图 | ⚠️ 占位 | 底部工具栏按钮 |
| 录屏 | ⚠️ 占位 | 底部工具栏按钮 |
| 对讲 | ⚠️ 占位 | 底部工具栏按钮 |
| 分辨率 | ⚠️ 占位 | 底部工具栏按钮 |
| 预置点 | ⚠️ 占位 | 底部工具栏按钮 |

### 错误处理

- 缓冲中显示加载动画
- 播放错误显示占位图 + Toast 错误信息

---

## 3. 登录系统

### 流程

1. 运行时权限申请（录音、相机、通知）
2. 用户输入手机号 + 密码
3. 密码 MD5 加密传输
4. POST 请求 `/api/v1/app/login`
5. 成功后提取 authToken
6. 配置 SIP 账号（手机号作为 SIP 用户名）
7. 触发 SIP 注册
8. 启动前台服务保活
9. 跳转首页

### 密码管理

- "记住密码" 复选框（SharedPreferences 存储）
- 密码可见性切换（眼睛图标）
- 清除密码按钮

---

## 4. 首页摄像头列表

### 功能

- **三列网格** - 摄像头缩略图 + 在线/离线状态点
- **标签筛选** - 横向滚动标签（全部/行政办公楼/公共教学楼/1号主广场）
- **搜索** - 文本输入按名称过滤
- **筛选弹窗** - PopupWindow 地点筛选
- **悬浮通话按钮** - AlertDialog 输入 SIP 号码

### 数据来源

优先从 API 获取，失败时降级到 MockData（12 个硬编码摄像头）

---

## 5. 本地数据库（Room）

### 表结构

**user 表：**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 自增主键 |
| user_id | String | 用户ID（唯一） |
| user_name | String | 用户名 |
| phone | String | 手机号 |
| token | String | 认证令牌 |
| create_time | Date | 创建时间 |

**cameras 表：**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 自增主键 |
| camera_id | String | 摄像头ID |
| camera_name | String | 摄像头名称 |
| camera_photo2 | String | 缩略图URL |
| camera_code | String | 摄像头编码 |
| rtspUrl | String | RTSP地址 |

### DAO 能力

- 完整 CRUD 操作
- 分页查询
- 模糊搜索（名称/手机号）
- 批量同步（`syncAll()` 事务）
- Upsert 逻辑（`updateOrInsert()`）

> 注：当前版本主要从网络 API 获取数据，Room 数据库为离线缓存预留。

---

## 6. 网络层

### API 接口

| 方法 | 路径 | 参数 | 返回 |
|------|------|------|------|
| POST | `app/login` | phone, password | LoginResponse |
| POST | `camera/info2` | access-token, UserID, page, size | CameraListResponse |

### 配置

- Base URL: `http://223.100.6.179:18206/api/v1/`
- 超时: 30s（连接/读取/写入）
- HTTP Body 日志拦截器

---

## 7. 前台服务 / 保活

- 启动 SIP 注册成功后立即启动
- 持久通知："视频监控系统 - SIP 服务运行中 - 已注册/未注册"
- 点击通知跳转 HomeActivity
- 前台服务类型: `microphone`

---

## 8. 权限

| 权限 | 用途 |
|------|------|
| INTERNET | 网络访问 |
| ACCESS_NETWORK_STATE | 网络状态 |
| ACCESS_WIFI_STATE | WiFi 状态 |
| READ_PHONE_STATE | 手机状态 |
| RECORD_AUDIO | 录音（通话/对讲） |
| CAMERA | 相机（截图/录屏） |
| WAKE_LOCK | 保持唤醒 |
| FOREGROUND_SERVICE | 前台服务 |
| POST_NOTIFICATIONS | 通知（API 33+） |
| USE_FULL_SCREEN_INTENT | 来电全屏显示 |

---

## 第三方库

| 库 | 版本 | 用途 |
|----|------|------|
| ExoPlayer (core + RTSP) | 2.19.1 | RTSP 视频流播放 |
| Retrofit | 2.9.0 | REST API 客户端 |
| OkHttp | 4.12.0 | HTTP 引擎 |
| RxJava2 | 2.2.21 | 异步调用处理 |
| Glide | 4.16.0 | 图片加载 |
| Room | 2.6.1 | 本地数据库 |
| Material | 1.10.0 | UI 组件 |

---

## 待完善功能

1. PTZ 云台控制 - 需对接实际摄像头协议
2. 截图/录屏 - 需实现 MediaRecorder
3. 对讲功能 - 需实现双向音频流
4. 分辨率切换 - 需对接摄像头 API
5. 预置点管理 - 需持久化存储
6. 免提切换 - 需实现音频路由
7. Room 数据库 - 当前未接入业务流程
8. 密码安全 - SharedPreferences 明文存储需改进
