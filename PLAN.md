# Surveillance App Implementation Plan

## 1. Architecture Overview

```
com.example.myapplication/
├── data/
│   ├── model/           # POJOs / data classes
│   ├── mock/            # Mock data providers
│   └── repository/      # Repository layer (abstraction for data access)
├── ui/
│   ├── login/           # LoginActivity + fragment_login.xml
│   ├── home/            # HomeActivity + FragmentHome + adapters
│   ├── camera/          # CameraActivity + FragmentCameraPreview + adapters
│   ├── widget/          # Custom views (PtzPadView, CameraGridItemDecoration)
│   └── adapter/         # Shared adapters (TabFilterAdapter, BottomToolbarAdapter)
├── util/                # SharedPreferencesHelper, ViewUtils
└── MyApplication.java   # Application class
```

**Activity/Fragment navigation:**
```
LoginActivity  ──Intent──▶  HomeActivity (hosts FragmentHome via FrameLayout)
                                   │
                                   └── click camera ──▶  CameraActivity (hosts FragmentCameraPreview via FrameLayout)
```

All three screens use **Activity + single Fragment** pattern for consistency and future flexibility.

---

## 2. Complete File List

### New Java Files (16 files)

| # | File Path | Purpose |
|---|-----------|---------|
| 1 | `app/src/main/java/com/example/myapplication/MyApplication.java` | Application class; init global prefs |
| 2 | `app/src/main/java/com/example/myapplication/ui/login/LoginActivity.java` | Login screen host |
| 3 | `app/src/main/java/com/example/myapplication/ui/login/FragmentLogin.java` | Login UI logic: input validation, remember-me, navigation |
| 4 | `app/src/main/java/com/example/myapplication/ui/home/HomeActivity.java` | Home screen host with bottom nav / frame |
| 5 | `app/src/main/java/com/example/myapplication/ui/home/FragmentHome.java` | Camera grid, tab filters, search |
| 6 | `app/src/main/java/com/example/myapplication/ui/home/CameraThumbnailAdapter.java` | RecyclerView adapter for 3-col camera grid |
| 7 | `app/src/main/java/com/example/myapplication/ui/home/TabFilterAdapter.java` | RecyclerView horizontal tab adapter |
| 8 | `app/src/main/java/com/example/myapplication/ui/camera/CameraActivity.java` | Camera preview screen host |
| 9 | `app/src/main/java/com/example/myapplication/ui/camera/FragmentCameraPreview.java` | Camera preview, PTZ, tabs, toolbar |
| 10 | `app/src/main/java/com/example/myapplication/ui/camera/BottomToolbarAdapter.java` | RecyclerView adapter for bottom toolbar icons |
| 11 | `app/src/main/java/com/example/myapplication/ui/widget/PtzPadView.java` | Custom PTZ directional pad view |
| 12 | `app/src/main/java/com/example/myapplication/data/model/Camera.java` | Camera POJO |
| 13 | `app/src/main/java/com/example/myapplication/data/model/TabFilter.java` | Tab filter POJO |
| 14 | `app/src/main/java/com/example/myapplication/data/model/BottomToolbarItem.java` | Toolbar item POJO |
| 15 | `app/src/main/java/com/example/myapplication/data/mock/MockData.java` | Central mock data provider |
| 16 | `app/src/main/java/com/example/myapplication/util/PreferencesManager.java` | SharedPreferences wrapper (remember password) |

### New Layout XML Files (9 files)

