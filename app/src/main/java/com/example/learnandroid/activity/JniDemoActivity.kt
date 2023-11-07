package com.example.learnandroid.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.learnandroid.R
import com.example.learnandroid.jnidemo.SpdlogHelper

class JniDemoActivity : AppCompatActivity() {
    companion object {
        const val TAG = "JniDemoActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jni_demo)

        testSpdlog()
    }

    private fun testSpdlog() {
        SpdlogHelper.nativeInit()
        Log.d(TAG, SpdlogHelper.getLoggerTag())
    }
}