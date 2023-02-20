package com.example.learnandroid.activity

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.*
import android.webkit.MimeTypeMap.getFileExtensionFromUrl
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.learnandroid.MainApplication.Companion.context
import com.example.learnandroid.R

class WebviewActivity : AppCompatActivity() {
    companion object {
        const val TAG = "WebviewActivity"
    }

    private val webview: WebView by lazy { findViewById(R.id.activity_webview_id) }
    private val button: Button by lazy { findViewById(R.id.webview_test_calljs_btn) }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        initWebview()

        button.setOnClickListener {
            webview.post {
                webview.loadUrl("javascript:callJS(\"NativeCallJs\")")
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebview() {
        webview.settings.javaScriptEnabled = true
        webview.settings.javaScriptCanOpenWindowsAutomatically = true // 设置允许JS弹窗
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

        // https://juejin.cn/post/6844904153605505032
        webview.webChromeClient = object : WebChromeClient() {
            override fun onJsPrompt(
                view: WebView?,
                url: String?, // file:///android_asset/webview_test.html
                message: String?, // js://showToast?arg=HelloJsCallNative
                defaultValue: String?,
                result: JsPromptResult?
            ): Boolean {
                val uri: Uri = Uri.parse(message)
                return if (uri.scheme?.equals("js") == true) {
                    if (uri.authority?.equals("showToast") == true) {
                        Toast.makeText(context, uri.query, Toast.LENGTH_SHORT).show()
                        result?.confirm("HelloResult");
                    }
                    true
                } else {
                    super.onJsPrompt(view, url, message, defaultValue, result)
                }
            }
        }
    }

}