| # | File Path | Purpose |
|---|-----------|---------|
| 1 | `app/src/main/res/layout/activity_login.xml` | LoginActivity container |
| 2 | `app/src/main/res/layout/fragment_login.xml` | Login form with all inputs |
| 3 | `app/src/main/res/layout/activity_home.xml` | HomeActivity container |
| 4 | `app/src/main/res/layout/fragment_home.xml` | Search bar + tabs + camera grid |
| 5 | `app/src/main/res/layout/item_camera_thumbnail.xml` | Single camera grid item |
| 6 | `app/src/main/res/layout/item_tab_filter.xml` | Single horizontal tab chip |
| 7 | `app/src/main/res/layout/activity_camera.xml` | CameraActivity container |
| 8 | `app/src/main/res/layout/fragment_camera_preview.xml` | Full camera preview layout |
| 9 | `app/src/main/res/layout/item_bottom_toolbar.xml` | Single bottom toolbar icon+label |

### New Drawable XML Files (12 files)

| # | File Path | Purpose |
|---|-----------|---------|
| 1 | `res/drawable/bg_login.xml` | Login screen gradient/solid background |
| 2 | `res/drawable/bg_edit_text_rounded.xml` | Rounded input field background |
| 3 | `res/drawable/bg_button_blue.xml` | Primary blue button background |
| 4 | `res/drawable/bg_button_blue_disabled.xml` | Disabled button state |
| 5 | `res/drawable/bg_camera_thumbnail.xml` | Camera card rounded corners |
| 6 | `res/drawable/bg_play_icon_circle.xml` | Semi-transparent play button overlay |
| 7 | `res/drawable/bg_tab_selected.xml` | Selected tab chip background |
| 8 | `res/drawable/bg_tab_unselected.xml` | Unselected tab chip background |
| 9 | `res/drawable/bg_ptz_pad.xml` | PTZ pad circular background |
| 10 | `res/drawable/bg_toolbar_item.xml` | Bottom toolbar item background |
| 11 | `res/drawable/ic_visibility_on.xml` | Eye-open vector icon |
| 12 | `res/drawable/ic_visibility_off.xml` | Eye-closed vector icon |

### New Drawable Vector Icons (10 files)

| # | File Path | Purpose |
|---|-----------|---------|
| 1 | `res/drawable/ic_play.xml` | Play triangle overlay |
| 2 | `res/drawable/ic_search.xml` | Search magnifying glass |
| 3 | `res/drawable/ic_ptz_up.xml` | PTZ arrow up |
| 4 | `res/drawable/ic_ptz_down.xml` | PTZ arrow down |
| 5 | `res/drawable/ic_ptz_left.xml` | PTZ arrow left |
| 6 | `res/drawable/ic_ptz_right.xml` | PTZ arrow right |
| 7 | `res/drawable/ic_zoom_in.xml` | Zoom plus |
| 8 | `res/drawable/ic_zoom_out.xml` | Zoom minus |
| 9 | `res/drawable/ic_screenshot.xml` | Screenshot toolbar icon |
| 10 | `res/drawable/ic_record.xml` | Record toolbar icon |

### Existing Files to Modify (6 files)

| # | File Path | Changes |
|---|-----------|---------|
| 1 | `app/build.gradle` | Add RecyclerView, ViewPager2, CardView, Glide dependencies |
| 2 | `gradle/libs.versions.toml` | Add version entries for new deps |
| 3 | `app/src/main/AndroidManifest.xml` | Register new Activities, set LoginActivity as launcher |
| 4 | `app/src/main/res/values/colors.xml` | Add full color scheme |
| 5 | `app/src/main/res/values/themes.xml` | Add app theme with colors |
| 6 | `app/src/main/res/values/strings.xml` | Add all string resources |

---

## 3. Data Models

```java
// Camera.java
public class Camera {
    private String id;
    private String name;
    private String location;        // e.g. "行政办公楼"
    private int thumbnailResId;     // placeholder drawable res
    private boolean isOnline;
}

// TabFilter.java
public class TabFilter {
    private String label;           // e.g. "全部"
    private boolean isSelected;
}

// BottomToolbarItem.java
public class BottomToolbarItem {
    private String label;           // e.g. "截图"
    private int iconResId;
    private boolean isActive;       // for toggle states like recording
}
```

---

## 4. Color Scheme (from PSD)

