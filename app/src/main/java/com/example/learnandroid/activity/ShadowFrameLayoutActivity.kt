package com.example.learnandroid.activity

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.learnandroid.R
import com.example.learnandroid.ShadowDialog
import com.example.learnandroid.utils.vibrate
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

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


        val button2 = findViewById<Button>(R.id.BottomSheetButton)
        button2.setOnClickListener {
            val dialog = BlankBottomSheetDialogFragment()
            dialog.show(supportFragmentManager, "BlankBottomSheetDialogFragment")
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

class BlankBottomSheetDialogFragment : BottomSheetDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(context!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        return inflater.inflate(R.layout.activity_bottom_sheet_layout, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogBg)
    }

    override fun onStart() {
        super.onStart()
        if (dialog is BottomSheetDialog) {
//            (dialog as BottomSheetDialog).behavior.peekHeight = 1280
            (dialog as BottomSheetDialog).behavior.state = BottomSheetBehavior.STATE_EXPANDED
//            dialog?.window?.setLayout(FrameLayout.LayoutParams.MATCH_PARENT, 1280) //最大高度也是
//            dialog?.window?.setGravity(Gravity.BOTTOM)
        }
    }

}