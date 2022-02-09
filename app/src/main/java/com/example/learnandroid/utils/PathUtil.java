package com.example.learnandroid.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class PathUtil {
    private static final String TAG = "PathUtil";

    public static String getSdOutDir() {
        String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/learn-android/";

        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }

        return dir;
    }
}
