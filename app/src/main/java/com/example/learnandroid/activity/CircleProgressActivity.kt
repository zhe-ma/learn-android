package com.example.learnandroid.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.learnandroid.R
import com.example.learnandroid.widget.CircleProgressView

class CircleProgressActivity : AppCompatActivity() {
    private lateinit var circleProgressView: CircleProgressView
    private lateinit var btnStartProgress: Button
    private val handler = Handler(Looper.getMainLooper())
    private var progress = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_circle_progress)

        circleProgressView = findViewById(R.id.circleProgressView)
        btnStartProgress = findViewById(R.id.btnStartProgress)

        btnStartProgress.setOnClickListener {
            startProgressAnimation()
        }
    }

    private fun startProgressAnimation() {
        progress = 0
        circleProgressView.setProgress(progress.toFloat())
        
        handler.post(object : Runnable {
            override fun run() {
                if (progress < 100) {
                    progress++
                    circleProgressView.setProgress(progress.toFloat())
                    handler.postDelayed(this, 50)
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
} 