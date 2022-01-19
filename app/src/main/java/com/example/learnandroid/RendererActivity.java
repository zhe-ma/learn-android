package com.example.learnandroid;

import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.learnandroid.renderer.CircleRenderer05;
import com.example.learnandroid.renderer.ClearRenderer01;
import com.example.learnandroid.renderer.PolygonRenderer06;
import com.example.learnandroid.renderer.TriangleRenderer02;
import com.example.learnandroid.renderer.TriangleRenderer03;
import com.example.learnandroid.renderer.TriangleRenderer04;

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
                glSurfaceView.setRenderer(new ClearRenderer01());
                break;
            case R.id.triangle_renderer:
                glSurfaceView.setRenderer(new TriangleRenderer02());
                break;
            case R.id.triangle_renderer_with_matrix:
                glSurfaceView.setRenderer(new TriangleRenderer03());
                break;
            case R.id.triangle_renderer_with_color:
                glSurfaceView.setRenderer(new TriangleRenderer04());
                break;
            case R.id.circle_renderer:
                glSurfaceView.setRenderer(new CircleRenderer05());
                break;
            case R.id.polygon_renderer:
                glSurfaceView.setRenderer(new PolygonRenderer06());
                break;
            default:
                break;
        }

        setContentView(glSurfaceView);
    }
}
