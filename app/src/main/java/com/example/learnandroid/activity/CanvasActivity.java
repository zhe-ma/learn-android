package com.example.learnandroid.activity;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import com.example.learnandroid.R;


public class CanvasActivity extends AppCompatActivity {
    private static final String TAG = "CanvasActivity";

    private int x = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_canvas);
        View v = findViewById(R.id.paintView);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                x += 200;
                v.scrollTo(x, 0);
            }
        });
    }
}