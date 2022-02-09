package com.example.learnandroid.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

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

    public static boolean saveBitmap(Bitmap bitmap, String filePath, Bitmap.CompressFormat format, int quality) {
        boolean ret = false;
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
            ret = bitmap.compress(format, quality, bos);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }

        return ret;
    }
}
