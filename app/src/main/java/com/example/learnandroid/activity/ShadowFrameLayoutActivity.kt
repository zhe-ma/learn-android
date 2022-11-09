package com.example.learnandroid.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import com.example.learnandroid.R
import com.example.learnandroid.ShadowDialog

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



        val seekBar = findViewById<SeekBar>(R.id.shadowSeekbar)


        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
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