```xml
<color name="primary_blue">#FF2B6CB0</color>       <!-- buttons, active tabs -->
<color name="primary_dark">#FF1A365D</color>        <!-- status bar, header bg -->
<color name="background_dark">#FF0F1724</color>     <!-- camera preview bg -->
<color name="background_light">#FFF1F5F9</color>    <!-- login bg, card bg -->
<color name="text_primary">#FF1E293B</color>        <!-- dark text -->
<color name="text_secondary">#FF64748B</color>      <!-- placeholder text -->
<color name="text_white">#FFFFFFFF</color>          <!-- on dark bg -->
<color name="text_hint">#FF94A3B8</color>           <!-- input hints -->
<color name="tab_selected_bg">#FF2B6CB0</color>
<color name="tab_selected_text">#FFFFFFFF</color>
<color name="tab_unselected_bg">#FFE2E8F0</color>
<color name="tab_unselected_text">#FF475569</color>
<color name="card_bg">#FFFFFFFF</color>
<color name="divider">#FFE2E8F0</color>
<color name="play_overlay">#80000000</color>        <!-- 50% black -->
<color name="ptz_pad_bg">#FF1E293B</color>
<color name="toolbar_bg">#FF1A202C</color>
<color name="toolbar_active">#FF2B6CB0</color>
<color name="status_online">#FF22C55E</color>
<color name="status_offline">#FFEF4444</color>
<color name="error_red">#FFEF4444</color>
```

---

## 5. Dependencies to Add

### libs.versions.toml additions:
```toml
recyclerview = "1.3.2"
viewpager2 = "1.0.0"
cardview = "1.0.0"
glide = "4.16.0"
```

### build.gradle dependencies block additions:
```groovy
implementation libs.recyclerview
implementation libs.viewpager2
implementation libs.cardview
implementation libs.glide
```

---

## 6. Screen Implementation Details

### Screen 1: Login (`LoginActivity` + `FragmentLogin`)

**Layout structure (`fragment_login.xml`):**
```
ConstraintLayout (match_parent)
├── ImageView (match_parent, scaleType=centerCrop) — background image
├── ConstraintLayout (content area, padded)
│   ├── TextView — "监控系统" title, large white, centered top
│   ├── LinearLayout (vertical, rounded bg)
│   │   ├── LinearLayout (horizontal) — phone input row
│   │   │   ├── ImageView — phone icon
│   │   │   └── EditText — hint "请输入手机号", inputType phone
│   │   ├── View — divider line
│   │   └── LinearLayout (horizontal) — password input row
│   │       ├── ImageView — lock icon
│   │       ├── EditText — hint "请输入密码", inputType textPassword
│   │       ├── ImageView — visibility toggle (eye icon)
│   │       └── TextView — "清除" text button
│   ├── LinearLayout (horizontal) — remember row
│   │   ├── CheckBox — "记住密码"
│   │   └── TextView — "忘记密码?" (clickable)
│   └── Button — "登 录", full width, blue rounded bg
```

**Logic:**
- `FragmentLogin.onCreateView()` inflates layout, `onViewCreated()` wires click listeners
- Phone input: `InputType.TYPE_CLASS_PHONE`, maxLength 11
- Password: starts as `TYPE_TEXT_VARIATION_PASSWORD`; toggle icon switches between `TYPE_TEXT_VARIATION_PASSWORD` and `TYPE_TEXT_VARIATION_VISIBLE_PASSWORD`
- "清除" clears password field
- Remember password: checked state saved/restored via `PreferencesManager`
- Login button: validates non-empty fields, on click `startActivity(Intent(this, HomeActivity.class))` + `finish()`
- Background: use `ic_launcher_foreground` as placeholder, or a gradient drawable

### Screen 2: Home/Camera List (`HomeActivity` + `FragmentHome`)

