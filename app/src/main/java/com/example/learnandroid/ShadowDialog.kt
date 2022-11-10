package com.example.learnandroid

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.SeekBar
import com.example.learnandroid.utils.dp

class ShadowDialog @JvmOverloads constructor(context: Context)
    : Dialog(context, R.style.FloatingDialog) {

    companion object {
        private val DIALOG_WIDTH = 76.dp
        private val DIALOG_HEIGHT = 254.dp

//        private val DIALOG_WIDTH = dp2px(90f)
//        private val DIALOG_HEIGHT = dp2px(264f)

        private fun dp2px(dpValue: Float): Int {
            val scale = Resources.getSystem().displayMetrics.density
            // 像素没有小数；此处+0.5是为了解决向上取整，防止非整型的dp数被int取整后丢失精度
            // e.g. 1.5dp在3x的手机上应该按5px处理
            return (dpValue * scale + 0.5f).toInt()
        }
    }

    private var rootView: View

    init {

        rootView = LayoutInflater.from(context).inflate(R.layout.shadow_dialog, null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(rootView!!)
        window?.setLayout(DIALOG_WIDTH, DIALOG_HEIGHT)
        window?.setGravity(Gravity.CENTER)
        window?.attributes?.dimAmount = 0.1f
        setCancelable(true)
        setCanceledOnTouchOutside(true)
    }


}
