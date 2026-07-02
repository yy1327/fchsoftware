package com.example.myapplication.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.data.model.BaseResponse;
import com.example.myapplication.data.model.LoginResponse;
import com.example.myapplication.data.sip.SipConfig;
import com.example.myapplication.data.sip.SipService;
import com.example.myapplication.network.RetrofitClient;
import com.example.myapplication.service.KeepAliveManager;
import com.example.myapplication.ui.home.HomeActivity;
import com.example.myapplication.util.PreferencesManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class FragmentLogin extends Fragment {
    private EditText etPhone;
    private EditText etPassword;
    private CheckBox cbRememberPassword;
    private ImageView btnTogglePassword;
    private ImageView btnClearPassword;
    private PreferencesManager prefsManager;
    private boolean isPasswordVisible = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        prefsManager = new PreferencesManager(requireContext());
        initViews(view);
        loadSavedData();
        setupListeners();
    }

    private void initViews(View view) {
        etPhone = view.findViewById(R.id.etPhone);
        etPassword = view.findViewById(R.id.etPassword);
        cbRememberPassword = view.findViewById(R.id.cbRememberPassword);
        btnTogglePassword = view.findViewById(R.id.btnTogglePassword);
        btnClearPassword = view.findViewById(R.id.btnClearPassword);
        Button btnLogin = view.findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> onLoginClicked());
    }

    private void loadSavedData() {
        if (prefsManager.isRememberPassword()) {
            etPhone.setText(prefsManager.getPhone());
            etPassword.setText(prefsManager.getPassword());
            cbRememberPassword.setChecked(true);
        }
    }

    private void setupListeners() {
        btnTogglePassword.setOnClickListener(v -> togglePasswordVisibility());
        btnClearPassword.setOnClickListener(v -> etPassword.setText(""));
    }

    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_visibility_on);
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_visibility_off);
        }
        etPassword.setSelection(etPassword.getText().length());
    }

    private void onLoginClicked() {
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (phone.isEmpty()) {
            Toast.makeText(requireContext(), "请输入手机号", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(requireContext(), "请输入密码", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cbRememberPassword.isChecked()) {
            prefsManager.setPhone(phone);
            prefsManager.setPassword(password);
            prefsManager.setRememberPassword(true);
        } else {
            prefsManager.setRememberPassword(false);
        }

        // 调用登录接口（密码MD5加密）
        String md5Password = md5(password);
        Log.d("Login", "尝试登录: phone=" + phone + ", md5=" + md5Password);
        RetrofitClient.getInstance()
                .getApiService()
                .login(phone, md5Password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<BaseResponse<LoginResponse>>() {
                    @Override
                    public void onNext(BaseResponse<LoginResponse> response) {
                        if (response.result != null) {
                            String token = response.result.authToken != null ? response.result.authToken : response.result.authtoken;
                            Log.d("Login", "result: return=" + response.result.returnCode
                                    + ", token=" + token
                                    + ", userId=" + response.result.userId);
                            if (response.result.returnCode == 1 && token != null) {
                                Log.d("Login", "登录成功, token=" + token);
                                Toast.makeText(requireContext(), "登录成功", Toast.LENGTH_SHORT).show();

                                // 配置 SIP 账号（使用手机号作为 SIP 用户名）
                                SipConfig config = SipConfig.getInstance();
                                config.setUsername(phone);
                                config.setPassword(password);

                                // 启动 SIP 注册
                                registerSip();

                                Intent intent = new Intent(requireContext(), HomeActivity.class);
                                intent.putExtra("token", token);
                                intent.putExtra("userId", response.result.userId);
                                startActivity(intent);
                                requireActivity().finish();
                            } else {
                                Toast.makeText(requireContext(), "登录失败", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(requireContext(), "登录失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("Login", "请求错误: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                        Toast.makeText(requireContext(), "登录失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02X", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void registerSip() {
        try {
            SipService sipService = SipService.getInstance(requireContext());
            sipService.setCallback(new SipService.SipCallback() {
                @Override
                public void onRegistered() {
                    Log.d("Login", "SIP 注册成功");
                }

                @Override
                public void onRegistrationFailed(String error) {
                    Log.e("Login", "SIP 注册失败: " + error);
                }

                @Override
                public void onUnregistered() {
                    Log.d("Login", "SIP 注销");
                }

                @Override
                public void onCallIncoming(String caller) {
                }

                @Override
                public void onCallConnected() {
                }

                @Override
                public void onCallEnded() {
                }

                @Override
                public void onCallFailed(String error) {
                }
            });

            boolean result = sipService.register();
            Log.d("Login", "SIP 注册请求: " + (result ? "已发送" : "失败"));

            // 启动前台服务保活
            KeepAliveManager.getInstance(requireContext()).startKeepAlive();
            Log.d("Login", "前台服务已启动");

        } catch (Exception e) {
            Log.e("Login", "SIP 注册异常", e);
        }
    }
}
