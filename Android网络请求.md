Android 网络请求与视频列表教学内容（Java）

## **一、HTTP & JSON 基础** 

## 1.HTTP 常见概念

| ***\*概念\**** | ***\*说明\****                |
| -------------- | ----------------------------- |
| URL            | 接口地址                      |
| Method         | GET / POST                    |
| Header         | 请求头（token、Content-Type） |
| Body           | 请求参数                      |
| Status Code    | 200 / 401 / 500               |

 

## 2.JSON 结构

```json
{

 "code": 0,

 "msg": "success",

 "data": {

  "token": "abc123"

 }
}
```

 

# **二、OkHttp & Retrofit 简介** 

***\*OkHttp\**** 

l 负责底层 HTTP 通信

l 连接池、超时、拦截器

***\*Retrofit\**** 

**l** ***\*RESTful 接口封装\****

l 自动解析 JSON

l 配合 RxJava / 线程切换

✅ **企业主流组合：Retrofit + OkHttp + Gson + RxJava**

 

# **三、需要引入的依赖（⭐必须）**

build.gradle（Module 级）

```java
dependencies {

  // Retrofit
  implementation 'com.squareup.retrofit2:retrofit:2.9.0'
  implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
  implementation 'com.squareup.retrofit2:adapter-rxjava2:2.9.0'


  // OkHttp
  implementation 'com.squareup.okhttp3:okhttp:4.12.0'


  // RxJava
  implementation 'io.reactivex.rxjava2:rxjava:2.2.21'
  implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'

 
  // RecyclerView
  implementation 'androidx.recyclerview:recyclerview:1.3.2'


  // Glide（图片加载）
  implementation 'com.github.bumptech.glide:glide:4.16.0'
  annotationProcessor 'com.github.bumptech.glide:compiler:4.16.0'

}
```

 权限

```
<uses-permission android:name="android.permission.INTERNET"/>
```



# **四、数据模型（Camera / Cameras）**

单个摄像头（示例）

```java
public class Cameras {

  private String cameraId;

  private String cameraName;

  private String cameraPhoto2;

......

 

  public String getCameraId() {

    return cameraId;

  }

 

  public String getCameraName() {

    return cameraName;

  }

 

  public String getCameraPhoto2() {

    return cameraPhoto2;

}

......

}
```

接口返回结构

```java
public class BaseResponse<T> {

    private T result;

}
```



# **五、Retrofit 网络封装**

RetrofitClient（单例）

示例：

```java
public class RetrofitClient {

  private static volatile RetrofitClient instance;
  private final ApiService apiService;
  private static final String BASE_URL = "http://223.100.6.179:18206/api/v1/";


  private RetrofitClient() {

    OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build();


    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build();


    apiService = retrofit.create(ApiService.class);

  }

 
  public static RetrofitClient getInstance() {
    if (instance == null) {
      synchronized (RetrofitClient.class) {
        if (instance == null) {
          instance = new RetrofitClient();
        }
      }
    }
    return instance;
  }

 
  public ApiService getApiService() {
    return apiService;
  }

}
```



# **六、ApiService 接口**

登录账号：18500000009(一个组一个账号，你是第几组后两位就是改成你对应的组号)

登录密码：fch123456

```java
public interface ApiService {
    
  @FormUrlEncoded
  @POST("app/login")
  Observable<BaseResponse<LoginResponse>> login( @Field("LoginForm[phone]") String phone, @Field("LoginForm[password]") String password);

  @FormUrlEncoded
  @POST("camera/info2")
  Observable<BaseResponse<CameraListResponse>> getCameraList(
      @Field("access-token") String token,
      @Field("UserID") String userId,
      @Field("page") int page,
      @Field("size") int size
  );

}
```



# **七、RecyclerView Adapter**

示例：

```java
public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {


  private Context context;
  private List<Cameras> videoItems = new ArrayList<>();
  private int selectedPosition = -1;
  private OnItemClickListener onItemClickListener;


  public interface OnItemClickListener {
    void onItemClick(int position);
  }

 
  public void setOnItemClickListener(OnItemClickListener listener) {
    this.onItemClickListener = listener;
  }


  public void setVideoItems(List<Cameras> videoItems) {
    this.videoItems = videoItems;
    notifyDataSetChanged();
  }


  public void addData(List<Cameras> newData) {
    this.videoItems.addAll(newData);
    notifyDataSetChanged();
  }

 

  public VideoAdapter(List<Cameras> videoItems, Context context) {
    this.videoItems = videoItems;
    this.context = context;
  }

 

  @Override
  public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.activity_video_item, parent, false);
    return new VideoViewHolder(view);
  }

 
  @Override
  public void onBindViewHolder(VideoViewHolder holder, int position) {

    Cameras item = videoItems.get(position);
    holder.title.setText(item.getCameraName());


    if (item.getCameraPhoto2() != null && !item.getCameraPhoto2().isEmpty()) {
      Glide.with(context)
          .load(item.getCameraPhoto2())
          .into(holder.cameraImg);
    } else {
      Glide.with(context)
          .load(R.mipmap.icon_player)
          .into(holder.cameraImg);
    }

 

    holder.itemView.setOnClickListener(v -> {
      if (onItemClickListener != null) {
        onItemClickListener.onItemClick(position);
      }
    });
  }

 
  @Override
  public int getItemCount() {
    return videoItems.size();
  }

  static class VideoViewHolder extends RecyclerView.ViewHolder {
    TextView title;
    ImageView cameraImg;

    public VideoViewHolder(View itemView) {
      super(itemView);
      title = itemView.findViewById(R.id.videoName);
      cameraImg = itemView.findViewById(R.id.cameraImg);
    }

  }

}
```



# **八、Activity 中调用接口并绑定 Adapter**

示例：

```java
public class VideoActivity extends AppCompatActivity {

  private RecyclerView recyclerView;
  private VideoAdapter videoAdapter;
  private List<Cameras> cameraList = new ArrayList<>();
  private String userID = "1";
  private String token = "";
  private int pageNo = 0;
  private int pageSize = 30;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_video);

    recyclerView = findViewById(R.id.recyclerView);
    recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

    videoAdapter = new VideoAdapter(cameraList, this);
    recyclerView.setAdapter(videoAdapter);

    videoAdapter.setOnItemClickListener(position -> {
      Cameras camera = cameraList.get(position);
      Toast.makeText(this, camera.getCameraName(), Toast.LENGTH_SHORT).show();
    });

    loadCameraList();

  }

 

  private void loadCameraList() {

    RetrofitClient.getInstance()
        .getApiService()
        .getCameraList(token, userId, pageNo, pageSize)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<BaseResponse<CameraListResponse>>() {

          @Override
          public void onNext(BaseResponse<CameraListResponse> response) {
            if (response.code == 0) {
              videoAdapter.setVideoItems(response.data.list);
            }
          }


          @Override
          public void onError(Throwable e) {
            Log.e("VideoActivity", e.getMessage());
          }

          @Override
          public void onComplete() {}

        });

  }

}
```

