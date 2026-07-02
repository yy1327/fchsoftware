package com.example.myapplication.ui.home;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;

public class HomeActivity extends AppCompatActivity {
    private String token;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_home);

        token = getIntent().getStringExtra("token");
        userId = "1";

        View rootView = findViewById(R.id.home_container);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            int statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(0, statusBarHeight, 0, 0);
            return insets;
        });

        if (savedInstanceState == null) {
            FragmentHome fragment = new FragmentHome();
            Bundle args = new Bundle();
            args.putString("token", token);
            args.putString("userId", userId);
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.home_container, fragment)
                .commit();
        }
    }

    public String getToken() {
        return token;
    }

    public String getUserId() {
        return userId;
    }
}
