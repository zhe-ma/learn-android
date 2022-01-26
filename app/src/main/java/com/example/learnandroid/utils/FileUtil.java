package com.example.learnandroid.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class FileUtil {
    private static final String TAG = "FileUtil";

    public static Bitmap loadBitmapFromAssets(Context context, String file) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(context.getAssets().open(file));
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }

        return bitmap;
    }
}
