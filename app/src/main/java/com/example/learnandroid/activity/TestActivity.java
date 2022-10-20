package com.example.learnandroid.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.learnandroid.R;
import com.example.learnandroid.test.TestLayout;

import org.libpag.PAGFile;
import org.libpag.PAGView;

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

    }
}