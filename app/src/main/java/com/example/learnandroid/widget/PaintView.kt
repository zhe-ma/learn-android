package com.example.learnandroid.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.learnandroid.utils.FileUtil
import com.example.learnandroid.utils.dp

class PaintView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null) {
            return
        }
        testBitmapCanvas()
        drawBg(canvas)
        drawPaint(canvas)
        drawBitmap(canvas)
    }

    private fun drawBitmap(canvas: Canvas) {
        val paint = Paint()
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL
        paint.strokeWidth = 3.dp.toFloat()

        val bitmap = FileUtil.loadBitmapFromAssets(context, "canvasbmp.jpg")
//        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        val rectF = RectF(0f, 0f, bitmap.width.toFloat() / 2, bitmap.height.toFloat() / 2)
//        canvas.drawBitmap(bitmap, null, rectF, paint)

//        canvas.rotate(45f, bitmap.width.toFloat() / 4, bitmap.height.toFloat() / 4)
//        val rect1 = Rect(0, 0, bitmap.width, bitmap.height)
//        val rect2 =  Rect(100, 100, bitmap.width / 2, bitmap.height / 2)
//        canvas.drawBitmap(bitmap, rect1, rect2, paint)

        val matrix = Matrix().apply {
            setValues(floatArrayOf(
                0.71325046f, -0.70090926f, 0.0f,
                0.70090926f, 0.71325046f, 0.0f,
                0.0f, 0.0f, 1.0f
            ))
        }

//        bitmap.rota

//        canvas.matrix = matrix
        val rectF11 = Rect(108, 663, 276, 831)

        matrix.postTranslate(-rectF11.left.toFloat(), -rectF11.top.toFloat())
        val rectF12 = Rect(0, 0, 158, 158)
//        canvas.rectc
//        canvas.drawBitmap(bitmap, rectF11, rectF12, paint)

        canvas.clipRect(rectF12)
        canvas.drawBitmap(bitmap, matrix, paint)
    }

    private fun drawBg(canvas: Canvas) {
        canvas.drawColor(Color.YELLOW)
    }

    private fun drawPaint(canvas: Canvas) {
        val paint = Paint()
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10.dp.toFloat()
        canvas.drawPoints(floatArrayOf(10f, 20f, 30f, 60f, 60f, 120f), paint)
        canvas.drawLine(300f, 300f, 500f , 600f, paint)
    }

    private fun testBitmapCanvas() {
        val bitmap = FileUtil.loadBitmapFromAssets(context, "canvasbmp.jpg").copy(Bitmap.Config.ARGB_8888, true)
//        val bitmap = Bitmap.createBitmap(900, 900, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 10.dp.toFloat()
        canvas.drawRect(10f, 10f, 400f, 400f, paint)
        canvas.drawPoints(floatArrayOf(10f, 20f, 30f, 60f, 60f, 120f), paint)
    }
}