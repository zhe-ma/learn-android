package com.example.learnandroid

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.learnandroid.activity.*

class MainActivity : AppCompatActivity(), View.OnClickListener {
    companion object {
        private const val TAG = "MainActivity"
    }

    private val recyclerView: RecyclerView? by lazy { findViewById(R.id.main_activity_rv) }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
        setContentView(R.layout.activity_main1)
        findViewById<View>(R.id.clear_renderer).setOnClickListener(this)
        findViewById<View>(R.id.triangle_renderer).setOnClickListener(this)
        findViewById<View>(R.id.triangle_renderer_with_matrix).setOnClickListener(this)
        findViewById<View>(R.id.triangle_renderer_with_color).setOnClickListener(this)
        findViewById<View>(R.id.circle_renderer).setOnClickListener(this)
        findViewById<View>(R.id.polygon_renderer).setOnClickListener(this)
        findViewById<View>(R.id.cube_renderer).setOnClickListener(this)
        findViewById<View>(R.id.square_texture_renderer).setOnClickListener(this)
        findViewById<View>(R.id.test_activity).setOnClickListener(this)
        findViewById<View>(R.id.test_recycler_view).setOnClickListener(this)
        findViewById<View>(R.id.pag_view_activity).setOnClickListener(this)
        findViewById<View>(R.id.lottie_view_activity).setOnClickListener(this)
        findViewById<View>(R.id.camera1_activity).setOnClickListener(this)
        findViewById<View>(R.id.shadowlayout_activity).setOnClickListener(this)
        findViewById<View>(R.id.webview_activity).setOnClickListener(this)
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

    override fun onClick(view: View) {
        startActivityByButtonId(view.id)
    }

    private fun startActivityByButtonId(id: Int) {
        var intent = if (id == R.id.test_activity) {
            Intent(this, TestActivity::class.java)
        } else if (id == R.id.test_recycler_view) {
            Intent(this, RecyclerViewActivity::class.java)
        } else if (id == R.id.pag_view_activity) {
            Intent(this, PagActivity::class.java)
        } else if (id == R.id.lottie_view_activity) {
            Intent(this, LottieActivity::class.java)
        } else if (id == R.id.camera1_activity) {
            Intent(this, Camera1Activity::class.java)
        } else if (id == R.id.shadowlayout_activity) {
            Intent(this, ShadowFrameLayoutActivity::class.java)
        } else if (id == R.id.webview_activity) {
            Intent(this, WebviewActivity::class.java)
        } else {
            Intent(this, RendererActivity::class.java)
        }
        intent.putExtra("buttonId", id)
        startActivity(intent)
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

}