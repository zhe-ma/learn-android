package com.example.learnandroid.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.learnandroid.R;
import com.example.learnandroid.renderer.Z05_CircleRenderer;
import com.example.learnandroid.renderer.Z01_ClearRenderer;
import com.example.learnandroid.renderer.Z07_CubeRenderer;
import com.example.learnandroid.renderer.Z06_PolygonRenderer;
import com.example.learnandroid.renderer.Z08_TextureRenderer;
import com.example.learnandroid.renderer.Z02_TriangleRenderer;
import com.example.learnandroid.renderer.Z03_TriangleRenderer;
import com.example.learnandroid.renderer.Z04_TriangleRenderer;
import com.example.learnandroid.utils.FileUtil;

public class RendererActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GLSurfaceView glSurfaceView = new GLSurfaceView(this);
        // 必须设置，否则会crash
        // <uses-feature android:glEsVersion="0x00020000" android:required="true" />
        // 设置opengl版本号
        glSurfaceView.setEGLContextClientVersion(2);

        Intent intent = getIntent();
        String buttonName = intent.getStringExtra("buttonName");
        switch (buttonName) {
            case ButtonConstantsKt.BUTTON_NAME_CLEAR_RENDERER:
                glSurfaceView.setRenderer(new Z01_ClearRenderer());
                break;
            case ButtonConstantsKt.BUTTON_NAME_TRIANGLE_RENDERER:
                glSurfaceView.setRenderer(new Z02_TriangleRenderer());
                break;
            case ButtonConstantsKt.BUTTON_NAME_MATRIX_TRIANGLE_RENDERER:
                glSurfaceView.setRenderer(new Z03_TriangleRenderer());
                break;
            case ButtonConstantsKt.BUTTON_NAME_COLOR_TRIANGLE_RENDERER:
                glSurfaceView.setRenderer(new Z04_TriangleRenderer());
                break;
            case ButtonConstantsKt.BUTTON_NAME_CIRCLE_RENDERER:
                glSurfaceView.setRenderer(new Z05_CircleRenderer());
                break;
            case ButtonConstantsKt.BUTTON_NAME_POLYGON_RENDERER:
                glSurfaceView.setRenderer(new Z06_PolygonRenderer());
                break;
            case ButtonConstantsKt.BUTTON_NAME_CUBE_RENDERER:
                glSurfaceView.setRenderer(new Z07_CubeRenderer());
                break;
            case ButtonConstantsKt.BUTTON_NAME_SQUARE_TEXTURE_RENDERER:
                Bitmap bitmap = FileUtil.loadBitmapFromAssets(this, "screenshot.png");
                glSurfaceView.setRenderer(new Z08_TextureRenderer(bitmap));
                break;
            default:
                break;
        }

        setContentView(glSurfaceView);
    }
}
