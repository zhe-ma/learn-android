package com.example.learnandroid.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator


class CircleProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND  // 设置圆角端点
    }

    private var progress = 0f
    private var maxProgress = 100f
    
    // 将dp转为px
    private val strokeWidth = context.resources.displayMetrics.density * 2f  // 2dp
    
    // 定义颜色常量
    private val progressColor = Color.parseColor("#FFFFFF")        // 进度颜色
    private val backgroundColor = Color.parseColor("#80FFFFFF")    // 背景颜色(50%透明度的白色)

    init {
        paint.strokeWidth = strokeWidth
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val centerX = width / 2f
        val centerY = height / 2f
        // 半径需要考虑画笔宽度，避免裁切
        val radius = (width.coerceAtMost(height) - strokeWidth) / 2f
        
        // 绘制背景圆环
        paint.color = backgroundColor
        canvas.drawCircle(centerX, centerY, radius, paint)
        
        // 绘制进度圆环
        paint.color = progressColor
        val sweepAngle = 360f * (progress / maxProgress)
        val rect = RectF(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )
        // 从12点方向开始绘制，所以起始角度是-90
        canvas.drawArc(rect, -90f, sweepAngle, false, paint)
    }

    /**
     * 设置进度
     * @param progress 进度值(0-100)
     * @param animate 是否使用动画
     */
    fun setProgress(progress: Float, animate: Boolean = true) {
        if (animate) {
            val animator = ValueAnimator.ofFloat(this.progress, progress)
            animator.duration = 300 // 动画持续300ms
            animator.interpolator = LinearInterpolator()
            animator.addUpdateListener { animation ->
                this.progress = animation.animatedValue as Float
                invalidate()
            }
            animator.start()
        } else {
            this.progress = progress
            invalidate()
        }
    }

    /**
     * 设置最大进度值
     */
    fun setMaxProgress(maxProgress: Float) {
        this.maxProgress = maxProgress
        invalidate()
    }
}