package com.example.learnandroid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

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
        int id = intent.getIntExtra("buttonId", -1);
        switch (id) {
            case R.id.clear_renderer:
                glSurfaceView.setRenderer(new Z01_ClearRenderer());
                break;
            case R.id.triangle_renderer:
                glSurfaceView.setRenderer(new Z02_TriangleRenderer());
                break;
            case R.id.triangle_renderer_with_matrix:
                glSurfaceView.setRenderer(new Z03_TriangleRenderer());
                break;
            case R.id.triangle_renderer_with_color:
                glSurfaceView.setRenderer(new Z04_TriangleRenderer());
                break;
            case R.id.circle_renderer:
                glSurfaceView.setRenderer(new Z05_CircleRenderer());
                break;
            case R.id.polygon_renderer:
                glSurfaceView.setRenderer(new Z06_PolygonRenderer());
                break;
            case R.id.cube_renderer:
                glSurfaceView.setRenderer(new Z07_CubeRenderer());
                break;
            case R.id.square_texture_renderer:
                Bitmap bitmap = FileUtil.loadBitmapFromAssets(this, "screenshot.png");
                glSurfaceView.setRenderer(new Z08_TextureRenderer(bitmap));
                break;
            default:
                break;
        }

        setContentView(glSurfaceView);
    }
}