**Layout structure (`fragment_home.xml`):**
```
LinearLayout (vertical, match_parent, bg=#F1F5F9)
├── Toolbar/LinearLayout (bg=primary_dark)
│   ├── TextView — "我的监控" title, white
│   └── ImageView — settings icon (optional)
├── LinearLayout (horizontal, padded)
│   ├── ImageView — search icon
│   └── EditText — hint "搜索摄像头", bg rounded
├── RecyclerView (horizontal) — tab filters
│   └── item_tab_filter.xml: TextView with bg selector
├── RecyclerView (grid, spanCount=3) — camera thumbnails
│   └── item_camera_thumbnail.xml:
│       └── CardView
│           ├── FrameLayout
│           │   ├── ImageView — thumbnail placeholder
│           │   └── ImageView — play icon (centered, circular semi-transparent bg)
│           └── LinearLayout (horizontal)
│               ├── View — online status dot (8dp circle)
│               └── TextView — camera name (single line, ellipsize)
```

**Logic:**
- Tab filters: horizontal RecyclerView with `LinearLayoutManager(HORIZONTAL)`. Default "全部" selected.
- Camera grid: `RecyclerView` with `GridLayoutManager(context, 3)`. 
- Tab click: updates selected state, filters camera list by location, notifies adapter.
- Search: `TextWatcher` on search EditText filters cameras by name (case-insensitive).
- Click on camera item: `startActivity(Intent to CameraActivity with camera ID extra)`.
- Mock data: `MockData.getCameras()` returns list of 9+ cameras across the 3 locations + "公共教学楼" + "1号主广场".

**TabFilterAdapter:**
- ViewHolder wraps a single `TextView`
- Binds `TabFilter` object, sets background to selected/unselected drawable
- Click listener: callback interface `OnTabSelectedListener.onTabSelected(int position)`

**CameraThumbnailAdapter:**
- ViewHolder: `CardView` containing thumbnail `ImageView`, play overlay `ImageView`, status dot `View`, name `TextView`
- Binds `Camera` object: sets name text, placeholder thumbnail, status dot color
- Click listener: callback interface `OnCameraClickListener.onCameraClick(Camera camera)`

### Screen 3: Camera Preview (`CameraActivity` + `FragmentCameraPreview`)

**Layout structure (`fragment_camera_preview.xml`):**
```
LinearLayout (vertical, match_parent, bg=background_dark)
├── LinearLayout (top bar)
│   ├── ImageView — back arrow
│   ├── TextView — camera name
│   └── ImageView — fullscreen toggle
├── FrameLayout (video preview area, weight=1)
│   ├── ImageView — placeholder "视频预览" text centered
│   └── (Future: TextureView/SurfaceView for actual video)
├── LinearLayout (horizontal) — sub-tabs
│   ├── TextView — "实时" (selected)
│   ├── TextView — "录像"
│   ├── TextView — "消息"
│   └── TextView — "场景"
├── FrameLayout or ConstraintLayout — PTZ + Zoom controls
│   ├── PtzPadView (custom view, centered)
│   │   ├── Up arrow (ImageView, top-center)
│   │   ├── Down arrow (ImageView, bottom-center)
│   │   ├── Left arrow (ImageView, center-left)
│   │   └── Right arrow (ImageView, center-right)
│   │   └── Center circle (neutral)
│   ├── LinearLayout (vertical) — zoom controls (right side)
│   │   ├── ImageButton — zoom in (+)
│   │   ├── SeekBar or TextView — zoom level
│   │   └── ImageButton — zoom out (-)
├── View — divider
└── RecyclerView (horizontal) — bottom toolbar
    └── item_bottom_toolbar.xml:
        └── LinearLayout (vertical, centered)
            ├── ImageView — icon (24dp, white/active blue)
            └── TextView — label (small, white)
    Items: 截图 | 录制 | 对讲 | 清晰度 | 预置点
```

