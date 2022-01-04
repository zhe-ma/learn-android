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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.clear_renderer:
            case R.id.triangle_renderer:
                startActivityByButtonId(view.getId());
                break;
            default:
                break;
        }
    }

    private void startActivityByButtonId(int id) {
        Intent intent = new Intent(this, RendererActivity.class);
        intent.putExtra("buttonId", id);
        startActivity(intent);
    }

}
