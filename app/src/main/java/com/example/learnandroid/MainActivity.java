package com.example.learnandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.clear_renderer).setOnClickListener(this);
        findViewById(R.id.triangle_renderer).setOnClickListener(this);
        findViewById(R.id.triangle_renderer_with_matrix).setOnClickListener(this);
        findViewById(R.id.triangle_renderer_with_color).setOnClickListener(this);
        findViewById(R.id.circle_renderer).setOnClickListener(this);
        findViewById(R.id.polygon_renderer).setOnClickListener(this);
        findViewById(R.id.cube_renderer).setOnClickListener(this);
        findViewById(R.id.square_texture_renderer).setOnClickListener(this);
        findViewById(R.id.test_activity).setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        startActivityByButtonId(view.getId());
    }

    private void startActivityByButtonId(int id) {
        Intent intent = null;
        if (id == R.id.test_activity) {
            intent = new Intent(this, TestActivity.class);
        } else {
            intent = new Intent(this, RendererActivity.class);
        }

        intent.putExtra("buttonId", id);
        startActivity(intent);
    }
}
