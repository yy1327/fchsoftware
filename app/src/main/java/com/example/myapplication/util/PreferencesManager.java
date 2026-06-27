package com.example.myapplication.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {
    private static final String PREF_NAME = "surveillance_prefs";
    private static final String KEY_PHONE = "saved_phone";
    private static final String KEY_PASSWORD = "saved_password";
    private static final String KEY_REMEMBER = "remember_password";

    private final SharedPreferences prefs;

    public PreferencesManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public String getPhone() { return prefs.getString(KEY_PHONE, ""); }
    public void setPhone(String phone) { prefs.edit().putString(KEY_PHONE, phone).apply(); }

    public String getPassword() { return prefs.getString(KEY_PASSWORD, ""); }
    public void setPassword(String password) { prefs.edit().putString(KEY_PASSWORD, password).apply(); }

    public boolean isRememberPassword() { return prefs.getBoolean(KEY_REMEMBER, false); }
    public void setRememberPassword(boolean remember) { prefs.edit().putBoolean(KEY_REMEMBER, remember).apply(); }
}
