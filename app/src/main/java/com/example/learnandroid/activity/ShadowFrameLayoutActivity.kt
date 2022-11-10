package com.example.learnandroid.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import com.example.learnandroid.R
import com.example.learnandroid.ShadowDialog
import com.example.learnandroid.utils.vibrate

class ShadowFrameLayoutActivity : AppCompatActivity() {
    companion object {
        const val TAG = "ShadowFrameLayoutActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shadowlayout)

        val button = findViewById<Button>(R.id.shadowButton)
        button.setOnClickListener {
            val dialog = ShadowDialog(this)
            dialog.show()
        }

        val seekBarNumber = findViewById<TextView>(R.id.seekBarNumber)
        val seekBar = findViewById<SeekBar>(R.id.shadowSeekbar)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            var canVibrate = true
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                seekBarNumber.text = "$progress"
                if (canVibrate && progress == 100) {
                    vibrate(100)
                    canVibrate = false
                } else if (progress in 95..105) {  // 95-105之间吸附到100
                    seekBar?.progress = 100
                } else {
                    canVibrate = true
                }
                Log.d(TAG, progress.toString())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        seekBar.progress = 105

    }
}