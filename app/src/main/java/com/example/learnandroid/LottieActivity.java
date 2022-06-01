package com.example.learnandroid;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.RenderMode;

import org.libpag.PAGFile;
import org.libpag.PAGView;

/* Lottie的学习使用
   网址：https://lottiefiles.com
 */

public class LottieActivity extends AppCompatActivity {
    private static final String TAG = "LottieActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lottie);

        LottieAnimationView lottieAnimationView = findViewById(R.id.lottie_view);

        lottieAnimationView.setAnimation("love.json");
        // 可选择软件，硬件，自动渲染。Pag只支持软件渲染
        lottieAnimationView.setRenderMode(RenderMode.AUTOMATIC);
        lottieAnimationView.setRepeatCount(1);

        Button playButton = findViewById(R.id.play_button);
        playButton.setOnClickListener(v -> {
            lottieAnimationView.playAnimation();
        });

        Button stopButton = findViewById(R.id.stop_button);
        stopButton.setOnClickListener(v -> lottieAnimationView.pauseAnimation());

        Button resumeButton = findViewById(R.id.resume_button);
        resumeButton.setOnClickListener(v -> lottieAnimationView.resumeAnimation());

        Button closeButton = findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> {
            lottieAnimationView.cancelAnimation();
        });

        SeekBar seekBar = findViewById(R.id.seek_bar);
        seekBar.setMax(100);
        seekBar.setMin(0);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                lottieAnimationView.setProgress((float) progress / 100);
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