**Logic:**
- Back button: `finish()`
- Sub-tabs: simple `TextView` array with click listeners, underline/highlight selected
- PTZ pad: custom `PtzPadView` handles touch events on directional arrows
- Zoom: +/- buttons increment/decrement a zoom level integer (mock 1x-20x)
- Bottom toolbar: horizontal RecyclerView, items are toggle buttons. "录制" toggles red recording state. Others show a Toast on click (mock behavior).
- Camera name comes from Intent extra.

### PtzPadView (Custom View)

```java
public class PtzPadView extends View {
    // Draws a circular pad with 4 directional arrows and center dot
    // Detects touch in 4 quadrants + center
    // Provides callback: onDirectionChanged(Direction direction)
    // Direction enum: UP, DOWN, LEFT, RIGHT, CENTER, NONE
    
    // Touch handling:
    // - ACTION_DOWN/MOVE: determine quadrant from touch vs center
    // - ACTION_UP: reset to CENTER
    // - Visual feedback: highlight active direction
}
```

Alternatively, the PTZ pad can be built as a **layout with 5 ImageButtons** (up/down/left/right/center) in a `ConstraintLayout` forming a cross pattern, each with click listeners and `OnTouchListener` for hold-to-move. This is simpler and more maintainable than a custom drawn view.

**Recommended approach: Layout-based PTZ pad.**

---

## 7. Mock Data

```java
public class MockData {
    public static List<Camera> getCameras() {
        return Arrays.asList(
            new Camera("1", "行政楼大厅", "行政办公楼", true),
            new Camera("2", "行政楼门卫", "行政办公楼", true),
            new Camera("3", "行政楼走廊", "行政办公楼", false),
            new Camera("4", "教学楼A101", "公共教学楼", true),
            new Camera("5", "教学楼B201", "公共教学楼", true),
            new Camera("6", "教学楼C301", "公共教学楼", true),
            new Camera("7", "广场东入口", "1号主广场", true),
            new Camera("8", "广场西入口", "1号主广场", false),
            new Camera("9", "广场中心", "1号主广场", true)
        );
    }

    public static List<TabFilter> getTabFilters() {
        return Arrays.asList(
            new TabFilter("全部", true),
            new TabFilter("行政办公楼", false),
            new TabFilter("公共教学楼", false),
            new TabFilter("1号主广场", false)
        );
    }

    public static List<BottomToolbarItem> getToolbarItems() {
        return Arrays.asList(
            new BottomToolbarItem("截图", R.drawable.ic_screenshot),
            new BottomToolbarItem("录制", R.drawable.ic_record),
            new BottomToolbarItem("对讲", R.drawable.ic_intercom),
            new BottomToolbarItem("清晰度", R.drawable.ic_resolution),
            new BottomToolbarItem("预置点", R.drawable.ic_preset)
        );
    }
}
```

---

## 8. PreferencesManager

```java
public class PreferencesManager {
    private static final String PREF_NAME = "surveillance_prefs";
    private static final String KEY_PHONE = "saved_phone";
    private static final String KEY_PASSWORD = "saved_password";
    private static final String KEY_REMEMBER = "remember_password";

    // getPhone(), setPhone()
    // getPassword(), setPassword()
    // isRememberPassword(), setRememberPassword()
    // All use SharedPreferences from MyApplication context
}
```

---

## 9. Navigation Flow

1. **App Launch** → `LoginActivity` (launcher)
2. **Login Success** → `HomeActivity` (with camera list)
3. **Click Camera** → `CameraActivity` (with camera ID + name extras)
4. **Back from Camera** → `HomeActivity`
5. **Back from Home** → `LoginActivity`

`LoginActivity` set as launcher in manifest. `HomeActivity` has `android:parentActivityName=".ui.login.LoginActivity"` for up navigation.

---

## 10. AndroidManifest Changes

