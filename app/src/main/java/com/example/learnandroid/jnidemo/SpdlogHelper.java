package com.example.learnandroid.jnidemo;

import android.util.Log;

public class SpdlogHelper {
    public static final String TAG = "SpdlogHelper";

    public native static String getLoggerTag();

    public native static void nativeInit();

    // 静态代码块，在虚拟机加载类的时候就会加载执行，而且只执行一次
    static {
        try {
            System.loadLibrary("learnandroid");
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
}
