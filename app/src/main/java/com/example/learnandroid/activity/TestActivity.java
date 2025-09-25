package com.example.learnandroid.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.airbnb.lottie.LottieAnimationView;
import com.example.learnandroid.R;
import com.example.learnandroid.test.TestLayout;
import com.example.learnandroid.utils.WinkAudioUtils;

import org.libpag.PAGFile;
import org.libpag.PAGView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

//        ImageView image_test = findViewById(R.id.image_test);
//        Bitmap bmp = FileUtil.loadBitmapFromAssets(this, "screenshot.png");
//        image_test.setImageBitmap(bmp);


        TextView textView = findViewById(R.id.tab_name);

        Drawable icon = getResources().getDrawable(R.drawable.red_dot);
        icon.setBounds(0, 0, 44, 44);

        String templateName = "测试小红点";
        ImageSpan imageSpan = new ImageSpan(icon, ImageSpan.ALIGN_BASELINE);
        SpannableString spannableString = new SpannableString(templateName + " " + "测试小红点");
        spannableString.setSpan(imageSpan,
                templateName.length(),
                templateName.length() + 1,
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        textView.setText(spannableString);

        LottieAnimationView lottieAnimationView = findViewById(R.id.Lottie_image);
        //有使用imgs图就添加下面代码，没有就直接设置setAnimation("chinese.json")即可
//        lottieAnimationView.setImageAssetsFolder("chineseImages/");
        lottieAnimationView.setAnimation("love.json");
//        lottieAnimationView.setRepeatMode(LottieDrawable.);//设置播放模式
        lottieAnimationView.setRepeatCount(1);//设置重复次数
        lottieAnimationView.setProgress(0.9f);
//        lottieAnimationView.playAnimation();

//        TestLayout t = findViewById(R.id.test_layout);
//
        PAGView pagView = findViewById(R.id.test_pag1);
        PAGFile pagFile = PAGFile.Load(getAssets(), "refreshing.pag");
        pagView.setComposition(pagFile);
        pagView.setRepeatCount(0);
        pagView.play();
        TestLayout testLayout = new TestLayout(this);

        LinearLayout linearLayout = findViewById(R.id.top_layout123);
        linearLayout.addView(testLayout, 500, 500 );

//        // 实现PCM转AAC功能
//        new Thread(() -> {
//            try {
//                // 从assets复制test.pcm到应用目录
//                String pcmFilePath = copyAssetToFilesDir("test.pcm");
//                if (pcmFilePath != null) {
//                    // 设置AAC输出路径到外部存储私有目录
//                    String aacFilePath = new File(getExternalFilesDir(null), "output.aac").getAbsolutePath();
//
//                    // 调用转换接口
//                    long startTime = System.currentTimeMillis();
//                    boolean success = WinkAudioUtils.INSTANCE.convertPcmToAac(pcmFilePath, aacFilePath, 1, 44100, 64000);
//                    long endTime = System.currentTimeMillis();
//                    long duration = endTime - startTime;
//                    android.util.Log.d("AudioConversion", "PCM转AAC耗时: " + duration + "ms");
//
//                    runOnUiThread(() -> {
//                        if (success) {
//                            Toast.makeText(TestActivity.this,
//                                "PCM转AAC成功！文件保存位置: " + aacFilePath,
//                                Toast.LENGTH_LONG).show();
//                        } else {
//                            Toast.makeText(TestActivity.this,
//                                "PCM转AAC失败",
//                                Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                runOnUiThread(() ->
//                    Toast.makeText(TestActivity.this,
//                        "转换出错: " + e.getMessage(),
//                        Toast.LENGTH_SHORT).show());
//            }
//        }).start();
        
        // 实现视频音频提取功能
        new Thread(() -> {
            try {
                // 从assets复制视频文件到应用目录
                String videoFilePath = copyAssetToFilesDir("1758611440502.mp4");
                if (videoFilePath != null) {
                    // 设置音频输出路径到外部存储私有目录
                    String audioFilePath = new File(getExternalFilesDir(null), "extracted_audio.aac").getAbsolutePath();
                    
                    // 调用音频提取接口
                    long startTime = System.currentTimeMillis();
                    boolean success = WinkAudioUtils.INSTANCE.extractAudioToFile(videoFilePath, audioFilePath);
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    android.util.Log.d("AudioExtraction", "视频音频提取耗时: " + duration + "ms: " + audioFilePath);
                    
                    runOnUiThread(() -> {
                        if (success) {
                            Toast.makeText(TestActivity.this, 
                                "视频音频提取成功！文件保存位置: " + audioFilePath, 
                                Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(TestActivity.this, 
                                "视频音频提取失败", 
                                Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> 
                    Toast.makeText(TestActivity.this, 
                        "音频提取出错: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show());
            }
        }).start();
        
    }
    
    private String copyAssetToFilesDir(String assetFileName) {
        try {
            InputStream inputStream = getAssets().open(assetFileName);
            File outputFile = new File(getFilesDir(), assetFileName);
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            
            outputStream.close();
            inputStream.close();
            
            return outputFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}