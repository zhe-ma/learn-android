package com.example.learnandroid.activity

import android.os.Bundle
import android.util.Log
import android.webkit.*
import android.webkit.MimeTypeMap.getFileExtensionFromUrl
import androidx.appcompat.app.AppCompatActivity
import com.example.learnandroid.MainApplication.Companion.context
import com.example.learnandroid.R

class WebviewActivity : AppCompatActivity() {
    companion object {
        const val TAG = "WebviewActivity"
    }

    private val webview: WebView by lazy { findViewById(R.id.activity_webview_id) }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        initWebview()
    }

    // TODO: 响应：https://www.jianshu.com/p/08920c2bb128
    private fun initWebview() {
        webview.settings.javaScriptEnabled = true
        webview.loadUrl("file:///android_asset/webview_test.html")

        webview.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                val fileUrl = request?.url.toString()
                if (fileUrl.endsWith(".mp4")) {
                    var mimeType = MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(getFileExtensionFromUrl(fileUrl))
                    Log.d(TAG, "url: $fileUrl, mimeType: $mimeType")
                    val header = HashMap<String, String>()
                    header["Access-Control-Allow-Origin"] = "*"
                    header["Access-Control-Allow-Headers"] = "Content-Type"
                    return WebResourceResponse(
                        mimeType,
                        "",
                        200,
                        "OK",
                        header,
                        context?.assets?.open("3比4.mp4")
                    )
                } else {
                    return super.shouldInterceptRequest(view, request)
                }
            }
        }
    }

}
