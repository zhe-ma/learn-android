package com.example.learnandroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.learnandroid.recyclerview.Fruit;
import com.example.learnandroid.recyclerview.FruitAdapter;

import java.util.ArrayList;
import java.util.List;

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

        // TODO: 计算好Padding距离！
        recyclerView.setPadding(500, 0, 500, 0);

        // 设置为Item居中对齐整个控件
        linearSnapHelper = new LinearSnapHelper();
        linearSnapHelper.attachToRecyclerView(recyclerView);

        // 惯性滑动，可设置滑动速度
        initLinearSmoothScroller();
        initFruitAdapter();

        recyclerView.setAdapter(fruitAdapter);
    }

    private void initFruitAdapter() {
        fruitAdapter = new FruitAdapter(fruitList);
        fruitAdapter.setOnItemClickListener(new FruitAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.d(TAG, "Select Item: " + position);
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

//                ImageView image = findViewById(R.id.selected_fruit);
//                image.setImageResource(fruitList.get(position).getImageId());
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
        Fruit apple = new Fruit("Apple", R.drawable.apple_pic);
        fruitList.add(apple);
        Fruit banana = new Fruit("Banana", R.drawable.banana_pic);
        fruitList.add(banana);
        Fruit orange = new Fruit("Orange", R.drawable.orange_pic);
        fruitList.add(orange);
        Fruit watermelon = new Fruit("Watermelon", R.drawable.watermelon_pic);
        fruitList.add(watermelon);
        Fruit pear = new Fruit("Pear", R.drawable.pear_pic);
        fruitList.add(pear);
        Fruit grape = new Fruit("Grape", R.drawable.grape_pic);
        fruitList.add(grape);
        Fruit pineapple = new Fruit("Pineapple", R.drawable.pineapple_pic);
        fruitList.add(pineapple);
        Fruit strawberry = new Fruit("Strawberry", R.drawable.strawberry_pic);
        fruitList.add(strawberry);
        Fruit cherry = new Fruit("Cherry", R.drawable.cherry_pic);
        fruitList.add(cherry);
        Fruit mango = new Fruit("Mango", R.drawable.mango_pic);
        fruitList.add(mango);
    }
}