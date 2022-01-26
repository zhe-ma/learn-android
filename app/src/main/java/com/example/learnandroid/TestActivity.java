package com.example.learnandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.learnandroid.utils.FileUtil;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        ImageView image_test = findViewById(R.id.image_test);
        Bitmap bmp = FileUtil.loadBitmapFromAssets(this, "screenshot.png");
        image_test.setImageBitmap(bmp);
    }
}