package com.example.learnandroid.recyclerview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learnandroid.R;

import org.libpag.PAGFile;
import org.libpag.PAGView;

import java.util.ArrayList;
import java.util.List;

public class TabLayout extends RecyclerView {
    LinearLayoutManager linearLayoutManager;
    FruitAdapter fruitAdapter;
    LinearSnapHelper linearSnapHelper;
    boolean smoothScroll = false;
    LinearSmoothScroller linearSmoothScroller;

    private List<Fruit> fruitList = new ArrayList<>();

    public TabLayout(@NonNull Context context) {
        this(context, null);
    }

    public interface OnItemClickListener {
        void onItemClick(int position, boolean needScroll);
    }
    public TabLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        setLayoutManager(linearLayoutManager);
        // 设置Item不绑定到边缘
        setClipToPadding(false);
        addItemDecoration(new FruitDecoration());

        // 获取RecyclerView的宽度
        ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int padding = getWidth() / 2;
                // 设置控件左右两边的留白
                setPadding(padding, 0, padding, 0);
            }
        });

        // 设置为Item居中对齐整个控件
        linearSnapHelper = new LinearSnapHelper();
        linearSnapHelper.attachToRecyclerView(this);

        // 惯性滑动，可设置滑动速度
//        initLinearSmoothScroller();
        initRecyclerViewScrollListener();
        initFruitList();
        initFruitAdapter();

        fruitAdapter.c = context;
        setAdapter(fruitAdapter);
        fruitAdapter.selectItem(0, true);
    }


    private void initRecyclerViewScrollListener() {
        // RecyclerView滚动：https://www.jianshu.com/p/ce347cf991db
        addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        fruitAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position, boolean needScroll) {
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

                        newscrollToPosition(position);
                    }
                }
            }
        });
    }

    private void newscrollToPosition(int position) {
        scrollToPosition(position);
        post(() -> {
            View view = linearLayoutManager.findViewByPosition(position);
            if (view == null) {
                return;
            }

            int[] snapDistance = linearSnapHelper.calculateDistanceToFinalSnap(linearLayoutManager, view);
            if (snapDistance.length >= 2 && (snapDistance[0] != 0 || snapDistance[1] != 0)) {
                scrollBy(snapDistance[0], snapDistance[1]);
            }
        });
    }

//    private void initLinearSmoothScroller() {
//        linearSmoothScroller = new LinearSmoothScroller(this) {
//            /* 控制单位速度。
//            毫秒/像素, 滑动1像素需要多少毫秒。默认为 (25F/densityDpi) 毫秒/像素。
//            mdpi上, 1英寸有160个像素点, 25/160, xxhdpi,1英寸有480个像素点, 25/480。
//             */
//            @Override
//            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
//                // 速度降为1/5。 也就是移动一个像素的时间扩大了5倍。
//                return super.calculateSpeedPerPixel(displayMetrics) * 5;
//            }
//        };
//    }

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

    public class FruitAdapter extends RecyclerView.Adapter<FruitAdapter.ViewHolder> {
        static private final String TAG = "FruitAdapter";

        private List<Fruit> fruitList;
        private OnItemClickListener onItemClickListener;
        private int selectedPosition = -1;
        public Context c;


        public FruitAdapter(List<Fruit> fruitList) {
            this.fruitList = fruitList;
        }

        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }

        public void selectItem(int position, boolean needScroll) {
            if (position < 0 || position >= this.fruitList.size() || selectedPosition == position) {
                return;
            }

            selectedPosition = position;
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(position, needScroll);
            }

            notifyDataSetChanged();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            PAGView pagView;
            TextView fruitName;

            public ViewHolder(View view) {
                super(view);
                fruitName = view.findViewById(R.id.fruit_name);
                imageView = view.findViewById(R.id.fruit_image);

                // 使用另外一种方式，动态添加控件到Layout里面
                LinearLayout linearLayout = view.findViewById(R.id.linearlayout_container);
                pagView = new PAGView(view.getContext());
                linearLayout.addView(pagView);
                ViewGroup.LayoutParams params = pagView.getLayoutParams();
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                pagView.setLayoutParams(params);

                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = getAdapterPosition();
                        Log.d(TAG, "GetAdapterPosition: " + getAdapterPosition());
                        FruitAdapter.this.selectItem(position, true);
                    }
                });
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fruit_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Fruit fruit = fruitList.get(position);
            holder.fruitName.setText(fruit.getName());
            holder.fruitName.setTypeface(Typeface.defaultFromStyle(selectedPosition == position ? Typeface.BOLD: Typeface.NORMAL));
            holder.imageView.setImageResource(fruit.getImageId());

            if (selectedPosition == position) {
                PAGFile pagFile = PAGFile.Load(c.getAssets(), "refreshing.pag");
                holder.pagView.setComposition(pagFile);
                holder.pagView.setRepeatCount(0);
                holder.pagView.play();
            } else {
                holder.pagView.stop();
                holder.pagView.setComposition(null);
                holder.pagView.flush();
            }
        }

        @Override
        public int getItemCount() {
            return fruitList.size();
        }
    }
}
