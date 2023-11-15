package com.example.learnandroid.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.learnandroid.R
import com.example.learnandroid.jnidemo.JniDemo
import com.example.learnandroid.jnidemo.JniDemoConfig
import com.example.learnandroid.jnidemo.JniDemoKotlin
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
//        testJniDemo()
        testJniDemoKotlin()
    }

    private fun testSpdlog() {
        SpdlogHelper.nativeInit()
        Log.d(TAG, SpdlogHelper.getLoggerTag())
    }

    /**
     * 使用java实现Jni
     */
    private fun testJniDemo() {
        val jniDemo = JniDemo(JniDemoConfig(1, "a"))
        Log.e(TAG, jniDemo.config.toString())
        jniDemo.setName("b")
        Log.e(TAG, jniDemo.config.toString())
    }

    /**
     * 使用kotlin实现Jni
     */
    private fun testJniDemoKotlin() {
        val jniDemo = JniDemoKotlin(JniDemoConfig(1, "a"))
        Log.e(TAG, jniDemo.getConfig().toString())
        jniDemo.setName("b")
        Log.e(TAG, jniDemo.getConfig().toString())
    }
}