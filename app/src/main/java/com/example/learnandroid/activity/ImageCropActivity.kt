package com.example.learnandroid.activity

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.learnandroid.R
import com.example.learnandroid.view.ImageCropView
import java.io.OutputStream

class ImageCropActivity : AppCompatActivity() {

    private lateinit var btnPickImage: Button
    private lateinit var btnReset: Button
    private lateinit var btnSave: Button
    private lateinit var imageCropView: ImageCropView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_crop)

        btnPickImage = findViewById(R.id.btnPickImage)
        btnReset = findViewById(R.id.btnReset)
        btnSave = findViewById(R.id.btnSave)
        imageCropView = findViewById(R.id.imageCropView)

        btnPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        btnReset.setOnClickListener {
            imageCropView.reset()
        }

        btnSave.setOnClickListener {
            saveToGallery()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val uri: Uri = data.data ?: return
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            imageCropView.setImageBitmap(bitmap)
        }
    }

    private fun saveToGallery() {
        val cropped = imageCropView.crop()
        if (cropped == null) {
            Toast.makeText(this, "请先选择图片", Toast.LENGTH_SHORT).show()
            return
        }

        val fileName = "crop_${System.currentTimeMillis()}.jpg"
        val outputStream: OutputStream?

        if (Build.VERSION.SDK_INT >= 29) {
            // Android 10+ 使用 MediaStore
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put("relative_path", "Pictures/ImageCrop")
            }
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            outputStream = uri?.let { contentResolver.openOutputStream(it) }
        } else {
            // Android 9 及以下
            @Suppress("DEPRECATION")
            val dir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_PICTURES
            )
            val file = java.io.File(dir, fileName)
            outputStream = java.io.FileOutputStream(file)
            // 通知媒体库
            sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))
        }

        outputStream?.use {
            cropped.compress(Bitmap.CompressFormat.JPEG, 95, it)
            Toast.makeText(this, "已保存到相册", Toast.LENGTH_SHORT).show()
        } ?: Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}
