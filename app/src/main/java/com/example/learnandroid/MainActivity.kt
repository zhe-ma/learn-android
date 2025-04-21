package com.example.learnandroid

import android.Manifest
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.graphics.Color
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.learnandroid.activity.*
import java.util.*

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
        private val BUTTON_DATA = mutableListOf<Pair<String, Class<*>>>(
            BUTTON_NAME_TEST to TestActivity::class.java,
            BUTTON_NAME_CLEAR_RENDERER to RendererActivity::class.java,
            BUTTON_NAME_TRIANGLE_RENDERER to RendererActivity::class.java,
            BUTTON_NAME_MATRIX_TRIANGLE_RENDERER to RendererActivity::class.java,
            BUTTON_NAME_COLOR_TRIANGLE_RENDERER to RendererActivity::class.java,
            BUTTON_NAME_CIRCLE_RENDERER to RendererActivity::class.java,
            BUTTON_NAME_POLYGON_RENDERER to RendererActivity::class.java,
            BUTTON_NAME_CUBE_RENDERER to RendererActivity::class.java,
            BUTTON_NAME_SQUARE_TEXTURE_RENDERER to RendererActivity::class.java,
            BUTTON_NAME_RECYCLER_VIEW to RecyclerViewActivity::class.java,
            BUTTON_NAME_PAG_VIEW to PagActivity::class.java,
            BUTTON_NAME_LOTTIE_VIEW to LottieActivity::class.java,
            BUTTON_NAME_CAMERA1 to JniDemoActivity::class.java,
            BUTTON_NAME_SHADOW_FRAME_LAYOUT to ShadowFrameLayoutActivity::class.java,
            BUTTON_NAME_WEBVIEW to WebviewActivity::class.java,
            BUTTON_CANVAS to CanvasActivity::class.java,
            BUTTON_NAME_CIRCLE_PROGRESS to CircleProgressActivity::class.java
        )
    }

    private val recyclerView: RecyclerView? by lazy { findViewById(R.id.main_activity_rv) }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView?.adapter = ButtonAdapter()
        recyclerView?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        checkPermissions()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }

    override fun onRestart() {
        Log.d(TAG, "onRestart")
        super.onRestart()
    }

    override fun onStart() {
        Log.d(TAG, "onStart")
        super.onStart()
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
        super.onStop()
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d(TAG, "onSaveInstanceState")
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        Log.d(TAG, "onRestoreInstanceState")
        super.onRestoreInstanceState(savedInstanceState)
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        for (permission in permissions) {
            val a = ContextCompat.checkSelfPermission(this, permission)
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), 1)
            }
        }
    }

    class ButtonAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return object : RecyclerView.ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.activity_main_rv_item, parent, false)
            ) {}
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val buttonItem = BUTTON_DATA[position]
            val button = holder.itemView.findViewById<TextView>(R.id.main_activity_rv_item_text)
            button.text = buttonItem.first
            button.setOnClickListener {
                val intent = Intent(holder.itemView.context, buttonItem.second)
                intent.putExtra("buttonName", buttonItem.first)
                startActivity(holder.itemView.context, intent, null)
            }

            button.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_DOWN) {
                    button.setTextColor(Color.parseColor("#6F2468AC"))
                    button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25f)
                } else if (event.action == MotionEvent.ACTION_UP) {
                    button.setTextColor(Color.parseColor("#6F1A1A1A"))
                    button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22f)
                }
                false
            }
        }

        override fun getItemCount(): Int {
            return BUTTON_DATA.size
        }
    }
}
