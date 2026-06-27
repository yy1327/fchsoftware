package com.example.myapplication.ui.camera;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;

public class CameraActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        if (savedInstanceState == null) {
            FragmentCameraPreview fragment = new FragmentCameraPreview();
            fragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.camera_container, fragment)
                .commit();
        }
    }
}
