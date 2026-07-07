package com.example.myapplication.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyTimeUtils {

    public static String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    public static String formatDate(long timestamp, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}