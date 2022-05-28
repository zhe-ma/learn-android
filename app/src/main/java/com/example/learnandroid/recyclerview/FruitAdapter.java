package com.example.learnandroid.recyclerview;

import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learnandroid.R;

import java.util.List;

public class FruitAdapter extends RecyclerView.Adapter<FruitAdapter.ViewHolder> {
    static private final String TAG = "FruitAdapter";

    private List<Fruit> fruitList;
    private OnItemClickListener onItemClickListener;
    private int selectedPosition = -1;

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView fruitImage;
        TextView fruitName;

        public ViewHolder(View view) {
            super(view);
            fruitImage = view.findViewById(R.id.fruit_image);
            fruitName = view.findViewById(R.id.fruit_name);

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

    public FruitAdapter(List<Fruit> fruitList) {
        this.fruitList = fruitList;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, boolean needScroll);
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

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fruit_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Fruit fruit = fruitList.get(position);
        holder.fruitImage.setImageResource(fruit.getImageId());
        holder.fruitName.setText(fruit.getName());
        holder.fruitName.setTypeface(Typeface.defaultFromStyle(selectedPosition == position ? Typeface.BOLD: Typeface.NORMAL));
    }

    @Override
    public int getItemCount() {
        return fruitList.size();
    }

}