```xml
<activity
    android:name=".ui.login.LoginActivity"
    android:exported="true"
    android:windowSoftInputMode="adjustResize">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
<activity
    android:name=".ui.home.HomeActivity"
    android:windowSoftInputMode="adjustResize" />
<activity
    android:name=".ui.camera.CameraActivity"
    android:screenOrientation="portrait"
    android:windowSoftInputMode="adjustResize" />
```

Remove `MainActivity` from manifest and delete `MainActivity.java` / `activity_main.xml` (or repurpose `MainActivity` as `LoginActivity` to avoid file churn).

---

## 11. Implementation Order

| Phase | Step | Files | Description |
|-------|------|-------|-------------|
| **0 - Setup** | 0.1 | `libs.versions.toml`, `build.gradle` | Add RecyclerView, CardView, Glide deps |
| | 0.2 | `AndroidManifest.xml` | Register 3 activities, set launcher |
| | 0.3 | `colors.xml`, `themes.xml`, `strings.xml` | Full color scheme, theme, all strings |
| **1 - Data** | 1.1 | `Camera.java`, `TabFilter.java`, `BottomToolbarItem.java` | Data models |
| | 1.2 | `MockData.java` | Centralized mock data |
| | 1.3 | `PreferencesManager.java` | SharedPreferences wrapper |
| **2 - Login** | 2.1 | `bg_login.xml`, `bg_edit_text_rounded.xml`, `bg_button_blue.xml`, `ic_visibility_*.xml` | Login drawables |
| | 2.2 | `fragment_login.xml` | Login layout |
| | 2.3 | `activity_login.xml` | Container layout |
| | 2.4 | `LoginActivity.java` | Host activity |
| | 2.5 | `FragmentLogin.java` | Login logic |
| **3 - Home** | 3.1 | `bg_camera_thumbnail.xml`, `bg_play_icon_circle.xml`, `bg_tab_*.xml`, `ic_search.xml` | Home drawables |
| | 3.2 | `item_tab_filter.xml`, `item_camera_thumbnail.xml` | List item layouts |
| | 3.3 | `fragment_home.xml`, `activity_home.xml` | Home layouts |
| | 3.4 | `TabFilterAdapter.java` | Tab filter adapter |
| | 3.5 | `CameraThumbnailAdapter.java` | Camera grid adapter |
| | 3.6 | `FragmentHome.java`, `HomeActivity.java` | Home logic |
| **4 - Camera** | 4.1 | `ic_ptz_*.xml`, `ic_zoom_*.xml`, `ic_screenshot.xml`, `ic_record.xml`, `bg_ptz_pad.xml`, `bg_toolbar_item.xml` | Camera drawables |
| | 4.2 | `item_bottom_toolbar.xml` | Toolbar item layout |
| | 4.3 | `fragment_camera_preview.xml`, `activity_camera.xml` | Camera layouts |
| | 4.4 | `BottomToolbarAdapter.java` | Toolbar adapter |
| | 4.5 | `PtzPadView.java` (optional) | Custom PTZ or use layout-based |
| | 4.6 | `FragmentCameraPreview.java`, `CameraActivity.java` | Camera logic |
| **5 - Polish** | 5.1 | All files | Edge cases, transitions, visual polish |

---

## 12. Key Implementation Notes

1. **No actual video streaming** — placeholder ImageView with "视频预览" text in preview area.
2. **PTZ controls are purely UI** — arrows show Toast on press, no backend.
3. **Tab filtering is client-side** — filters MockData list in memory.
4. **Password visibility toggle** — swap EditText inputType + swap drawable icon.
5. **Grid item play icon** — centered FrameLayout overlay with semi-transparent circle background.
6. **All drawables are vector XML** — no bitmap dependencies for icons.
7. **Glide** is included for potential future image loading, but thumbnail placeholders use colored drawables or `ic_launcher_foreground`.
8. **Edge-to-edge** — use WindowInsetsCompat in all activities for modern status bar handling.
9. **Delete or keep `MainActivity`** — can be repurposed or removed. Plan assumes removal.
