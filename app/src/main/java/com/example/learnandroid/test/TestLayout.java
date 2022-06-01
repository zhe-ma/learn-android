package com.example.learnandroid.test;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import org.libpag.PAGFile;
import org.libpag.PAGView;

public class TestLayout extends FrameLayout  {
    public TestLayout(Context context) {
        this(context, null);
    }

    public TestLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TestLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        setBackgroundColor(Color.LTGRAY);

        PAGView pagView = new PAGView(context);
        PAGFile pagFile = PAGFile.Load(context.getAssets(), "refreshing.pag");
        pagView.setComposition(pagFile);
        pagView.setRepeatCount(0);
        pagView.play();

        addView(pagView, new LayoutParams(500, 500));

        TextView textView = new TextView(context);
        textView.setTextColor(Color.RED);
        textView.setText("阿斯蒂芬大师傅");
        addView(textView, new LayoutParams(400, 200));
    }

}
