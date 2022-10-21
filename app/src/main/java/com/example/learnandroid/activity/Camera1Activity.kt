package com.example.learnandroid.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.example.learnandroid.R

class Camera1Activity : AppCompatActivity() {
    companion object {
        const val TAG = "Camera1Activity"

        // Used to load the 'myapplication' library on application startup.
        init {
            System.loadLibrary("learnandroid")
        }
    }

    external fun stringFromJNI(): String

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera1)

        findViewById<TextView>(R.id.tv).text = stringFromJNI()
    }
}