package com.example.myapplication.ui.video;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.VideoAdapter;
import com.example.myapplication.data.model.BaseResponse;
import com.example.myapplication.data.model.CameraListResponse;
import com.example.myapplication.data.model.Cameras;
import com.example.myapplication.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class VideoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VideoAdapter videoAdapter;
    private List<Cameras> cameraList = new ArrayList<>();
    private String token = "";
    private String userId = "1";
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

        // 从Intent获取token
        if (getIntent() != null) {
            token = getIntent().getStringExtra("token");
            userId = getIntent().getStringExtra("userId");
        }

        loadCameraList();
    }

    private void loadCameraList() {
        RetrofitClient.getInstance()
                .getApiService()
                .getCameraList(token, userId, pageNo, pageSize)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<BaseResponse<CameraListResponse>>() {
                    @Override
                    public void onNext(BaseResponse<CameraListResponse> response) {
                        if (response.result != null && response.result.message != null && response.result.message.cameras != null) {
                            videoAdapter.setVideoItems(response.result.message.cameras);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("VideoActivity", "Error: " + e.getMessage());
                        Toast.makeText(VideoActivity.this, "加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }
}
