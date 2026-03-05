package com.example.learnandroid.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class ImageCropView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // ---- 图片相关 ----
    private var bitmap: Bitmap? = null
    // 图片变换矩阵（缩放 + 平移）
    private val imageMatrix = Matrix()
    // 初始矩阵，用于重置
    private val initMatrix = Matrix()

    // ---- 裁剪框 ----
    private val cropRect = RectF()
    private val initCropRect = RectF()

    // ---- 画笔 ----
    private val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    // 蒙层画笔（半透明黑色）
    private val maskPaint = Paint().apply {
        color = Color.argb(150, 0, 0, 0)
        style = Paint.Style.FILL
    }
    // 裁剪框边框画笔
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    // 把手画笔
    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
        strokeCap = Paint.Cap.ROUND
    }

    // ---- 把手尺寸 ----
    private val handleLength = 40f   // 把手线段长度（px）
    private val handleTouchRadius = 60f  // 把手触摸判定半径

    // ---- 手势：缩放 ----
    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())

    // ---- 触摸状态 ----
    private enum class TouchMode { NONE, DRAG_IMAGE, DRAG_HANDLE }
    private var touchMode = TouchMode.NONE

    // 拖动图片时的上一个触摸点
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    // 当前拖动的把手索引（0=左上, 1=右上, 2=右下, 3=左下）
    private var activeHandle = -1

    // 裁剪框最小尺寸
    private val minCropSize = 80f

    // 最大缩放比（相对于初始缩放）
    private val maxScaleFactor = 10f

    // ---- 对外接口 ----

    /** 设置要裁剪的图片 */
    fun setImageBitmap(bmp: Bitmap) {
        bitmap = bmp
        post { initLayout() }
    }

    /**
     * 获取当前图片在 View 坐标系中的边界 RectF
     */
    private fun getImageBounds(): RectF {
        val bmp = bitmap ?: return RectF()
        val src = RectF(0f, 0f, bmp.width.toFloat(), bmp.height.toFloat())
        val dst = RectF()
        imageMatrix.mapRect(dst, src)
        return dst
    }

    /**
     * 修正图片矩阵，确保图片始终覆盖裁剪框：
     * 1. 若图片宽或高小于裁剪框，先等比放大到恰好覆盖裁剪框
     * 2. 再修正平移，使图片边缘不缩进裁剪框内部
     */
    private fun clampImageMatrix() {
        val bmp = bitmap ?: return
        var bounds = getImageBounds()

        // --- 第一步：若图片尺寸小于裁剪框，强制放大到恰好覆盖 ---
        val scaleW = if (bounds.width() < cropRect.width()) cropRect.width() / bounds.width() else 1f
        val scaleH = if (bounds.height() < cropRect.height()) cropRect.height() / bounds.height() else 1f
        val forceScale = maxOf(scaleW, scaleH)
        if (forceScale > 1f) {
            // 以裁剪框中心为缩放中心，保持视觉稳定
            imageMatrix.postScale(forceScale, forceScale, cropRect.centerX(), cropRect.centerY())
            bounds = getImageBounds()
        }

        // --- 第二步：修正平移，确保图片覆盖裁剪框 ---
        var dx = 0f
        var dy = 0f

        // 水平方向
        if (bounds.left > cropRect.left) dx = cropRect.left - bounds.left
        if (bounds.right < cropRect.right) dx = cropRect.right - bounds.right

        // 垂直方向
        if (bounds.top > cropRect.top) dy = cropRect.top - bounds.top
        if (bounds.bottom < cropRect.bottom) dy = cropRect.bottom - bounds.bottom

        if (dx != 0f || dy != 0f) {
            imageMatrix.postTranslate(dx, dy)
        }
    }

    /** 重置裁剪框和图片变换到初始状态 */
    fun reset() {
        imageMatrix.set(initMatrix)
        cropRect.set(initCropRect)
        invalidate()
    }

    /**
     * 执行裁剪，返回裁剪后的 Bitmap
     * 将裁剪框坐标通过矩阵逆变换映射回原图坐标
     */
    fun crop(): Bitmap? {
        val bmp = bitmap ?: return null

        // 求图片矩阵的逆矩阵
        val invertMatrix = Matrix()
        if (!imageMatrix.invert(invertMatrix)) return null

        // 将裁剪框映射到原图坐标
        val srcRect = RectF(cropRect)
        invertMatrix.mapRect(srcRect)

        // 限制在原图范围内
        val bmpRect = RectF(0f, 0f, bmp.width.toFloat(), bmp.height.toFloat())
        if (!srcRect.intersect(bmpRect)) return null

        return Bitmap.createBitmap(
            bmp,
            srcRect.left.toInt().coerceAtLeast(0),
            srcRect.top.toInt().coerceAtLeast(0),
            srcRect.width().toInt().coerceAtLeast(1),
            srcRect.height().toInt().coerceAtLeast(1)
        )
    }

    // ---- 初始化布局 ----

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (bitmap != null) initLayout()
    }

    private fun initLayout() {
        val bmp = bitmap ?: return
        val vw = width.toFloat()
        val vh = height.toFloat()
        if (vw <= 0 || vh <= 0) return

        // 计算图片适配 View 的缩放比（fitCenter）
        val scale = min(vw / bmp.width, vh / bmp.height)
        val scaledW = bmp.width * scale
        val scaledH = bmp.height * scale
        val dx = (vw - scaledW) / 2f
        val dy = (vh - scaledH) / 2f

        imageMatrix.reset()
        imageMatrix.setScale(scale, scale)
        imageMatrix.postTranslate(dx, dy)
        initMatrix.set(imageMatrix)

        // 裁剪框默认适应图片大小（留一点内边距）
        val padding = 0f
        cropRect.set(dx + padding, dy + padding, dx + scaledW - padding, dy + scaledH - padding)
        initCropRect.set(cropRect)

        invalidate()
    }

    // ---- 绘制 ----

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bmp = bitmap ?: return

        // 1. 绘制图片
        canvas.drawBitmap(bmp, imageMatrix, imagePaint)

        // 2. 绘制蒙层（裁剪框外四个矩形）
        val vw = width.toFloat()
        val vh = height.toFloat()
        // 上
        canvas.drawRect(0f, 0f, vw, cropRect.top, maskPaint)
        // 下
        canvas.drawRect(0f, cropRect.bottom, vw, vh, maskPaint)
        // 左
        canvas.drawRect(0f, cropRect.top, cropRect.left, cropRect.bottom, maskPaint)
        // 右
        canvas.drawRect(cropRect.right, cropRect.top, vw, cropRect.bottom, maskPaint)

        // 3. 绘制裁剪框边框
        canvas.drawRect(cropRect, borderPaint)

        // 4. 绘制四角把手（L 形）
        drawHandles(canvas)
    }

    private fun drawHandles(canvas: Canvas) {
        val l = cropRect.left
        val t = cropRect.top
        val r = cropRect.right
        val b = cropRect.bottom
        val hl = handleLength

        // 左上角
        canvas.drawLine(l, t, l + hl, t, handlePaint)
        canvas.drawLine(l, t, l, t + hl, handlePaint)
        // 右上角
        canvas.drawLine(r - hl, t, r, t, handlePaint)
        canvas.drawLine(r, t, r, t + hl, handlePaint)
        // 右下角
        canvas.drawLine(r - hl, b, r, b, handlePaint)
        canvas.drawLine(r, b - hl, r, b, handlePaint)
        // 左下角
        canvas.drawLine(l, b - hl, l, b, handlePaint)
        canvas.drawLine(l, b, l + hl, b, handlePaint)
    }

    // ---- 触摸事件 ----

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // 双指缩放交给 ScaleGestureDetector
        scaleDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y
                val handle = hitTestHandle(x, y)
                if (handle >= 0) {
                    touchMode = TouchMode.DRAG_HANDLE
                    activeHandle = handle
                } else {
                    touchMode = TouchMode.DRAG_IMAGE
                    lastTouchX = x
                    lastTouchY = y
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (scaleDetector.isInProgress) return true
                val x = event.x
                val y = event.y
                when (touchMode) {
                    TouchMode.DRAG_IMAGE -> {
                        val dx = x - lastTouchX
                        val dy = y - lastTouchY
                        imageMatrix.postTranslate(dx, dy)
                        clampImageMatrix()
                        lastTouchX = x
                        lastTouchY = y
                        invalidate()
                    }
                    TouchMode.DRAG_HANDLE -> {
                        updateCropRect(activeHandle, x, y)
                        invalidate()
                    }
                    else -> {}
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touchMode = TouchMode.NONE
                activeHandle = -1
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                // 双指开始，切换为缩放模式，停止图片拖动
                touchMode = TouchMode.NONE
            }
        }
        return true
    }

    /** 判断触摸点是否命中某个把手，返回把手索引，否则返回 -1 */
    private fun hitTestHandle(x: Float, y: Float): Int {
        val corners = arrayOf(
            PointF(cropRect.left, cropRect.top),    // 0 左上
            PointF(cropRect.right, cropRect.top),   // 1 右上
            PointF(cropRect.right, cropRect.bottom),// 2 右下
            PointF(cropRect.left, cropRect.bottom)  // 3 左下
        )
        for (i in corners.indices) {
            val dx = x - corners[i].x
            val dy = y - corners[i].y
            if (sqrt(dx * dx + dy * dy) <= handleTouchRadius) return i
        }
        return -1
    }

    /** 根据把手索引更新裁剪框 */
    private fun updateCropRect(handle: Int, x: Float, y: Float) {
        when (handle) {
            0 -> { // 左上
                cropRect.left = min(x, cropRect.right - minCropSize)
                cropRect.top = min(y, cropRect.bottom - minCropSize)
            }
            1 -> { // 右上
                cropRect.right = max(x, cropRect.left + minCropSize)
                cropRect.top = min(y, cropRect.bottom - minCropSize)
            }
            2 -> { // 右下
                cropRect.right = max(x, cropRect.left + minCropSize)
                cropRect.bottom = max(y, cropRect.top + minCropSize)
            }
            3 -> { // 左下
                cropRect.left = min(x, cropRect.right - minCropSize)
                cropRect.bottom = max(y, cropRect.top + minCropSize)
            }
        }
    }

    // ---- 缩放手势监听 ----

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val bmp = bitmap ?: return true
            // 计算当前缩放值（取矩阵 X 方向缩放分量）
            val matrixValues = FloatArray(9)
            imageMatrix.getValues(matrixValues)
            val currentScale = matrixValues[Matrix.MSCALE_X]
            // 初始缩放值
            val initValues = FloatArray(9)
            initMatrix.getValues(initValues)
            val initScale = initValues[Matrix.MSCALE_X]

            var scaleFactor = detector.scaleFactor
            // 最大缩放限制（相对于初始缩放）
            val maxScale = initScale * maxScaleFactor
            val newScale = currentScale * scaleFactor
            if (newScale > maxScale) {
                scaleFactor = maxScale / currentScale
            }

            // 以双指中心点为缩放中心
            imageMatrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
            clampImageMatrix()
            invalidate()
            return true
        }
    }
}
