package com.example.learnandroid.utils

import com.example.learnandroid.MainApplication
import kotlin.math.roundToInt

val Int.dp: Int
    get() = this.toFloat().dp

val Float.dp: Int
    get() = dpToPx(this)

fun dpToPx(dp: Float): Int {
    return MainApplication.context?.let {
        (it.resources.displayMetrics.density * dp).roundToInt()
    } ?: 0
}

fun px2dp(px: Float): Float {
    return MainApplication.context?.let {
        px / it.resources.displayMetrics.density
    } ?: 0f
}
