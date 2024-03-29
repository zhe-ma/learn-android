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
