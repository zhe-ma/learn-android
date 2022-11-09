package com.example.learnandroid.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.FrameLayout
import com.example.learnandroid.R

class ShadowFrameLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private val TAG = "ShadowFrameLayout"
        private const val DEFAULT_VALUE_SHADOW_RADIUS = 0
        private const val DEFAULT_VALUE_SHADOW_TOP_BREADTH = 5
        private const val DEFAULT_VALUE_SHADOW_LEFT_BREADTH = 5
        private const val DEFAULT_VALUE_SHADOW_RIGHT_BREADTH = 5
        private const val DEFAULT_VALUE_SHADOW_BOTTOM_BREADTH = 5
        private const val DEFAULT_VALUE_SHADOW_OFFSET_X = 0
        private const val DEFAULT_VALUE_SHADOW_OFFSET_Y = 0
        private val DEFAULT_VALUE_SHADOW_BREADTH_COLOR: Int = Color.parseColor("#1A000000")
        private val DEFAULT_VALUE_SHADOW_BACKGROUND_COLOR = Color.parseColor("#FFFFFFFF")
        private const val DEFAULT_VALUE_SHADOW_BLUR = 10
    }

    private var shadowRadius: Int
    private var shadowLeftBreadth: Int
    private var shadowTopBreadth: Int
    private var shadowRightBreadth: Int
    private var shadowBottomBreadth: Int
    private var shadowOffsetX: Int
    private var shadowOffsetY: Int
    private var shadowBackgroundColor: Int
    private var shadowBreadthColor: Int
    private var shadowBlur: Int

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShadowFrameLayout)
        shadowRadius = typedArray.getDimensionPixelSize(
            R.styleable.ShadowFrameLayout_shadowRadius, DEFAULT_VALUE_SHADOW_RADIUS
        )
        shadowLeftBreadth = typedArray.getDimensionPixelSize(
            R.styleable.ShadowFrameLayout_shadowLeftBreadth, DEFAULT_VALUE_SHADOW_LEFT_BREADTH
        )
        shadowTopBreadth = typedArray.getDimensionPixelSize(
            R.styleable.ShadowFrameLayout_shadowTopBreadth, DEFAULT_VALUE_SHADOW_TOP_BREADTH
        )
        shadowRightBreadth = typedArray.getDimensionPixelSize(
            R.styleable.ShadowFrameLayout_shadowRightBreadth, DEFAULT_VALUE_SHADOW_RIGHT_BREADTH
        )
        shadowBottomBreadth = typedArray.getDimensionPixelSize(
            R.styleable.ShadowFrameLayout_shadowBottomBreadth, DEFAULT_VALUE_SHADOW_BOTTOM_BREADTH
        )
        shadowOffsetX = typedArray.getDimensionPixelSize(
            R.styleable.ShadowFrameLayout_shadowOffsetX, DEFAULT_VALUE_SHADOW_OFFSET_X
        )
        shadowOffsetY = typedArray.getDimensionPixelSize(
            R.styleable.ShadowFrameLayout_shadowOffsetY, DEFAULT_VALUE_SHADOW_OFFSET_Y
        )
        shadowBackgroundColor = typedArray.getColor(
            R.styleable.ShadowFrameLayout_shadowBackgroundColor, DEFAULT_VALUE_SHADOW_BACKGROUND_COLOR
        )
        shadowBreadthColor = typedArray.getColor(
            R.styleable.ShadowFrameLayout_shadowBreadthColor, DEFAULT_VALUE_SHADOW_BREADTH_COLOR
        )
        shadowBlur= typedArray.getDimensionPixelSize(
            R.styleable.ShadowFrameLayout_shadowBlur, DEFAULT_VALUE_SHADOW_BLUR
        )
        typedArray.recycle()

        setPadding(shadowLeftBreadth, shadowTopBreadth, shadowRightBreadth, shadowBottomBreadth)
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        val x = paddingLeft
        val y = paddingTop
        val right = width - paddingRight
        val bottom = height - paddingBottom

        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
        paint.color = shadowBackgroundColor
        paint.setShadowLayer(
            shadowBlur.toFloat(),
            shadowOffsetX.toFloat(),
            shadowOffsetY.toFloat(),
            shadowBreadthColor
        )
        val rectF = RectF(x.toFloat(), y.toFloat(), right.toFloat(), bottom.toFloat())
        canvas?.drawRoundRect(rectF, shadowRadius.toFloat(), shadowRadius.toFloat(), paint)
        super.onDraw(canvas)
    }
}
