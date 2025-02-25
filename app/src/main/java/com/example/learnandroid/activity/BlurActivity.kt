package com.example.learnandroid.activity

import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.learnandroid.R
import com.example.learnandroid.widget.BlurImageView
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.engine.ImageEngine
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.utils.ActivityCompatHelper


class BlurActivity : AppCompatActivity() {

    private var blurImageView: BlurImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blur)
        blurImageView = findViewById(R.id.blur_image_view)

        PictureSelector.create(this)
            .openGallery(SelectMimeType.ofImage())
            .setImageEngine(GlideEngine.createGlideEngine())
            .forResult(object : OnResultCallbackListener<LocalMedia?> {
                override fun onResult(result: ArrayList<LocalMedia?>) {
                }

                override fun onCancel() {
                }
            })

    }



}


/**
 * @author：luck
 * @date：2019-11-13 17:02
 * @describe：Glide加载引擎
 */
class GlideEngine private constructor() : ImageEngine {
    /**
     * 加载图片
     *
     * @param context   上下文
     * @param url       资源url
     * @param imageView 图片承载控件
     */
    override fun loadImage(context: Context, url: String, imageView: ImageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return
        }
        Glide.with(context)
            .load(url)
            .into(imageView)
    }

    override fun loadImage(context: Context, imageView: ImageView, url: String, maxWidth: Int, maxHeight: Int) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return
        }
        Glide.with(context)
            .load(url)
            .override(maxWidth, maxHeight)
            .into(imageView)
    }

    /**
     * 加载相册目录封面
     *
     * @param context   上下文
     * @param url       图片路径
     * @param imageView 承载图片ImageView
     */
    override fun loadAlbumCover(context: Context, url: String, imageView: ImageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return
        }
        Glide.with(context)
            .asBitmap()
            .load(url)
            .override(180, 180)
            .sizeMultiplier(0.5f)
            .transform(CenterCrop(), RoundedCorners(8))
            .placeholder(R.drawable.ps_image_placeholder)
            .into(imageView)
    }


    /**
     * 加载图片列表图片
     *
     * @param context   上下文
     * @param url       图片路径
     * @param imageView 承载图片ImageView
     */
    override fun loadGridImage(context: Context, url: String, imageView: ImageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return
        }
        Glide.with(context)
            .load(url)
            .override(200, 200)
            .centerCrop()
            .placeholder(R.drawable.ps_image_placeholder)
            .into(imageView)
    }

    override fun pauseRequests(context: Context) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return
        }
        Glide.with(context).pauseRequests()
    }

    override fun resumeRequests(context: Context) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return
        }
        Glide.with(context).resumeRequests()
    }

    private object InstanceHolder {
        val instance: GlideEngine = GlideEngine()
    }

    companion object {
        fun createGlideEngine(): GlideEngine {
            return InstanceHolder.instance
        }
    }
}