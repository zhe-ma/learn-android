package com.example.learnandroid.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.example.learnandroid.R;

import org.libpag.PAGFile;
import org.libpag.PAGView;

/* Pag的学习使用
   网址：https://pag.io/
   Demo：https://github.com/libpag/pag-android
 */

public class PagActivity extends AppCompatActivity {
    private static final String TAG = "PagActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pag);

        PAGView pagView = findViewById(R.id.refreshing_pag);
        PAGFile pagFile = PAGFile.Load(getAssets(), "refreshing.pag");
        pagView.setComposition(pagFile);
        pagView.setRepeatCount(1);
        Log.d(TAG,  pagFile.duration() + "");

        Button playButton = findViewById(R.id.play_button);
        playButton.setOnClickListener(v -> {
            pagView.setVisibility(View.VISIBLE);
            pagView.setProgress(0);
            pagView.play();
        });

        Button stopButton = findViewById(R.id.stop_button);
        stopButton.setOnClickListener(v -> pagView.stop());

        Button resumeButton = findViewById(R.id.resume_button);
        resumeButton.setOnClickListener(v -> pagView.play());

        Button closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> {
            pagView.stop();
            pagView.setVisibility(View.GONE);
        });

        SeekBar seekBar = findViewById(R.id.seek_bar);
        seekBar.setMax(100);
        seekBar.setMin(0);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pagView.stop();
                pagView.setProgress((double) progress / 100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}