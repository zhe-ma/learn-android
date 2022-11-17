package com.example.learnandroid.recyclerview

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class FruitDecoration : RecyclerView.ItemDecoration() {
    private val paint = Paint()

    companion object {
        private const val DIVIDER_INDEX = 1
        private const val DIVIDER_RIGHT_PADDING = 400
    }

    init {
        paint.isAntiAlias = true
        paint.color = Color.GRAY
        paint.strokeWidth = 10f
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)

        for (i in 0 until parent.childCount) {
            val view = parent.getChildAt(i)
            val index = parent.getChildAdapterPosition(view)
            if (index == DIVIDER_INDEX) {
                val top = view.top
                val left = view.right + DIVIDER_RIGHT_PADDING / 2
                val right = view.right + DIVIDER_RIGHT_PADDING / 2 + 10
                val bottom = view.bottom
                c.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
            }
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = (view.layoutParams as RecyclerView.LayoutParams).viewLayoutPosition

        if (position == DIVIDER_INDEX) {
            outRect.set(0, 0, DIVIDER_RIGHT_PADDING, 0)
        } else {
            outRect.set(0, 0, 0, 0)
        }
    }
}