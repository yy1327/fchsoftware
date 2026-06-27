package com.example.myapplication.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
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
import com.example.myapplication.ui.home.HomeActivity;
import com.example.myapplication.util.PreferencesManager;

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

        Intent intent = new Intent(requireContext(), HomeActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }
}
