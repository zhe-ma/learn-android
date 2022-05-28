package com.example.learnandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.example.learnandroid.recyclerview.Fruit;
import com.example.learnandroid.recyclerview.FruitAdapter;

import java.util.ArrayList;
import java.util.List;

/* 模拟抖音底部滚动文字TAB选中栏
目前实现的功能：
    1. 滑动停止后选中，并位于中间
    2. 选中Item的自动位于中间
    3. 选中Item的字体编程粗体
    4. 点击Item可以选中并自动滚动到中间
    5. 移动Item时，Item自动惯性移动到离中间最近的Item。使用LinearSnapHelper
    6. 避免重复选中
    7. 解决了scrollToPosition和LinerSnapHelper一起使用时位置不准确的问题。
    8. 控件左右留了Padding，使得第一个和最后一个Item可以居中。setPadding，clipToPadding=false
    9. 可以设置平滑滚动。使用LinearSmoothScroller
 */

public class RecyclerViewActivity extends AppCompatActivity {
    private static final String TAG = "RecyclerViewActivity";

    private List<Fruit> fruitList = new ArrayList<>();
    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;
    FruitAdapter fruitAdapter;
    LinearSnapHelper linearSnapHelper;
    boolean smoothScroll = false;
    LinearSmoothScroller linearSmoothScroller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);

        initFruitList();

        recyclerView = findViewById(R.id.recycler_view);
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        // 设置Item不绑定到边缘
        recyclerView.setClipToPadding(false);

        // 获取RecyclerView的宽度
        ViewTreeObserver viewTreeObserver = recyclerView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int padding = recyclerView.getWidth() / 2;
                // 设置控件左右两边的留白
                recyclerView.setPadding(padding, 0, padding, 0);
            }
        });

        // 设置为Item居中对齐整个控件
        linearSnapHelper = new LinearSnapHelper();
        linearSnapHelper.attachToRecyclerView(recyclerView);

        // 惯性滑动，可设置滑动速度
        initLinearSmoothScroller();
        initRecyclerViewScrollListener();
        initFruitAdapter();

        recyclerView.setAdapter(fruitAdapter);
        fruitAdapter.selectItem(0, true);
    }

    private void initRecyclerViewScrollListener() {
        // RecyclerView滚动：https://www.jianshu.com/p/ce347cf991db
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // 滚动停止的时候再去设置选中
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    View v = linearSnapHelper.findSnapView(linearLayoutManager);
                    if (v != null) {
                        int position = linearLayoutManager.getPosition(v);
                        fruitAdapter.selectItem(position, false);
                    }
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void initFruitAdapter() {
        fruitAdapter = new FruitAdapter(fruitList);
        fruitAdapter.setOnItemClickListener(new FruitAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, boolean needScroll) {
                Log.d(TAG, "Select Item: " + position);
                if (needScroll) {
                    if (smoothScroll) {
                        linearSmoothScroller.setTargetPosition(position);
                        linearLayoutManager.startSmoothScroll(linearSmoothScroller);
                    } else {
                        // 使用scrollToPosition滑动的位置并不会使item居中，因为没有触发SnapHelper。必须调用smoothScroll才能触发。
                        // 方法一：调用完scrollToPosition后再进行一个像素的smoothScrollBy的来触发SnapHelper。
                        //   代码如下：
                        //     linearLayoutManager.scrollToPosition(position);
                        //      recyclerView.smoothScrollBy(1, 0);
                        //    但是有个问题是smoothScrollBy的smooth操作会导致一个惯性效果，也就是移动1个像素后会惯性震荡一下。
                        //    所以这边在调用scrollToPosition后，调用了一下smoothScrollBy来触发SnapHelper。
                        // 方法二：scrollToPosition移动后，该position的view和SnapHelper的距离，然后再进行一个位置调整。
                        //  代码见scrollToPosition
                        // 参考：
                        // https://stackoverflow.com/questions/41280176/recyclerview-with-snaphelper-item-is-not-snapped-after-scrolltoposition
                        // https://stackoverflow.com/questions/42988016/how-to-programmatically-snap-to-position-on-recycler-view-with-linearsnaphelper

                        scrollToPosition(position);
                    }
                }

                ImageView image = findViewById(R.id.selected_fruit);
                image.setImageResource(fruitList.get(position).getImageId());
            }
        });
    }

    private void scrollToPosition(int position) {
        recyclerView.scrollToPosition(position);
        recyclerView.post(() -> {
            View view = linearLayoutManager.findViewByPosition(position);
            if (view == null) {
                return;
            }

            int[] snapDistance = linearSnapHelper.calculateDistanceToFinalSnap(linearLayoutManager, view);
            if (snapDistance.length >= 2 && (snapDistance[0] != 0 || snapDistance[1] != 0)) {
                recyclerView.scrollBy(snapDistance[0], snapDistance[1]);
            }
        });
    }

    private void initLinearSmoothScroller() {
        linearSmoothScroller = new LinearSmoothScroller(this) {
            /* 控制单位速度。
            毫秒/像素, 滑动1像素需要多少毫秒。默认为 (25F/densityDpi) 毫秒/像素。
            mdpi上, 1英寸有160个像素点, 25/160, xxhdpi,1英寸有480个像素点, 25/480。
             */
            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                // 速度降为1/5。 也就是移动一个像素的时间扩大了5倍。
                return super.calculateSpeedPerPixel(displayMetrics) * 5;
            }
        };
    }

    private void initFruitList() {
        Fruit apple = new Fruit("0Apple", R.drawable.apple_pic);
        fruitList.add(apple);
        Fruit banana = new Fruit("1Banana", R.drawable.banana_pic);
        fruitList.add(banana);
        Fruit orange = new Fruit("2Orange", R.drawable.orange_pic);
        fruitList.add(orange);
        Fruit watermelon = new Fruit("3Watermelon", R.drawable.watermelon_pic);
        fruitList.add(watermelon);
        Fruit pear = new Fruit("4Pear", R.drawable.pear_pic);
        fruitList.add(pear);
        Fruit grape = new Fruit("5Grape", R.drawable.grape_pic);
        fruitList.add(grape);
        Fruit pineapple = new Fruit("6Pineapple", R.drawable.pineapple_pic);
        fruitList.add(pineapple);
        Fruit strawberry = new Fruit("7Strawberry", R.drawable.strawberry_pic);
        fruitList.add(strawberry);
        Fruit cherry = new Fruit("8Cherry", R.drawable.cherry_pic);
        fruitList.add(cherry);
        Fruit mango = new Fruit("9Mango", R.drawable.mango_pic);
        fruitList.add(mango);
    }
}