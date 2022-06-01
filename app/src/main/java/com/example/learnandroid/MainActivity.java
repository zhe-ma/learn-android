package com.example.learnandroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
        findViewById(R.id.test_recycler_view).setOnClickListener(this);
        findViewById(R.id.pag_view_activity).setOnClickListener(this);
        findViewById(R.id.lottie_view_activity).setOnClickListener(this);

        checkPermissions();
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
        } else if (id == R.id.test_recycler_view) {
            intent = new Intent(this, RecyclerViewActivity.class);
        } else if (id == R.id.pag_view_activity) {
            intent = new Intent(this, PagActivity.class);
        } else if (id == R.id.lottie_view_activity) {
            intent = new Intent(this, LottieActivity.class);
        } else {
            intent = new Intent(this, RendererActivity.class);
        }

        intent.putExtra("buttonId", id);
        startActivity(intent);
    }

    private void checkPermissions() {
        String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
        };

        for (String permission : permissions) {
            int a = ContextCompat.checkSelfPermission(this, permission);
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{permission}, 1);
            }
        }
    }
}
