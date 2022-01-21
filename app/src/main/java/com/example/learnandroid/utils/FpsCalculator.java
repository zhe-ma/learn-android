package com.example.learnandroid.utils;

import android.util.Log;

public class FpsCalculator {
    private static final String TAG = "FpsCalculator";

    private long startTime = 0;
    private long endTime = 0;
    private long frameCount = 0;
    private double fps = 0.0;

    private static final long INTERVAL = 1000;  // 时间间隔为1秒

    public double fps() {
        long nowTime = System.currentTimeMillis();
        if (startTime == 0) {
            startTime = nowTime;
            return fps;
        }

        frameCount++;
        if (nowTime - startTime > INTERVAL) {
            fps = frameCount * 1000.0 / (nowTime - startTime);
            Log.d(TAG, String.format("FPS: %.2f", fps));

            startTime = nowTime;
            frameCount = 0;
        }

        return fps;
    }
}
