package com.example.learnandroid.utils

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.example.learnandroid.MainApplication
import kotlin.math.roundToInt

val Int.dp: Int
    get() = this.toFloat().dp

val Float.dp: Int
    get() = dpToPx(this)

fun dpToPx(dp: Float): Int {
    // 获取density的另一种方式 Resources.getSystem().displayMetrics.density
    return MainApplication.context?.let {
        (it.resources.displayMetrics.density * dp).roundToInt()
    } ?: 0
}

fun px2dp(px: Float): Float {
    return MainApplication.context?.let {
        px / it.resources.displayMetrics.density
    } ?: 0f
}

fun vibrate(milliseconds: Long) {
    // 需要权限：android.permission.VIBRATE
    MainApplication.context?.let {
        val vibrator = it.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(effect)
        } else {
            vibrator.vibrate(milliseconds)
        }
    }
}