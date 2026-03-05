package com.example.learnandroid.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.animation.DecelerateInterpolator
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
    private val imageMatrix = Matrix()
    private val initMatrix = Matrix()

    // ---- 裁剪框 ----
    private val cropRect = RectF()
    private val initCropRect = RectF()
    // 初始化时裁剪框对应的 bitmap 坐标区域（作为「未裁剪」的基准）
    private val initCropBitmapRect = RectF()

    // ---- 脏标记：裁剪结果是否和原图不一样 ----
    private var isDirty = false

    // ---- 对外回调 ----
    var onDirtyChanged: ((Boolean) -> Unit)? = null

    // ---- 画笔 ----
    private val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val maskPaint = Paint().apply {
        color = Color.argb(150, 0, 0, 0)
        style = Paint.Style.FILL
    }
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
        strokeCap = Paint.Cap.ROUND
    }

    // ---- 重置按钮绘制相关 ----
    private val resetBtnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(180, 30, 30, 30)
        style = Paint.Style.FILL
    }
    private val resetBtnTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }
    private val resetBtnRect = RectF()          // 重置按钮的点击区域
    private val resetBtnPaddingH = 24f
    private val resetBtnPaddingV = 10f
    private val resetBtnMarginTop = 20f         // 距裁剪框顶部的距离

    // ---- 把手尺寸 ----
    private val handleLength = 40f
    private val handleTouchRadius = 60f

    // ---- 手势：缩放 ----
    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())

    // ---- 触摸状态 ----
    private enum class TouchMode { NONE, DRAG_IMAGE, DRAG_HANDLE }
    private var touchMode = TouchMode.NONE
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var activeHandle = -1

    // ---- 裁剪框最小尺寸 ----
    private val minCropSize = 80f

    // ---- 最大缩放比（相对于初始缩放）----
    private val maxScaleFactor = 10f

    // ---- 居中动画 ----
    private var centerAnimator: ValueAnimator? = null
    private val centerAnimRunnable = Runnable { startCenterAnimation() }
    private val centerAnimDelay = 500L

    // ---- 回弹动画 ----
    private var springAnimator: ValueAnimator? = null

    // ---- 裁剪框左右边距 ----
    private val cropHorizontalPadding = 40f

    // ---- 对外接口 ----

    fun setImageBitmap(bmp: Bitmap) {
        bitmap = bmp
        post { initLayout() }
    }

    fun reset() {
        centerAnimator?.cancel()
        springAnimator?.cancel()
        removeCallbacks(centerAnimRunnable)
        imageMatrix.set(initMatrix)
        cropRect.set(initCropRect)
        setDirty(false)
        invalidate()
    }

    fun crop(): Bitmap? {
        val bmp = bitmap ?: return null
        val invertMatrix = Matrix()
        if (!imageMatrix.invert(invertMatrix)) return null
        val srcRect = RectF(cropRect)
        invertMatrix.mapRect(srcRect)
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

    // ---- 私有工具方法 ----

    /**
     * 重新计算当前裁剪结果是否和初始裁剪结果一致，并更新 isDirty 状态。
     * 判断依据：将当前 cropRect 通过 imageMatrix 逆矩阵映射回 bitmap 坐标系，
     * 与初始化时记录的 initCropBitmapRect 比较，误差在阈值内则认为未裁剪（不脏）。
     */
    private fun updateDirtyState() {
        if (initCropBitmapRect.isEmpty) {
            setDirty(false)
            return
        }
        val invertMatrix = Matrix()
        if (!imageMatrix.invert(invertMatrix)) {
            setDirty(true)
            return
        }

        // 将当前裁剪框映射回 bitmap 坐标
        val currentCropBitmapRect = RectF(cropRect)
        invertMatrix.mapRect(currentCropBitmapRect)

        val threshold = 1f
        val same =
            Math.abs(currentCropBitmapRect.left - initCropBitmapRect.left) <= threshold &&
            Math.abs(currentCropBitmapRect.top - initCropBitmapRect.top) <= threshold &&
            Math.abs(currentCropBitmapRect.right - initCropBitmapRect.right) <= threshold &&
            Math.abs(currentCropBitmapRect.bottom - initCropBitmapRect.bottom) <= threshold

        setDirty(!same)
    }

    private fun setDirty(dirty: Boolean) {
        if (isDirty != dirty) {
            isDirty = dirty
            onDirtyChanged?.invoke(dirty)
            invalidate()
        }
    }

    private fun getImageBounds(): RectF {
        val bmp = bitmap ?: return RectF()
        val src = RectF(0f, 0f, bmp.width.toFloat(), bmp.height.toFloat())
        val dst = RectF()
        imageMatrix.mapRect(dst, src)
        return dst
    }

    /**
     * 修正图片矩阵，确保图片始终覆盖裁剪框：
     * 1. 若图片宽或高小于裁剪框，先等比放大到恰好覆盖
     * 2. 再修正平移，使图片边缘不缩进裁剪框内部
     */
    private fun clampImageMatrix() {
        var bounds = getImageBounds()

        // 第一步：若图片尺寸小于裁剪框，强制放大到恰好覆盖
        val scaleW = if (bounds.width() < cropRect.width()) cropRect.width() / bounds.width() else 1f
        val scaleH = if (bounds.height() < cropRect.height()) cropRect.height() / bounds.height() else 1f
        val forceScale = maxOf(scaleW, scaleH)
        if (forceScale > 1f) {
            imageMatrix.postScale(forceScale, forceScale, cropRect.centerX(), cropRect.centerY())
            bounds = getImageBounds()
        }

        // 第二步：修正平移
        var dx = 0f
        var dy = 0f
        if (bounds.left > cropRect.left) dx = cropRect.left - bounds.left
        if (bounds.right < cropRect.right) dx = cropRect.right - bounds.right
        if (bounds.top > cropRect.top) dy = cropRect.top - bounds.top
        if (bounds.bottom < cropRect.bottom) dy = cropRect.bottom - bounds.bottom
        if (dx != 0f || dy != 0f) {
            imageMatrix.postTranslate(dx, dy)
        }
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

        // 裁剪框左右留边距，按图片宽高比计算裁剪框高度
        val cropW = vw - cropHorizontalPadding * 2
        val imgRatio = bmp.height.toFloat() / bmp.width.toFloat()
        val cropH = min(cropW * imgRatio, vh - cropHorizontalPadding * 2)
        val cropLeft = (vw - cropW) / 2f
        val cropTop = (vh - cropH) / 2f

        cropRect.set(cropLeft, cropTop, cropLeft + cropW, cropTop + cropH)
        initCropRect.set(cropRect)

        // 图片缩放到恰好覆盖裁剪框
        val scale = max(cropW / bmp.width, cropH / bmp.height)
        val scaledW = bmp.width * scale
        val scaledH = bmp.height * scale
        val dx = (vw - scaledW) / 2f
        val dy = (vh - scaledH) / 2f

        imageMatrix.reset()
        imageMatrix.setScale(scale, scale)
        imageMatrix.postTranslate(dx, dy)
        initMatrix.set(imageMatrix)

        // 记录初始裁剪框对应的 bitmap 坐标区域，作为「未裁剪」的基准
        val invertInit = Matrix()
        if (imageMatrix.invert(invertInit)) {
            initCropBitmapRect.set(cropRect)
            invertInit.mapRect(initCropBitmapRect)
        }

        setDirty(false)
        invalidate()
    }

    // ---- 绘制 ----

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bmp = bitmap ?: return

        // 1. 绘制图片
        canvas.drawBitmap(bmp, imageMatrix, imagePaint)

        // 2. 绘制蒙层
        val vw = width.toFloat()
        val vh = height.toFloat()
        canvas.drawRect(0f, 0f, vw, cropRect.top, maskPaint)
        canvas.drawRect(0f, cropRect.bottom, vw, vh, maskPaint)
        canvas.drawRect(0f, cropRect.top, cropRect.left, cropRect.bottom, maskPaint)
        canvas.drawRect(cropRect.right, cropRect.top, vw, cropRect.bottom, maskPaint)

        // 3. 绘制裁剪框边框
        canvas.drawRect(cropRect, borderPaint)

        // 4. 绘制四角把手
        drawHandles(canvas)

        // 5. 绘制重置按钮（仅 isDirty 时显示）
        if (isDirty) {
            drawResetButton(canvas)
        }
    }

    private fun drawHandles(canvas: Canvas) {
        val l = cropRect.left
        val t = cropRect.top
        val r = cropRect.right
        val b = cropRect.bottom
        val hl = handleLength
        // 左上
        canvas.drawLine(l, t, l + hl, t, handlePaint)
        canvas.drawLine(l, t, l, t + hl, handlePaint)
        // 右上
        canvas.drawLine(r - hl, t, r, t, handlePaint)
        canvas.drawLine(r, t, r, t + hl, handlePaint)
        // 右下
        canvas.drawLine(r - hl, b, r, b, handlePaint)
        canvas.drawLine(r, b - hl, r, b, handlePaint)
        // 左下
        canvas.drawLine(l, b - hl, l, b, handlePaint)
        canvas.drawLine(l, b, l + hl, b, handlePaint)
    }

    private fun drawResetButton(canvas: Canvas) {
        val text = "重置"
        val textWidth = resetBtnTextPaint.measureText(text)
        val btnW = textWidth + resetBtnPaddingH * 2
        val btnH = resetBtnTextPaint.textSize + resetBtnPaddingV * 2
        val btnCenterX = cropRect.centerX()
        val btnTop = cropRect.top + resetBtnMarginTop

        resetBtnRect.set(
            btnCenterX - btnW / 2f,
            btnTop,
            btnCenterX + btnW / 2f,
            btnTop + btnH
        )

        // 绘制胶囊背景
        canvas.drawRoundRect(resetBtnRect, btnH / 2f, btnH / 2f, resetBtnPaint)

        // 绘制文字（垂直居中）
        val textY = resetBtnRect.centerY() - (resetBtnTextPaint.descent() + resetBtnTextPaint.ascent()) / 2f
        canvas.drawText(text, btnCenterX, textY, resetBtnTextPaint)
    }

    // ---- 触摸事件 ----

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // 取消待执行的居中动画和回弹动画
                removeCallbacks(centerAnimRunnable)
                centerAnimator?.cancel()
                springAnimator?.cancel()

                val x = event.x
                val y = event.y

                // 优先判断重置按钮点击
                if (isDirty && resetBtnRect.contains(x, y)) {
                    reset()
                    return true
                }

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
                        // 允许图片超出边界（松手后回弹），但加阻尼减少超出感
                        val bounds = getImageBounds()
                        val dampedDx = applyDamping(dx, bounds.left - cropRect.left, bounds.right - cropRect.right)
                        val dampedDy = applyDamping(dy, bounds.top - cropRect.top, bounds.bottom - cropRect.bottom)
                        imageMatrix.postTranslate(dampedDx, dampedDy)
                        lastTouchX = x
                        lastTouchY = y
                        invalidate()
                    }
                    TouchMode.DRAG_HANDLE -> {
                        updateCropRect(activeHandle, x, y)
                        clampImageMatrix()
                        invalidate()
                    }
                    else -> {}
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                when (touchMode) {
                    TouchMode.DRAG_HANDLE -> {
                        // 裁剪框宽高发生了变化，重新计算脏状态并延迟触发居中动画
                        updateDirtyState()
                        postDelayed(centerAnimRunnable, centerAnimDelay)
                    }
                    TouchMode.DRAG_IMAGE -> {
                        // 图片移动后先触发回弹，回弹结束后再判断脏状态
                        startSpringAnimationIfNeeded()
                    }
                    else -> {}
                }
                touchMode = TouchMode.NONE
                activeHandle = -1
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                touchMode = TouchMode.NONE
            }
        }
        return true
    }

    private fun hitTestHandle(x: Float, y: Float): Int {
        val corners = arrayOf(
            PointF(cropRect.left, cropRect.top),
            PointF(cropRect.right, cropRect.top),
            PointF(cropRect.right, cropRect.bottom),
            PointF(cropRect.left, cropRect.bottom)
        )
        for (i in corners.indices) {
            val dx = x - corners[i].x
            val dy = y - corners[i].y
            if (sqrt(dx * dx + dy * dy) <= handleTouchRadius) return i
        }
        return -1
    }

    private fun updateCropRect(handle: Int, x: Float, y: Float) {
        // 裁剪框不能超出图片边界
        val imgBounds = getImageBounds()
        val clampedX = x.coerceIn(imgBounds.left, imgBounds.right)
        val clampedY = y.coerceIn(imgBounds.top, imgBounds.bottom)

        when (handle) {
            0 -> { // 左上
                cropRect.left = min(clampedX, cropRect.right - minCropSize)
                cropRect.top = min(clampedY, cropRect.bottom - minCropSize)
            }
            1 -> { // 右上
                cropRect.right = max(clampedX, cropRect.left + minCropSize)
                cropRect.top = min(clampedY, cropRect.bottom - minCropSize)
            }
            2 -> { // 右下
                cropRect.right = max(clampedX, cropRect.left + minCropSize)
                cropRect.bottom = max(clampedY, cropRect.top + minCropSize)
            }
            3 -> { // 左下
                cropRect.left = min(clampedX, cropRect.right - minCropSize)
                cropRect.bottom = max(clampedY, cropRect.top + minCropSize)
            }
        }
    }

    // ---- 回弹动画 ----

    /**
     * 对移动量施加阻尼：当图片已超出边界时，继续拖动的位移会衰减，产生橡皮筋感。
     * @param delta 原始位移
     * @param overLeft  图片左边超出量（正值 = 图片左边在裁剪框左边右侧，即超出）
     * @param overRight 图片右边超出量（负值 = 图片右边在裁剪框右边左侧，即超出）
     */
    private fun applyDamping(delta: Float, overLeft: Float, overRight: Float): Float {
        val dampFactor = 0.3f
        return when {
            delta > 0 && overLeft > 0 -> delta * dampFactor   // 向右拖但左边已超出
            delta < 0 && overRight < 0 -> delta * dampFactor  // 向左拖但右边已超出
            else -> delta
        }
    }

    /**
     * 检查图片是否超出裁剪框边界，若超出则用动画平滑回弹到合法位置。
     * 同时处理：尺寸不足时先放大，再修正平移。
     */
    private fun startSpringAnimationIfNeeded() {
        val startValues = FloatArray(9).also { imageMatrix.getValues(it) }

        // 用临时矩阵计算目标状态（复用 clampImageMatrix 逻辑）
        val targetMatrix = Matrix().apply { setValues(startValues.clone()) }
        val tempView = object {
            // 在临时矩阵上执行 clamp 逻辑
            fun clamp(): Matrix {
                val bmp = bitmap ?: return targetMatrix
                // 计算当前 targetMatrix 下的图片边界
                fun getBounds(m: Matrix): RectF {
                    val src = RectF(0f, 0f, bmp.width.toFloat(), bmp.height.toFloat())
                    val dst = RectF()
                    m.mapRect(dst, src)
                    return dst
                }

                var bounds = getBounds(targetMatrix)

                // 第一步：尺寸不足时放大
                val scaleW = if (bounds.width() < cropRect.width()) cropRect.width() / bounds.width() else 1f
                val scaleH = if (bounds.height() < cropRect.height()) cropRect.height() / bounds.height() else 1f
                val forceScale = maxOf(scaleW, scaleH)
                if (forceScale > 1f) {
                    targetMatrix.postScale(forceScale, forceScale, cropRect.centerX(), cropRect.centerY())
                    bounds = getBounds(targetMatrix)
                }

                // 第二步：修正平移
                var dx = 0f
                var dy = 0f
                if (bounds.left > cropRect.left) dx = cropRect.left - bounds.left
                if (bounds.right < cropRect.right) dx = cropRect.right - bounds.right
                if (bounds.top > cropRect.top) dy = cropRect.top - bounds.top
                if (bounds.bottom < cropRect.bottom) dy = cropRect.bottom - bounds.bottom
                if (dx != 0f || dy != 0f) targetMatrix.postTranslate(dx, dy)

                return targetMatrix
            }
        }
        tempView.clamp()

        val endValues = FloatArray(9).also { targetMatrix.getValues(it) }

        // 检查是否有变化
        var hasChange = false
        for (i in 0..8) {
            if (Math.abs(startValues[i] - endValues[i]) > 0.01f) {
                hasChange = true
                break
            }
        }
        if (!hasChange) {
            // 无需回弹，直接判断脏状态
            updateDirtyState()
            return
        }

        springAnimator?.cancel()
        springAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 250
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                val t = anim.animatedValue as Float
                val animValues = FloatArray(9)
                for (i in 0..8) animValues[i] = lerp(startValues[i], endValues[i], t)
                imageMatrix.setValues(animValues)
                invalidate()
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    // 回弹结束后判断脏状态
                    updateDirtyState()
                }
            })
            start()
        }
    }

    // ---- 居中放大动画 ----

    /**
     * 将裁剪框动画放大居中到可用区域，图片跟随做相同变换。
     * 保持裁剪框宽高比不变，尽可能撑满可用区域（留 padding）。
     */
    private fun startCenterAnimation() {
        val vw = width.toFloat()
        val vh = height.toFloat()
        if (vw <= 0 || vh <= 0) return

        val padding = 40f  // 裁剪框距 View 边缘的最小间距

        // 计算目标裁剪框（保持宽高比，居中放大）
        val cropW = cropRect.width()
        val cropH = cropRect.height()
        val availW = vw - padding * 2
        val availH = vh - padding * 2
        val scale = min(availW / cropW, availH / cropH)

        val targetCropW = cropW * scale
        val targetCropH = cropH * scale
        val targetCropLeft = (vw - targetCropW) / 2f
        val targetCropTop = (vh - targetCropH) / 2f
        val targetCropRect = RectF(targetCropLeft, targetCropTop,
            targetCropLeft + targetCropW, targetCropTop + targetCropH)

        // 记录动画起始状态
        val startCropRect = RectF(cropRect)
        val startMatrixValues = FloatArray(9).also { imageMatrix.getValues(it) }

        // 图片跟随变换：以裁剪框起始中心为缩放中心，scale 后再平移到目标中心
        // 最终图片矩阵 = startMatrix × scale(scale, cx, cy) × translate(dcx, dcy)
        // 其中 dcx = targetCropCenterX - startCropCenterX，dcy 同理
        val startCropCx = startCropRect.centerX()
        val startCropCy = startCropRect.centerY()
        val targetCropCx = targetCropRect.centerX()
        val targetCropCy = targetCropRect.centerY()

        // 预计算动画结束时图片矩阵（t=1 时的状态）
        val endMatrix = Matrix().apply {
            setValues(startMatrixValues.clone())
            postScale(scale, scale, startCropCx, startCropCy)
            postTranslate(targetCropCx - startCropCx, targetCropCy - startCropCy)
        }
        val endMatrixValues = FloatArray(9).also { endMatrix.getValues(it) }

        centerAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                val t = anim.animatedValue as Float

                // 插值裁剪框
                cropRect.left = lerp(startCropRect.left, targetCropRect.left, t)
                cropRect.top = lerp(startCropRect.top, targetCropRect.top, t)
                cropRect.right = lerp(startCropRect.right, targetCropRect.right, t)
                cropRect.bottom = lerp(startCropRect.bottom, targetCropRect.bottom, t)

                // 插值图片矩阵（逐分量插值，保证图片与裁剪框同步运动）
                val animValues = FloatArray(9)
                for (i in 0..8) {
                    animValues[i] = lerp(startMatrixValues[i], endMatrixValues[i], t)
                }
                imageMatrix.setValues(animValues)

                invalidate()
            }
            start()
        }
    }

    private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t

    // ---- 缩放手势监听 ----

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val matrixValues = FloatArray(9)
            imageMatrix.getValues(matrixValues)
            val currentScale = matrixValues[Matrix.MSCALE_X]
            val initValues = FloatArray(9)
            initMatrix.getValues(initValues)
            val initScale = initValues[Matrix.MSCALE_X]

            var scaleFactor = detector.scaleFactor
            val maxScale = initScale * maxScaleFactor
            val newScale = currentScale * scaleFactor
            if (newScale > maxScale) {
                scaleFactor = maxScale / currentScale
            }

            imageMatrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
            // 不实时 clamp，允许短暂超出，松手后回弹
            invalidate()
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            // 缩放结束后先触发回弹，回弹结束后再判断脏状态
            startSpringAnimationIfNeeded()
        }
    }
}
